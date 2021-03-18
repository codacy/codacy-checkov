import os
import sys
import json
import jsonpickle
from subprocess import Popen, PIPE
import signal
from contextlib import contextmanager
import traceback


@contextmanager
def timeout(time):
    # Register a function to raise a TimeoutError on the signal.
    signal.signal(signal.SIGALRM, lambda: sys.exit(2))
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


def toJson(obj): return jsonpickle.encode(obj, unpicklable=False)


def readJsonFile(path):
    with open(path, 'r') as file:
        res = json.loads(file.read())
    return res


def runCheckov(config):
    file_opts = ([opt for f in config.files for opt in ['-f', f]]
                 if config.files
                 else ['-d', '.'])
    process = Popen(
        ['checkov', '-o', 'json', '--quiet', '--no-guide',
         '--skip-fixes', '--skip-suppressions'] + file_opts + config.rules,
        stdout=PIPE,
        cwd='/src'
    )
    stdout = process.communicate()[0]
    return json.loads(stdout.decode('utf-8'))


def readConfiguration():
    try:
        configuration = readJsonFile('/.codacyrc')
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
        rules = []

    return Configuration(rules, files)


def runTool():
    config = readConfiguration()
    reports = runCheckov(config)

    # Checkov can either return a single report or a list of reports
    # for every "check_type" ("kubernetes", "serverless", etc.)
    # When it returns a root report we wrap it in a list
    if isinstance(reports, dict):
        reports = [reports]

    res = []

    for report in reports:
        failed_checks = report['results']['failed_checks']
        for failed_check in failed_checks:
            filename = failed_check['file_path'].lstrip('/')
            res.append(Result(
                filename, failed_check['check_name'], failed_check['check_id'], failed_check['file_line_range'][0]))
    return res


def resultsToJson(results):
    return os.linesep.join([toJson(res) for res in results])


if __name__ == '__main__':
    with timeout(getTimeout(os.environ.get('TIMEOUT_SECONDS') or '')):
        try:
            results = runTool()
            print(resultsToJson(results))
        except Exception:
            traceback.print_exc()
            sys.exit(1)
