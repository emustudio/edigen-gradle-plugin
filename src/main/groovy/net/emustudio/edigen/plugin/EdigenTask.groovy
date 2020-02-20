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
    File specification = project.extensions.specification

    @OutputDirectory
    @Optional
    File disassemblerOutputDir = project.extensions.disassemblerOutputDir

    @OutputDirectory
    @Optional
    File decoderOutputDir = project.extensions.decoderOutputDir


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
        if (!project.extensions.decoderName.contains("."))
            throw new GradleException("Decoder name must include a package.");

        if (!project.extensions.disassemblerName.contains("."))
            throw new GradleException("Disassembler name must include a package.");
    }

    private void addArguments(ArgumentList arguments) {
        arguments.add(specification.getPath())
        arguments.add(project.extensions.decoderName)
        arguments.add(project.extensions.disassemblerName)

        arguments.addOutputDirectory("-ao", disassemblerOutputDir, project.extensions.disassemblerName);
        arguments.addTemplate("-at", project.extensions.disassemblerTemplate);

        arguments.addFlag("-d", project.extensions.debug);

        arguments.addOutputDirectory("-do", decoderOutputDir, project.extensions.decoderName);
        arguments.addTemplate("-dt", project.extensions.decoderTemplate);
    }
}
