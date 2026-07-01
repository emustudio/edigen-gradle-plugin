# Edigen Gradle Plugin
![Edigen Gradle Plugin Build](https://github.com/emustudio/edigen-gradle-plugin/workflows/Edigen%20Gradle%20Plugin%20Build/badge.svg)
![Maven Central Version](https://img.shields.io/maven-central/v/net.emustudio/edigen-gradle-plugin)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This is a Gradle plugin which automatically generates an instruction decoder and disassembler of an emuStudio CPU
plugin from a specification using [Edigen](https://github.com/emustudio/edigen).

It is a reimplementation of [edigen-maven-plugin](https://github.com/emustudio/edigen-maven-plugin) to be used with Gradle.

For more information, see [official documentation](https://www.emustudio.net/docdevel/emulator_tutorial/index/#CPU_HOWTO)
of emuStudio plugin development. 

## Usage

NOTE: The plugin is compiled against Java 11 (file version 55). Earlier Java versions are thus not supported.

```
plugins {
  id 'net.emustudio.edigen-plugin' version 1.5.2
}

edigen {
  decoderName = 'decoder.package.ClassName'
  disassemblerName = 'disassembler.package.ClassName'
}
```

## Reference

Ths plugin provides a single task `edigen/generateSources`. It can be configured using `edigen` extension block,
with the following parameters.

### Required parameters

- `decoderName` - the generated instruction decoder package + class name
- `disassemblerName` - the generated disassembler package + class name

### Optional parameters

- `specification` - the specification file location; _default_: `src/main/edigen/cpu.eds`
- `decoderTemplate` - the template to use for the decoder generation
- `decoderOutputDir` - the generated decoder output directory; _default_: `target/generated-sources/edigen/`. This path
   will be appended with the decoder package name
- `disassemblerTemplate` - the template to use for the disassembler generation
- `disassemblerOutputDir` - the generated disassembler output directory; _default_: `target/generated-sources/edigen/`.
   This path will be appended with the disassembler package name
- `debug` - setting to true enables Edigen's debug mode

## Publishing to Maven Central

The plugin is published to [Maven Central](https://central.sonatype.com/) using the
[com.vanniktech.maven.publish](https://vanniktech.github.io/gradle-maven-publish-plugin/) plugin, configured in
`build.gradle`. Both the artifact coordinates (`net.emustudio:edigen-gradle-plugin`) and the signing setup are defined
there.

### Prerequisites

- A [Sonatype Central Portal](https://central.sonatype.com/) account with publishing rights for the `net.emustudio`
  namespace.
- A GPG signing key (all published artifacts are signed via `signAllPublications()`).

### Required credentials

The build reads the following credentials from Gradle properties (e.g. `~/.gradle/gradle.properties`) or environment
variables:

- `GPG_KEY` - the ASCII-armored GPG private key used for signing.
- `GPG_PASSWORD` - the passphrase for the GPG key.
- Maven Central (Central Portal) user token, provided to the `com.vanniktech.maven.publish` plugin as
  `mavenCentralUsername` / `mavenCentralPassword` (or the matching `ORG_GRADLE_PROJECT_*` environment variables).

Never commit these values to the repository.

### Releasing

1. Set the release version in `build.gradle` (remove the `-SNAPSHOT` suffix for a final release; keep it for snapshots).
2. Run:

   ```
   ./gradlew publish
   ```

   Snapshot versions are published to the Central Portal snapshots repository. Release versions are uploaded and, thanks
   to `publishToMavenCentral(true, DeploymentValidation.PUBLISHED)`, automatically released once validation succeeds.
3. After a release, bump the version back to the next `-SNAPSHOT` version.

### Automated deployment

Pushing to the `master` branch triggers the `.github/workflows/deploy.yml` workflow, which runs `./gradlew publish`.
The required secrets (`GPG_KEY`, `GPG_PASSWORD`, `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`) are configured as GitHub
Actions repository secrets, so a normal release only requires merging to `master`.
