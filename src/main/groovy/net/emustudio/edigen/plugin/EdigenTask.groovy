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

import net.emustudio.edigen.Edigen
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional

class EdigenTask extends DefaultTask {

    @TaskAction
    def generateEdigen() {
        def edigen = project.extensions.getByType(EdigenPluginExtension)

        project.delete(edigen.disassemblerOutputDir)
        project.delete(edigen.decoderOutputDir)

        project.mkdir(edigen.disassemblerOutputDir)
        try {
            project.mkdir(edigen.decoderOutputDir)
        } catch(Exception ignored) {}

        validate(edigen)

        def arguments = new ArgumentList()
        addArguments(arguments, edigen)

        try {
            new Edigen().run(arguments.get());
        } catch (Exception ex) {
            throw new GradleException(ex.getMessage(), ex);
        }
    }

    private static void validate(EdigenPluginExtension edigen) throws GradleException {
        Objects.requireNonNull(edigen.decoderName, "'edigen.decoderName' must be defined")
        Objects.requireNonNull(edigen.disassemblerName, "'edigen.disassemblerName' name must be defined")

        if (!edigen.decoderName.contains("."))
            throw new GradleException("Decoder name must include a package.");

        if (!edigen.disassemblerName.contains("."))
            throw new GradleException("Disassembler name must include a package.");
    }

    private static void addArguments(ArgumentList arguments, EdigenPluginExtension edigen) {
        arguments.add(edigen.specification.getPath())
        arguments.add(edigen.decoderName)
        arguments.add(edigen.disassemblerName)

        arguments.addOutputDirectory("-ao", edigen.disassemblerOutputDir, edigen.disassemblerName);
        arguments.addTemplate("-at", edigen.disassemblerTemplate);

        arguments.addFlag("-d", edigen.debug);

        arguments.addOutputDirectory("-do", edigen.decoderOutputDir, edigen.decoderName);
        arguments.addTemplate("-dt", edigen.decoderTemplate);
    }
}
