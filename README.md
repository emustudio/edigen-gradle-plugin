# Edigen Gradle Plugin
![Edigen Gradle Plugin Build](https://github.com/emustudio/edigen-gradle-plugin/workflows/Edigen%20Gradle%20Plugin%20Build/badge.svg)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=Gradle%20Plugin&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fnet%2Femustudio%2Fedigen-plugin%2Fnet.emustudio.edigen-plugin.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/net.emustudio.edigen-plugin)

This is a Gradle plugin which automatically generates an instruction decoder and disassembler of an emuStudio CPU
plugin from a specification using [Edigen](https://github.com/emustudio/edigen).

It is a reimplementation of [edigen-maven-plugin](https://github.com/emustudio/edigen-maven-plugin) to be used with Gradle.

For more information, see [official documentation](https://www.emustudio.net/docdevel/emulator_tutorial/index/#CPU_HOWTO)
of emuStudio plugin development. 

## License

This project is released under [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.html) license.

## Usage

NOTE: The plugin is compiled against Java 11 (file version 55). Earlier Java versions are thus not supported.

```
plugins {
  id 'net.emustudio.edigen-plugin' version 1.5.1
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
