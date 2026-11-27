import os
import sys
import json
import jsonpickle
from subprocess import Popen, PIPE
import signal
from contextlib import contextmanager
import traceback


checkovProcess = None


def onTimeout(signum, frame):
    if checkovProcess is not None:
        checkovProcess.kill()
    sys.exit(2)


@contextmanager
def timeout(time):
    # Register a function to raise a TimeoutError on the signal.
    signal.signal(signal.SIGALRM, onTimeout)
    # Schedule the signal to be sent after ``time``.
    signal.alarm(time)
    yield


DEFAULT_TIMEOUT = 15 * 60


def getTimeout(timeoutString):
    if not timeoutString.isdigit():
        return DEFAULT_TIMEOUT
    return int(timeoutString)


class Result:
    def __init__(self, filename, message, patternId, line):
        self.filename = filename
        self.message = message
        self.patternId = patternId
        self.line = line


class Configuration:
    def __init__(self, rules, files):
        self.rules = rules
        self.files = files


def toJson(obj):
    return jsonpickle.encode(obj, unpicklable=False, keys=True)


def readJsonFile(path):
    with open(path, 'r') as file:
        res = json.loads(file.read())
    return res


CHECKOV_CONFIG_FILES = ['.checkov.yaml', '.checkov.yml']


def findCheckovConfigFile(srcDir):
    for name in CHECKOV_CONFIG_FILES:
        path = os.path.join(srcDir, name)
        if os.path.isfile(path):
            return path
    return None


YAML_FRAMEWORKS = ['kubernetes', 'cloudformation', 'openapi', 'ansible', 'argo_workflows']
JSON_FRAMEWORKS = ['kubernetes', 'cloudformation', 'openapi', 'arm', 'terraform_plan', 'terraform_json']

FRAMEWORKS_BY_EXTENSION = {
    '.tf': ['terraform'],
    '.json': JSON_FRAMEWORKS,
    '.yaml': YAML_FRAMEWORKS,
    '.yml': YAML_FRAMEWORKS,
    '.template': ['cloudformation'],
    '.bicep': ['bicep'],
}

FRAMEWORKS_BY_FILE_NAME = {
    'Chart.yaml': ['helm'],
    'kustomization.yaml': ['kustomize'],
    'kustomization.yml': ['kustomize'],
    'Dockerfile': ['dockerfile'],
    'serverless.yml': ['serverless'],
    'serverless.yaml': ['serverless'],
    'bitbucket-pipelines.yml': ['bitbucket_pipelines'],
    'bitbucket-pipelines.yaml': ['bitbucket_pipelines'],
    'azure-pipelines.yml': ['azure_pipelines'],
    'azure-pipelines.yaml': ['azure_pipelines'],
}


def frameworksForFile(path):
    name = os.path.basename(path)
    extension = os.path.splitext(name)[1]
    frameworks = FRAMEWORKS_BY_FILE_NAME.get(name, []) + FRAMEWORKS_BY_EXTENSION.get(extension, [])
    normalized = '/' + os.path.normpath(path).replace(os.sep, '/').lstrip('/')
    if extension in ('.yml', '.yaml'):
        if '/.github/workflows/' in normalized:
            frameworks = frameworks + ['github_actions']
        if '/.circleci/' in normalized:
            frameworks = frameworks + ['circleci_pipelines']
        if name.endswith(('.gitlab-ci.yml', '.gitlab-ci.yaml')):
            frameworks = frameworks + ['gitlab_ci']
    return frameworks


def listFiles(srcDir):
    for root, dirs, files in os.walk(srcDir):
        dirs[:] = [d for d in dirs if d != '.git']
        for name in files:
            yield os.path.relpath(os.path.join(root, name), srcDir)


def secretsEnabled(rules):
    patternIds = rules[1].split(',') if rules[1] else []
    return patternIds == [] or any(p.startswith('CKV_SECRET_') for p in patternIds)


def selectFrameworks(files, rules):
    frameworks = []
    for f in files:
        for framework in frameworksForFile(f):
            if framework not in frameworks:
                frameworks.append(framework)
    if secretsEnabled(rules):
        frameworks.append('secrets')
    return frameworks


def configFileSetsFrameworks(configFile):
    with open(configFile, 'r') as file:
        return any(line.split(':')[0].strip() in ('framework', 'skip-framework') for line in file)


def runCheckov(config, srcDir):
    file_opts = ([opt for f in config.files for opt in ['-f', f]]
                 if config.files
                 else ['-d', '.'])
    processEnv = os.environ.copy()
    processEnv["http_proxy"] = "http://127.0.0.1"
    processEnv["https_proxy"] = "https://127.0.0.1"
    processEnv["LOG_LEVEL"] = "INFO"
    processEnv["RENDER_EDGES_DUPLICATE_ITER_COUNT"] = "10"
    processEnv.setdefault("CHECKOV_PARALLELIZATION_TYPE", "none")

    checkov_config = findCheckovConfigFile(srcDir)
    config_file_opts = ['--config-file', checkov_config] if checkov_config else []

    # If rules are specified, it's because the tool is relying on the UI patterns. If not, it's using the configuration file
    if config.rules != [] and config.rules[1] == '' and config_file_opts != []: #if rules are empty but there's a config file
        command = ['checkov', '-o', 'json', '--quiet', '--skip-download'] + config_file_opts + file_opts
        if configFileSetsFrameworks(checkov_config):
            frameworks = None
        else:
            frameworks = selectFrameworks(config.files or listFiles(srcDir), ['-c', ''])
    else: # if there are rules
        command = ['checkov', '-o', 'json', '--quiet', '--skip-download'] + config.rules + file_opts
        frameworks = selectFrameworks(config.files or listFiles(srcDir), config.rules)

    if frameworks == []:
        return []
    if frameworks is not None:
        command = command + ['--framework', ','.join(frameworks)]

    global checkovProcess
    checkovProcess = Popen(
        command,
        stdout=PIPE,
        cwd=srcDir,
        env=processEnv
    )

    stdout = checkovProcess.communicate()[0]

    if len(stdout) > 0:
        return json.loads(stdout.decode('utf-8'))
    else:
        return None


def readConfiguration(configFile):
    try:
        configuration = readJsonFile(configFile)
    except:
        configuration = {}
    files = configuration.get('files') or []
    tools = [t for t in configuration.get('tools') or []
             if t.get('name') == 'checkov']
    if tools and 'patterns' in tools[0]:
        checkov = tools[0]
        rules = ['-c', ','.join([p['patternId']
                                 for p in checkov.get('patterns') or []])]
    else:
        rules = ['-c', '']

    return Configuration(rules, files)


def runTool(configFile, srcDir):
    config = readConfiguration(configFile)
    reports = runCheckov(config,srcDir)

    # Checkov can either return a single report or a list of reports
    # for every "check_type" ("kubernetes", "serverless", etc.)
    # When it returns a root report we wrap it in a list
    if isinstance(reports, dict):
        reports = [reports]

    res = []

    for report in reports:
        if 'results' in report: # if the tool runs without specified files 'results' key does not exist
            failed_checks = report['results']['failed_checks']
            for failed_check in failed_checks:
                filename = failed_check['repo_file_path'].lstrip('/')
                res.append(Result(
                    filename, failed_check['check_name'], failed_check['check_id'], failed_check['file_line_range'][0]))
        else:
            print("No specified files")
    return res


def resultsToJson(results):
    return os.linesep.join([toJson(res) for res in results])


if __name__ == '__main__':
    with timeout(getTimeout(os.environ.get('TIMEOUT_SECONDS') or '')):
        try:
            results = runTool("/.codacyrc", "/src") 
            print(resultsToJson(results))
        except Exception:
            traceback.print_exc()
            sys.exit(1)
