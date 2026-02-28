/* SPDX-FileCopyrightText: 2020-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class EdigenPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.with {
            extensions.create('edigen', EdigenPluginExtension, project)

            apply plugin: 'java'
            tasks.create(name: 'generateSources', type: EdigenTask, {
                group = "edigen"
                description = "Generate sources from edigen specification"
            })

            tasks.compileJava.dependsOn tasks.generateSources

            afterEvaluate {
                EdigenPluginExtension ext = extensions.getByType(EdigenPluginExtension)
                sourceSets.main.java.srcDirs += [
                        ext.disassemblerOutputDir, ext.decoderOutputDir
                ]
            }
        }
    }
}
