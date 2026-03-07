/* SPDX-FileCopyrightText: 2020-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.plugin

/**
 * A command-line argument list for edigen.
 */
class ArgumentList {
    private final List<String> arguments = []

    void add(String argument) {
        arguments.add(argument);
    }

    void addAll(String... args) {
        arguments.addAll(args as List<String>)
    }

    String[] get() {
        return arguments.toArray(new String[0])
    }

    /**
     * Adds the argument representing the template file to the list of
     * arguments.
     * @param argument the argument name (e.g. "-at")
     * @param template the template file name
     */
    void addTemplate(String argument, File template) {
        if (template != null) {
            addAll(argument, template.path)
        }
    }

    /**
     * Adds the argument representing the output directory to the list of
     * arguments.
     * @param argument the argument name (e.g. "-ao")
     * @param directory the directory supplied by a user in the configuration
     *        file (can be null)
     * @param packageAndClass the package + class name of the generated source
     *        code
     */
    void addOutputDirectory(String argument, File directory, String packageAndClass) {
        def packageDirectory = extractPackageDirectory(packageAndClass)
        def outputDirectory = directory.toPath().resolve(packageDirectory).toFile()

        outputDirectory.mkdirs()

        addAll(argument, outputDirectory.path)
    }

    /**
     * Adds the flag to the argument list if the flag value is true.
     * @param argument the argument name (e.g. "-d")
     * @param flag the flag value
     */
    void addFlag(String argument, boolean flag) {
        if (flag) {
            add(argument)
        }
    }

    /**
     * Creates the directory name from the Java package + class name.
     * @param packageAndClass the package + class name
     * @return the directory of the package
     */
    private static String extractPackageDirectory(String packageAndClass) {
        int dotIndex = packageAndClass.lastIndexOf('.')
        return packageAndClass.substring(0, dotIndex).replace('.', File.separatorChar)
    }
}
