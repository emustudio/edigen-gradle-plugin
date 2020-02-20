package net.emustudio.edigen.plugin

import org.gradle.api.Project

class EdigenPluginExtension {
    private final Project project

    /**
     * Edigen Specification file.
     */
    File specification = project.file('src/main/edigen/cpu.eds')

    /**
     * Instruction decoder package + class name.
     */
    String decoderName

    /**
     * Instruction decoder template file.
     */
    File decoderTemplate

    /**
     * Disassembler package + class name.
     */
    String disassemblerName

    /**
     * Disassembler template file.
     */
    File disassemblerTemplate

    /**
     * Disassembler output directory.
     *
     * The default is "$project.buildDir/generated-sources/edigen"
     */
    File disassemblerOutputDir = project.file("$project.buildDir/generated-sources/edigen")

    /**
     * Instruction decoder output directory.
     *
     * The default is "$project.buildDir/generated-sources/edigen"
     */
    File decoderOutputDir = project.file("$project.buildDir/generated-sources/edigen")

    /**
     * Debug mode (display tree transformations).
     */
    boolean debug = false

    EdigenPluginExtension(Project project) {
        this.project = project
    }
}
