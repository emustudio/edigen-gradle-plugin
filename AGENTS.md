# emuStudio Repo Routing

## Current Repository
- `edigen-gradle-plugin` owns Gradle integration for Edigen, including the Gradle task and DSL used to generate decoder and disassembler sources from `.eds`.

## Sibling Repositories
- [emuLib](https://github.com/emustudio/emuLib): shared plugin API, runtime services, shared UI helpers, and reusable utilities.
- [edigen](https://github.com/emustudio/edigen): decoder/disassembler generator from `.eds` specifications.
- [emuStudio](https://github.com/emustudio/emuStudio): desktop application, bundled plugins, virtual computers, configs, and packaging.
- [emustudio.github.io](https://github.com/emustudio/emustudio.github.io): website, user documentation, developer documentation, and release-facing pages.
- [cpu-testsuite](https://github.com/emustudio/cpu-testsuite): shared CPU instruction test framework and reusable verification helpers.

Before checking or updating any sibling repository, verify that it is checked out locally (typically as a sibling directory of this repository). If the repository is not available locally, do not attempt to modify it — report to the user that it is missing and, if needed, point them to the GitHub link above.

## When To Update Which Repository
- Gradle DSL, task wiring, source generation lifecycle, or build integration for Edigen: update `edigen-gradle-plugin`.
- Generator inputs, parser behavior, or generated source shape: update `edigen`; then verify plugin compatibility in `edigen-gradle-plugin`.
- Shared API or runtime contract used by generated classes: update `emuLib`; then check `emuStudio`.
- Bundled CPU plugins or sample builds that rely on generated sources: update `emuStudio`.
- User or developer documentation for Edigen Gradle usage: update `emustudio.github.io`.

## Tickets And Commits
- Every change must have an existing GitHub ticket.
- Every commit subject must start with the ticket prefix: `[#123] Short summary`.
- If one task touches multiple emuStudio repositories, use the same ticket prefix in each related commit.
