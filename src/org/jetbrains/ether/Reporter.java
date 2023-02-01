// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.ether;

import org.jetbrains.jps.Module;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: 03.12.10
 * Time: 19:39
 * To change this template use File | Settings | File Templates.
 */
public class Reporter {
    public static final String myOkFlag = ".jps.ok";
    public static final String myFailFlag = ".jps.fail";

    private static String getSafePath (final String path) {
        final File f = new File(path);

        if (! f.exists()) {
            f.mkdir();
        }

        return path;
    }

    private static String getOkFlag(final Module m) {
        final String outputPath = m.getOutputPath();

        if (outputPath == null)
            return null;

        return getSafePath (outputPath) + File.separator + myOkFlag;
    }

    private static String getFailFlag(final Module m) {
        final String outputPath = m.getOutputPath();

        if (outputPath == null)
            return null;

        return getSafePath (outputPath) + File.separator + myFailFlag;
    }

    private static String getOkTestFlag(final Module m) {
        final String testOutputPath = m.getTestOutputPath();

        if (testOutputPath == null)
            return null;

        return getSafePath (testOutputPath) + File.separator + myOkFlag;
    }

    private static String getFailTestFlag(final Module m) {
        final String testOutputPath = m.getTestOutputPath();

        if (testOutputPath == null) {
            return null;
        }

        return getSafePath(testOutputPath) + File.separator + myFailFlag;
    }

    private static void write(final String name, final String contents) {
        if (name == null)
            return;

        try {
            final File file = new File (name);
            final String path = file.getParent();

            new File (path).mkdirs();

            final FileWriter writer = new FileWriter(name);

            writer.write(contents);
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reportBuildSuccess(final Module m, final boolean tests) {
        if (tests) {
            write(getOkTestFlag(m), "dummy");
        }
        else {
            write(getOkFlag(m), "dummy");
        }
    }

    public static void reportBuildFailure(final Module m, final boolean tests, final String reason) {
        if (tests) {
            write(getFailTestFlag(m), reason);
        }
        else {
            write(getFailFlag(m), reason);
        }
    }
}
