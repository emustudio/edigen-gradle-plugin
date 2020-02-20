/*
 * Edigen Gradle Plugin.
 *
 *     Copyright (C) 2020  Peter Jakubčo
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

import com.github.sulir.edigen.Edigen
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional

class EdigenTask extends DefaultTask {

    @InputFile
    @Optional
    File specification = project.file('src/main/edigen/cpu.eds')

    /**
     * Instruction decoder package + class name.
     */
    @Input
    String decoderName

    /**
     * Instruction decoder template file.
     */
    @InputFile
    @Optional
    File decoderTemplate

    /**
     * Disassembler package + class name.
     */
    @Input
    String disassemblerName

    /**
     * Disassembler template file.
     */
    @InputFile
    @Optional
    File disassemblerTemplate

    /**
     * Disassembler output directory.
     *
     * The default is "$project.buildDir/generated-sources/edigen"
     */
    @OutputDirectory
    @Optional
    File disassemblerOutputDir = project.file("$project.buildDir/generated-sources/edigen")

    /**
     * Instruction decoder output directory.
     *
     * The default is "$project.buildDir/generated-sources/edigen"
     */
    @OutputDirectory
    @Optional
    File decoderOutputDir = project.file("$project.buildDir/generated-sources/edigen")

    /**
     * Debug mode (display tree transformations).
     */
    @Input
    @Optional
    boolean debug = false



    @TaskAction
    def generateEdigen() {
        project.delete(disassemblerOutputDir)
        project.delete(decoderOutputDir)

        project.mkdir(disassemblerOutputDir)
        try {
            project.mkdir(decoderOutputDir)
        } catch(Exception ignored) {}

        validate()

        def arguments = new ArgumentList()
        addArguments(arguments)

        try {
            new Edigen().run(arguments.get());
        } catch (Exception ex) {
            throw new GradleException(ex.getMessage(), ex);
        }
    }

    private void validate() throws GradleException {
        if (!decoderName.contains("."))
            throw new GradleException("Decoder name must include a package.");

        if (!disassemblerName.contains("."))
            throw new GradleException("Disassembler name must include a package.");
    }

    private void addArguments(ArgumentList arguments) {
        arguments.add(specification.getPath())
        arguments.add(decoderName)
        arguments.add(disassemblerName)

        arguments.addOutputDirectory("-ao", disassemblerOutputDir, disassemblerName);
        arguments.addTemplate("-at", disassemblerTemplate);

        arguments.addFlag("-d", debug);

        arguments.addOutputDirectory("-do", decoderOutputDir, decoderName);
        arguments.addTemplate("-dt", decoderTemplate);
    }
}
