/* SPDX-FileCopyrightText: 2020-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.plugin

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/** Inputs and generated-source locations for the main instruction set. */
abstract class EdigenPluginExtension {
    abstract RegularFileProperty getSpecification()
    abstract Property<String> getDecoderName()
    abstract RegularFileProperty getDecoderTemplate()
    abstract DirectoryProperty getDecoderOutputDir()
    abstract Property<Boolean> getDebug()
}
