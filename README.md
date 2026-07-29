# Codacy Checkov

This is the docker engine we use at Codacy to have [Checkov](http://www.checkov.io/) support.

## Usage

You can create the docker by doing:

  ```bash
  docker build -t codacy-checkov:latest .
  ```

The docker is ran with the following command:

  ```bash
  docker run -it -v $srcDir:/src codacy-checkov:latest
  ```

## Generate Docs

 1. Update the version in `requirements.txt`
 2. Install the dependencies:

```bash
pip3 install -r requirements.txt
```

 3. Run the DocGenerator:

```bash
./scala-cli doc-generator.sc
```

## Test

We use the [codacy-plugins-test](https://github.com/codacy/codacy-plugins-test) to test our external tools integration.
You can follow the instructions there to make sure your tool is working as expected.

## Agent Playbook: Updating This Repository End-to-End

This section is written for an AI coding agent (or a human) tasked with updating this repo — most commonly bumping the wrapped Checkov version, but also base image / orb / dependency bumps. Follow it top to bottom; it tells you what to change, how to regenerate derived files, how to test locally, and how to interpret CI so you can iterate on failures without guessing.

### 1. What this repository is

This is a **Codacy engine**: a thin Python wrapper (`src/codacy_checkov.py`) that shells out to the [Checkov](http://www.checkov.io/) CLI and packages it as a Docker image Codacy's platform can run against a customer's infrastructure-as-code source. `codacy_checkov.py` reads `/.codacyrc`, builds a `checkov` command line from the enabled patterns (or a `.checkov.yaml`/`.checkov.yml` config file found in the source directory), runs it, and translates the JSON `failed_checks` output into Codacy's result format.

The `docs/` directory is not just documentation — it is **machine-consumed configuration**:

- `docs/patterns.json` — the full list of Checkov checks ("patterns") Codacy knows about, generated. Do not hand-edit.
- `docs/description/description.json` + `docs/description/*.md` — human-readable titles/descriptions per pattern, generated. Do not hand-edit.
- `docs/multiple-tests/*` — fixtures used by `codacy-plugins-test` to validate the engine produces the results it claims to for real code samples.
- `docs/tool-description.md` — short blurb about the tool, hand-maintained.

The generated artifacts above come from **`doc-generator.sc`**, a Scala CLI script (run via the `scala-cli` launcher checked into this repo, built on `com.codacy::codacy-engine-scala-seed`) that reads the target version straight out of `requirements.txt` (`case s"checkov==$version" => ...`), then shells out to the locally installed `checkov -l` command to scrape the list of checks and their metadata into `docs/patterns.json` and `docs/description/*`. This means the generator needs the **`checkov` CLI actually installed** (via `pip install -r requirements.txt`) and network access to run.

### 2. Files that encode versions — check all of these on every update

| File | What it controls | What to check |
|---|---|---|
| `requirements.txt` → `checkov==X.Y.Z` | Which Checkov release is bundled and used at runtime | Bump to the target version; this is also what `doc-generator.sc` reads to compute doc URLs/version strings. |
| `Dockerfile` → `FROM python:...-alpine...` | Python/Alpine runtime the packaged app runs on | Only bump if the new Checkov version raises its minimum Python requirement, or on routine base-image hygiene. |
| `.circleci/config.yml` → `codacy/base` orb | Shared CircleCI steps (checkout, version tagging, docker publish) | Check the latest published version. |
| `.circleci/config.yml` → `codacy/plugins-test` orb | Runs `codacy-plugins-test` in CI | Same as above. |
| `.circleci/config.yml` → CI Docker image (`cimg/python:...`) and Java install step | Build environment `scala-cli`/`doc-generator.sc` runs in | Bump only if scala-cli/doc-generator tooling requires it. |
| `doc-generator.sc` → `//> using dep ...` lines | Scala/codacy-engine-scala-seed/os-lib/upickle/requests versions used by the doc generator itself | Bump independently of the Checkov version if the generator's own dependencies need updating. |

### 3. Step-by-step update procedure

1. **Bump the version** in `requirements.txt` (and any other files from the table above that are in scope for this task).
2. **Install the dependencies and regenerate the docs**:
   ```bash
   pip3 install -r requirements.txt
   ./scala-cli doc-generator.sc
   ```
   This requires `checkov` to be installed (from the step above) and network access, since the generator invokes `checkov -l` and fetches metadata. Review the diff in `docs/patterns.json` and `docs/description/*` for new/removed/renamed checks, and check whether `docs/multiple-tests/*` fixtures reference any check IDs that changed.
3. **Build the Docker image**:
   ```bash
   docker build -t codacy-checkov:latest .
   ```
4. **Run it locally** against a sample source tree to sanity-check behavior before pushing:
   ```bash
   docker run -it -v $srcDir:/src codacy-checkov:latest
   ```
5. **Run `codacy-plugins-test` locally** before pushing — clone https://github.com/codacy/codacy-plugins-test and run the relevant test commands (this repo's CI runs it in "multiple tests" mode, per `docs/multiple-tests/*`) against your local image tag.
6. **Iterate on failures**, re-running only the relevant test command after each fix.
7. **Commit** the version bump together with the regenerated `docs/patterns.json` and `docs/description/*` files in one change.
8. **Push and open a PR.**
9. **Poll the PR's real CI checks until they all pass — local validation is NOT the finish line.** After every push, run `gh pr checks <pr-url>` and keep re-polling (short sleep while any check is `pending`) until all checks finish. If a check fails, fetch its actual log (don't guess), find the true root cause, fix it, push again (never `--no-verify`, never force-push), and re-poll. Repeat until every check is green. **The CI environment's toolchain can differ from your local one** — CircleCI installs its own JDK and fetches `scala-cli` fresh — so a clean local run does not guarantee CI passes. Only stop iterating when every check passes, or you hit a genuine product/infra decision that needs a human.

### 4. Common failure modes and fixes

| Symptom | Likely cause | Fix |
|---|---|---|
| `doc-generator.sc` fails to find `checkov` on `PATH` | Dependencies not installed before running the generator | Run `pip3 install -r requirements.txt` first (CI does this in the "Install Python deps" step before "Run doc-generator"). |
| New/renamed check IDs appear in `docs/patterns.json` but `docs/multiple-tests/*` fixtures still reference old IDs | Checkov added/removed/renamed checks between versions | Update or remove the affected fixture files so `codacy-plugins-test` doesn't fail on stale check IDs. |
| CI's "Run doc-generator" step fails but a local `./scala-cli doc-generator.sc` run succeeds | CI fetches `scala-cli` fresh via `virtuslab.github.io/scala-cli-packages` and needs a JDK it installs itself (`openjdk-17-jre`) | Confirm the CircleCI job's Java/scala-cli install steps still match what `doc-generator.sc`'s `//> using` directives require. |

### 5. Definition of done

- `requirements.txt` (and any other in-scope files from the table in section 2) reflect the target version(s).
- `docs/patterns.json` and `docs/description/*` regenerated via `./scala-cli doc-generator.sc`, with any stale `docs/multiple-tests/*` fixture references resolved.
- Docker image builds successfully (`docker build -t codacy-checkov:latest .`).
- `codacy-plugins-test` passes locally against the freshly built image.
- **After pushing and opening/updating the PR, every CI check on it is green.** Poll `gh pr checks <pr-url>` and iterate on any failure until all pass.

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features

- Identify new Static Analysis issues
- Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
- Auto-comments on Commits and Pull Requests
- Integrations with Slack, HipChat, Jira, YouTrack
- Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
