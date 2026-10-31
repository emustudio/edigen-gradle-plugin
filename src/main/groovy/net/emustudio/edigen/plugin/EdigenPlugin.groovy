/* SPDX-FileCopyrightText: 2020-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class EdigenPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply('java')
        def extension = project.extensions.create('edigen', EdigenPluginExtension)
        extension.specification.convention(project.layout.projectDirectory.file('src/main/edigen/cpu.eds'))
        extension.decoderOutputDir.convention(project.layout.buildDirectory.dir('generated-sources/edigen'))
        extension.disassemblerOutputDir.convention(project.layout.buildDirectory.dir('generated-sources/edigen'))
        extension.debug.convention(false)

        def preparation = project.tasks.register('prepareEdigenSources', EdigenGenerationTask) {
            description = 'Generate decoder and disassembler into private staging'
            specification.set(extension.specification)
            decoderName.set(extension.decoderName)
            decoderTemplate.set(extension.decoderTemplate)
            disassemblerName.set(extension.disassemblerName)
            disassemblerTemplate.set(extension.disassemblerTemplate)
            debug.set(extension.debug)
            stagedSources.convention(project.layout.buildDirectory.dir('edigen-staging/generateSources'))
        }
        def generation = project.tasks.register('generateSources', EdigenTask) {
            group = 'edigen'
            description = 'Publish generated sources from edigen specification'
            stagedSources.set(preparation.flatMap { it.stagedSources })
            decoderName.set(extension.decoderName)
            disassemblerName.set(extension.disassemblerName)
            decoderOutputDir.set(extension.decoderOutputDir)
            disassemblerOutputDir.set(extension.disassemblerOutputDir)
        }

        project.extensions.getByType(SourceSetContainer).named('main') {
            java.srcDir(project.files(generation.flatMap { it.decoderOutputDir }).builtBy(generation))
            java.srcDir(project.files(generation.flatMap { it.disassemblerOutputDir }).builtBy(generation))
        }
    }
}
