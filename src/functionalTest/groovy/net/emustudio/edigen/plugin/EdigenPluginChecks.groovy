/* SPDX-FileCopyrightText: 2020-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.plugin

import net.emustudio.edigen.Edigen
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files

class EdigenPluginChecks {
    private File temporaryDirectory

    static void main(String[] args) {
        List<String> checks = [
                'unchangedInputsAreUpToDateAndSpecificationEditsRegenerate',
                'bothTemplateEditsInvalidateGeneration',
                'renameRemovesOnlyPreviouslyOwnedSource',
                'invalidSpecificationPreservesPublishedSourcesAndUnrelatedFiles',
                'unownedCustomTargetSourceIsNeverOverwritten',
                'unownedBuildTargetSourceIsNeverDeletedByGradleCleanup',
                'firstGenerationPreservesUnrelatedSourcesInOutputDirectory',
                'renameWithInvalidSpecificationPreservesPreviousOwnedSource',
                'generationSupportsConfigurationCache',
                'twoProjectsSharingOutputDirectoryPreserveEachOthersSources',
                'secondProjectCannotClaimFirstProjectsSource',
                'decoderOutputOverrideKeepsIndependentDisassemblerDefault',
                'invalidSpecificationWithColdHistoryPreservesPublishedSources',
                'collidingTargetsWithColdHistoryPreservePublishedSources'
        ]
        for (String check : checks) {
            def fixture = new EdigenPluginChecks()
            fixture.setUp()
            try {
                fixture.invokeMethod(check, null)
                println("PASS ${check}")
            } finally {
                fixture.temporaryDirectory.deleteDir()
            }
        }
        println("${checks.size()} generation checks passed.")
    }

    private static void assertEquals(Object expected, Object actual) {
        assert expected == actual
    }

    private static void assertNotEquals(Object first, Object second) {
        assert first != second
    }

    private static void assertTrue(boolean value) {
        assert value
    }

    private static void assertFalse(boolean value) {
        assert !value
    }

    private File projectDir
    private File specification
    private File outputDir

    void setUp() {
        temporaryDirectory = Files.createTempDirectory('edigen-plugin-check-').toFile()
        projectDir = new File(temporaryDirectory, 'consumer')
        projectDir.mkdirs()
        new File(projectDir, 'settings.gradle').text = "rootProject.name = 'consumer'\n"
        new File(projectDir, 'build.gradle').text = '''
plugins { id 'net.emustudio.edigen-gradle-plugin' }
edigen {
    decoderName = 'sample.DecoderImpl'
    disassemblerName = 'sample.DisassemblerImpl'
}
'''
        specification = new File(projectDir, 'src/main/edigen/cpu.eds')
        specification.parentFile.mkdirs()
        specification.text = spec('nop')
        outputDir = new File(projectDir, 'build/generated-sources/edigen')
    }

    private static String spec(String mnemonic) {
        return "root instruction;\ninstruction = \"${mnemonic}\": 0x00;\n%%\n\"%s\" = instruction;\n"
    }

    private GradleRunner runner(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments((arguments as List) + ['--stacktrace', '--offline'])
    }

    private File generated(String className) {
        return new File(outputDir, "sample/${className}.java")
    }

    void unchangedInputsAreUpToDateAndSpecificationEditsRegenerate() {
        assertEquals(TaskOutcome.SUCCESS, runner('generateSources').build().task(':generateSources').outcome)
        String previous = generated('DecoderImpl').text
        assertEquals(TaskOutcome.UP_TO_DATE, runner('generateSources').build().task(':generateSources').outcome)
        specification.text = spec('halt')
        assertEquals(TaskOutcome.SUCCESS, runner('generateSources').build().task(':generateSources').outcome)
        assertNotEquals(previous, generated('DecoderImpl').text)
    }

    void bothTemplateEditsInvalidateGeneration() {
        File decoderTemplate = new File(projectDir, 'Decoder.edt')
        File disassemblerTemplate = new File(projectDir, 'Disassembler.edt')
        decoderTemplate.text = Edigen.getResource('/Decoder.edt').text
        disassemblerTemplate.text = Edigen.getResource('/Disassembler.edt').text
        new File(projectDir, 'build.gradle') << '''
edigen {
    decoderTemplate = file('Decoder.edt')
    disassemblerTemplate = file('Disassembler.edt')
}
'''
        runner('generateSources').build()
        decoderTemplate << '\n// decoder template changed\n'
        assertEquals(TaskOutcome.SUCCESS, runner('generateSources').build().task(':generateSources').outcome)
        assertTrue(generated('DecoderImpl').text.contains('// decoder template changed'))
        disassemblerTemplate << '\n// disassembler template changed\n'
        assertEquals(TaskOutcome.SUCCESS, runner('generateSources').build().task(':generateSources').outcome)
        assertTrue(generated('DisassemblerImpl').text.contains('// disassembler template changed'))
    }

    void renameRemovesOnlyPreviouslyOwnedSource() {
        runner('generateSources').build()
        File unrelated = new File(outputDir, 'sample/HandWritten.java')
        unrelated.text = 'package sample; class HandWritten {}\n'
        File build = new File(projectDir, 'build.gradle')
        build.text = build.text.replace('sample.DecoderImpl', 'sample.RenamedDecoder')
        runner('generateSources').build()
        assertTrue(generated('RenamedDecoder').isFile())
        assertFalse(generated('DecoderImpl').exists())
        assertEquals('package sample; class HandWritten {}\n', unrelated.text)
    }

    void invalidSpecificationPreservesPublishedSourcesAndUnrelatedFiles() {
        runner('generateSources').build()
        String decoder = generated('DecoderImpl').text
        String disassembler = generated('DisassemblerImpl').text
        File manifest = outputDir.listFiles().find { it.name.endsWith('-owned-sources') }
        String ownership = manifest.text
        File unrelated = new File(outputDir, 'keep.txt')
        unrelated.text = 'keep me'
        specification.text = 'this is not an instruction set'
        runner('generateSources').buildAndFail()
        assertEquals(decoder, generated('DecoderImpl').text)
        assertEquals(disassembler, generated('DisassemblerImpl').text)
        assertEquals(ownership, manifest.text)
        assertEquals('keep me', unrelated.text)
    }

    void unownedCustomTargetSourceIsNeverOverwritten() {
        outputDir = new File(projectDir, 'generated')
        new File(projectDir, 'build.gradle') << "\nedigen { decoderOutputDir = file('generated'); disassemblerOutputDir = file('generated') }\n"
        File existing = generated('DecoderImpl')
        existing.parentFile.mkdirs()
        existing.text = 'hand written source'
        def result = runner('generateSources').buildAndFail()
        assertTrue(result.output.contains('Refusing to overwrite source not owned by Edigen'))
        assertEquals('hand written source', existing.text)
        assertFalse(generated('DisassemblerImpl').exists())
    }

    void unownedBuildTargetSourceIsNeverDeletedByGradleCleanup() {
        File existing = generated('DecoderImpl')
        existing.parentFile.mkdirs()
        existing.text = 'hand written source'
        def result = runner('generateSources').buildAndFail()
        assertTrue(result.output.contains('Refusing to overwrite source not owned by Edigen'))
        assertEquals('hand written source', existing.text)
    }

    void firstGenerationPreservesUnrelatedSourcesInOutputDirectory() {
        File unrelated = new File(outputDir, 'sample/HandWritten.java')
        unrelated.parentFile.mkdirs()
        unrelated.text = 'package sample; class HandWritten {}\n'
        runner('generateSources').build()
        assertEquals('package sample; class HandWritten {}\n', unrelated.text)
    }

    void renameWithInvalidSpecificationPreservesPreviousOwnedSource() {
        runner('generateSources').build()
        String previous = generated('DecoderImpl').text
        File build = new File(projectDir, 'build.gradle')
        build.text = build.text.replace('sample.DecoderImpl', 'sample.RenamedDecoder')
        specification.text = 'invalid specification'
        runner('generateSources').buildAndFail()
        assertEquals(previous, generated('DecoderImpl').text)
        assertFalse(generated('RenamedDecoder').exists())
    }

    private void addSecondProject(String decoderName = 'second.DecoderImpl') {
        new File(projectDir, 'settings.gradle') << "include 'second'\n"
        File secondProject = new File(projectDir, 'second')
        secondProject.mkdirs()
        new File(secondProject, 'build.gradle').text = """
plugins { id 'net.emustudio.edigen-gradle-plugin' }
edigen {
    specification = file('../src/main/edigen/cpu.eds')
    decoderName = '${decoderName}'
    disassemblerName = 'second.DisassemblerImpl'
    decoderOutputDir = file('../build/generated-sources/edigen')
    disassemblerOutputDir = file('../build/generated-sources/edigen')
}
"""
    }

    void twoProjectsSharingOutputDirectoryPreserveEachOthersSources() {
        addSecondProject()
        runner(':generateSources', ':second:generateSources').build()
        assertTrue(generated('DecoderImpl').isFile())
        File secondDecoder = new File(outputDir, 'second/DecoderImpl.java')
        String secondSource = secondDecoder.text
        File build = new File(projectDir, 'build.gradle')
        build.text = build.text.replace('sample.DecoderImpl', 'sample.RenamedDecoder')
        runner(':generateSources').build()
        assertTrue(generated('RenamedDecoder').isFile())
        assertFalse(generated('DecoderImpl').exists())
        assertEquals(secondSource, secondDecoder.text)
        assertEquals(2, outputDir.listFiles().count { it.name.endsWith('-owned-sources') })
    }

    void secondProjectCannotClaimFirstProjectsSource() {
        runner(':generateSources').build()
        String firstSource = generated('DecoderImpl').text
        addSecondProject('sample.DecoderImpl')
        def result = runner(':second:generateSources').buildAndFail()
        assertTrue(result.output.contains('Refusing to overwrite source not owned by Edigen'))
        assertEquals(firstSource, generated('DecoderImpl').text)
    }

    void decoderOutputOverrideKeepsIndependentDisassemblerDefault() {
        new File(projectDir, 'build.gradle') << "\nedigen { decoderOutputDir = file('build/decoder') }\n"
        runner('generateSources').build()
        assertTrue(new File(projectDir, 'build/decoder/sample/DecoderImpl.java').isFile())
        assertTrue(generated('DisassemblerImpl').isFile())
        assertFalse(generated('DecoderImpl').exists())
    }

    void invalidSpecificationWithColdHistoryPreservesPublishedSources() {
        runner('generateSources').build()
        String decoder = generated('DecoderImpl').text
        String disassembler = generated('DisassemblerImpl').text
        File manifest = outputDir.listFiles().find { it.name.endsWith('-owned-sources') }
        String ownership = manifest.text
        assertTrue(new File(projectDir, '.gradle').deleteDir())
        specification.text = 'invalid specification'
        runner('generateSources').buildAndFail()
        assertEquals(decoder, generated('DecoderImpl').text)
        assertEquals(disassembler, generated('DisassemblerImpl').text)
        assertEquals(ownership, manifest.text)
    }

    void collidingTargetsWithColdHistoryPreservePublishedSources() {
        runner('generateSources').build()
        String decoder = generated('DecoderImpl').text
        String disassembler = generated('DisassemblerImpl').text
        File manifest = outputDir.listFiles().find { it.name.endsWith('-owned-sources') }
        String ownership = manifest.text
        assertTrue(new File(projectDir, '.gradle').deleteDir())
        new File(projectDir, 'build.gradle') << '''
edigen {
    disassemblerName = 'sample.DecoderImpl'
    disassemblerOutputDir = file('build/generated-sources/edigen/../edigen')
}
'''
        def result = runner('generateSources').buildAndFail()
        assertTrue(result.output.contains('Decoder and disassembler must have distinct output files.'))
        assertEquals(decoder, generated('DecoderImpl').text)
        assertEquals(disassembler, generated('DisassemblerImpl').text)
        assertEquals(ownership, manifest.text)
    }

    void generationSupportsConfigurationCache() {
        runner('generateSources', '--configuration-cache').build()
        def result = runner('generateSources', '--configuration-cache').build()
        assertTrue(result.output.contains('Reusing configuration cache.'))
        assertEquals(TaskOutcome.UP_TO_DATE, result.task(':generateSources').outcome)
    }
}
