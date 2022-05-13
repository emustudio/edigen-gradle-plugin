/*
 * Edigen Gradle Plugin.
 *
 *     Copyright (C) 2020-2022  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
