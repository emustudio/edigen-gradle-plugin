/* SPDX-FileCopyrightText: 2020-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.plugin

import net.emustudio.edigen.Edigen
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import javax.lang.model.SourceVersion
import java.nio.file.Path

/** Generates into private staging; publication cannot start if this task fails. */
abstract class EdigenGenerationTask extends DefaultTask {
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getSpecification()

    @Input
    abstract Property<String> getDecoderName()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getDecoderTemplate()

    @Input
    abstract Property<String> getDisassemblerName()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getDisassemblerTemplate()

    @Input
    abstract Property<Boolean> getDebug()

    @OutputDirectory
    abstract DirectoryProperty getStagedSources()

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations()

    @TaskAction
    void generateEdigen() {
        String decoder = decoderName.get()
        String disassembler = disassemblerName.get()
        sourcePath(decoder)
        sourcePath(disassembler)

        File staging = stagedSources.get().asFile
        fileSystemOperations.delete { delete(staging) }
        def arguments = new ArgumentList()
        arguments.addAll(specification.get().asFile.path, decoder, disassembler)
        arguments.addOutputDirectory('-do', new File(staging, 'decoder'), decoder)
        arguments.addOutputDirectory('-ao', new File(staging, 'disassembler'), disassembler)
        arguments.addTemplate('-dt', decoderTemplate.orNull?.asFile)
        arguments.addTemplate('-at', disassemblerTemplate.orNull?.asFile)
        arguments.addFlag('-d', debug.get())

        try {
            new Edigen().run(arguments.get())
        } catch (Exception ex) {
            throw new GradleException(ex.message, ex)
        }
    }

    static Path sourcePath(String className) {
        if (!className.contains('.') || !SourceVersion.isName(className)) {
            throw new GradleException('Generated class name must be a fully qualified Java class name.')
        }
        return Path.of(className.replace('.', '/') + '.java')
    }
}
