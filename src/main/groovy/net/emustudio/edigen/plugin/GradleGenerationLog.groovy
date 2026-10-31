/* SPDX-FileCopyrightText: 2020-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.plugin

import net.emustudio.edigen.GenerationLog
import org.gradle.api.logging.Logger

import java.util.function.Supplier

/** Routes one generation invocation through its task logger. */
final class GradleGenerationLog implements GenerationLog {
    private final Logger logger
    private final boolean debugRequested

    GradleGenerationLog(Logger logger, boolean debugRequested) {
        this.logger = logger
        this.debugRequested = debugRequested
    }

    @Override
    void info(String message) {
        logger.lifecycle(message)
    }

    @Override
    void warn(String message) {
        logger.warn(message)
    }

    @Override
    void debug(Supplier<String> message) {
        if (debugRequested) {
            if (logger.isLifecycleEnabled()) {
                logger.lifecycle(message.get())
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug(message.get())
        }
    }
}
