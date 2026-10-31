/* SPDX-FileCopyrightText: 2020-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import static net.emustudio.edigen.plugin.EdigenGenerationTask.sourcePath

/** Publishes valid staged sources without claiming unrelated output files. */
abstract class EdigenTask extends DefaultTask {
    private final String ownershipFile

    EdigenTask() {
        // Capture identity during configuration; execution never reads Project.
        String owner = project.rootProject.projectDir.canonicalPath + ':' + path
        ownershipFile = '.edigen-' + UUID.nameUUIDFromBytes(owner.getBytes(StandardCharsets.UTF_8)) + '-owned-sources'
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getStagedSources()

    @Input
    abstract Property<String> getDecoderName()

    @Input
    abstract Property<String> getDisassemblerName()

    @Internal
    abstract DirectoryProperty getDecoderOutputDir()

    @Internal
    abstract DirectoryProperty getDisassemblerOutputDir()

    @OutputFiles
    List<File> getGeneratedFiles() {
        String decoder = decoderName.get()
        String disassembler = disassemblerName.get()
        File decoderRoot = decoderOutputDir.get().asFile
        File disassemblerRoot = disassemblerOutputDir.get().asFile
        validateDistinctTargets(decoderRoot.toPath().resolve(sourcePath(decoder)),
                disassemblerRoot.toPath().resolve(sourcePath(disassembler)))
        // Check before Gradle's stale-output cleanup can claim an existing source.
        validateOwnership(decoderRoot.toPath(), sourcePath(decoder), ownedSources(decoderRoot.toPath(), ownershipFile))
        validateOwnership(disassemblerRoot.toPath(), sourcePath(disassembler), ownedSources(disassemblerRoot.toPath(), ownershipFile))
        return [
                new File(decoderRoot, sourcePath(decoder).toString()),
                new File(disassemblerRoot, sourcePath(disassembler).toString()),
                new File(decoderRoot, ownershipFile),
                new File(disassemblerRoot, ownershipFile)
        ].unique()
    }

    @TaskAction
    void publishEdigen() {
        Path decoderRoot = decoderOutputDir.get().asFile.toPath().toAbsolutePath().normalize()
        Path disassemblerRoot = disassemblerOutputDir.get().asFile.toPath().toAbsolutePath().normalize()
        Path decoderFile = sourcePath(decoderName.get())
        Path disassemblerFile = sourcePath(disassemblerName.get())
        validateDistinctTargets(decoderRoot.resolve(decoderFile), disassemblerRoot.resolve(disassemblerFile))

        Path staging = stagedSources.get().asFile.toPath()
        Map<Path, Map<Path, Path>> outputs = [:]
        outputs.computeIfAbsent(decoderRoot) { [:] }[decoderFile] = staging.resolve('decoder').resolve(decoderFile)
        outputs.computeIfAbsent(disassemblerRoot) { [:] }[disassemblerFile] = staging.resolve('disassembler').resolve(disassemblerFile)
        publishSources(outputs, ownershipFile)
    }

    private static void validateDistinctTargets(Path decoderTarget, Path disassemblerTarget) {
        if (decoderTarget.toFile().canonicalFile == disassemblerTarget.toFile().canonicalFile) {
            throw new GradleException('Decoder and disassembler must have distinct output files.')
        }
    }

    private static Set<Path> ownedSources(Path root, String ownershipFile) {
        Path manifest = root.resolve(ownershipFile)
        Set<Path> owned = [] as Set
        if (Files.exists(manifest)) {
            for (String line : Files.readAllLines(manifest, StandardCharsets.UTF_8)) {
                Path relative = Path.of(line).normalize()
                if (relative.isAbsolute() || relative.startsWith('..') || !line.endsWith('.java')) {
                    throw new GradleException("Invalid Edigen ownership entry in ${manifest}: ${line}")
                }
                owned.add(relative)
            }
        }
        return owned
    }

    private static void validateOwnership(Path root, Path relative, Set<Path> owned) {
        if (Files.exists(root.resolve(relative)) && !owned.contains(relative)) {
            throw new GradleException("Refusing to overwrite source not owned by Edigen: ${root.resolve(relative)}")
        }
    }

    private static void publishSources(Map<Path, Map<Path, Path>> outputs, String ownershipFile) {
        Map<Path, Set<Path>> owned = [:]
        // Validate every destination before changing any published source.
        outputs.each { root, sources ->
            owned[root] = ownedSources(root, ownershipFile)
            sources.each { relative, staged ->
                validateOwnership(root, relative, owned[root])
            }
        }
        outputs.each { root, sources ->
            sources.each { relative, staged ->
                Path target = root.resolve(relative)
                Files.createDirectories(target.parent)
                Files.copy(staged, target, StandardCopyOption.REPLACE_EXISTING)
            }
            for (Path previous : owned[root]) {
                if (!sources.containsKey(previous)) {
                    Files.deleteIfExists(root.resolve(previous))
                }
            }
            Files.write(root.resolve(ownershipFile), sources.keySet().collect { it.toString() }, StandardCharsets.UTF_8)
        }
    }
}
