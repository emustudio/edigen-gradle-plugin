# ADR-001: Model generation inputs and own individual output sources

## Status
Accepted

## Context
The generation task read mutable project configuration during execution and recursively deleted configurable
output directories. Gradle could not track input changes, and generation failure or shared directories could
remove existing sources. Related: emustudio/emuStudio#406.

## Decision
The extension and task expose typed Gradle properties for specifications, templates, class names, debug mode,
and output directories. Directory providers register generated Java sources and their producer dependency.
Gradle tracks the exact generated source files and ownership manifests as publication outputs, so it never
claims an entire shared directory.
Task execution uses captured properties and injected filesystem operations, supporting configuration caching.

Generate both sources in a separate prerequisite task into task-owned staging. Publication depends on its valid
outputs; failed generation prevents publication and Gradle's publication-output cleanup from running, even after
local task history is lost. Each output directory records relative source
paths in a task-specific `.edigen-<owner>-owned-sources` manifest. The owner is derived from the
root-project directory and task path, so one task cannot remove another task's sources in a shared directory.
Only recorded files may be overwritten or removed; unowned collisions fail.
Do not recursively delete configured output directories.

## Consequences
Gradle skips generation for unchanged inputs. Failed generation preserves published sources. Renaming a generated
class removes its previous owned file while preserving unrelated files. Consumers should use dedicated output
directories and clean pre-ownership generated output once. Existing extension assignment syntax remains valid;
code reading extension values must use the Gradle property interface. No runtime plugin interface changes.
