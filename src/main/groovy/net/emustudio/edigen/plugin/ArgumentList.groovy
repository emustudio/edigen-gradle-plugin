/*
 * This file is part of edigen-gradle-plugin.
 *
 * Copyright (C) 2020-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.edigen.plugin

/**
 * A command-line argument list for edigen.
 */
class ArgumentList {
    private List<String> arguments = new ArrayList<String>();

    void add(String argument) {
        arguments.add(argument);
    }

    String[] get() {
        return arguments.toArray(new String[0]);
    }

    /**
     * Adds the argument representing the template file to the list of
     * arguments.
     * @param argument the argument name (e.g. "-at")
     * @param template the template file name
     */
    void addTemplate(String argument, File template) {
        if (template != null) {
            add(argument);
            add(template.getPath());
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

        outputDirectory.mkdirs();

        add(argument);
        add(outputDirectory.getPath())
    }

    /**
     * Adds the flag to the argument list if the flag value is true.
     * @param argument the argument name (e.g. "-d")
     * @param flag the flag value
     */
    void addFlag(String argument, boolean flag) {
        if (flag)
            add(argument);
    }

    /**
     * Creates the directory name from the Java package + class name.
     * @param packageAndClass the package + class name
     * @return the directory of the package
     */
    private static String extractPackageDirectory(String packageAndClass) {
        int dotIndex = packageAndClass.lastIndexOf('.')
        return packageAndClass.substring(0, dotIndex).replace('.', File.separator)
    }
}

