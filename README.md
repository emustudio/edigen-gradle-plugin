# Edigen Gradle Plugin
![edigen-gradle-plugin](https://github.com/emustudio/edigen-gradle-plugin/workflows/edigen-gradle-plugin/badge.svg?branch=master)

This is a Gradle plugin which automatically generates an instruction decoder and disassembler of an emuStudio CPU
plugin from a specification using Edigen.

It is a reimplementation of [edigen-maven-plugin](https://github.com/sulir/edigen-maven-plugin) to be used with Gradle.

## License

This project is released under [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.html) license.

## Usage

```
plugins {
  id 'edigen' version 1.0.0
}

edigen {
  decoderName = 'decoder.package.ClassName'
  disassemblerName = 'disassembler.package.ClassName'
}
```

## Reference

Ths plugin provides a single task `edigen`. It can be configured with the following parameters.

### Required parameters

- `decoderName` - the generated instruction decoder package + class name
- `disassemblerName` - the generated disassembler package + class name

### Optional parameters

- `specification` - the specification file location; _default_: `src/main/edigen/cpu.eds`
- `decoderTemplate` - the template to use for the decoder generation
- `decoderOutputDir` - the generated decoder output directory; _default_: `target/generated-sources/edigen/` + path
   obtained from the decoder package name
- `disassemblerTemplate` - the template to use for the disassembler generation
- `disassemblerOutputDir` - the generated disassembler output directory; _default_: `target/generated-sources/edigen/` +
   path obtained from the disassembler package name
- `debug` - setting to true enables Edigen's debug mode
