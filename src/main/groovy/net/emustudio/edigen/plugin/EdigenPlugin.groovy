/*
 * This file is part of edigen-gradle-plugin.
 *
 * Copyright (C) 2020-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
