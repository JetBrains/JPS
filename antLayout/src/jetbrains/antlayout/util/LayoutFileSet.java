// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package jetbrains.antlayout.util;

import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.FileSet;

/**
 * @author max
 */
public class LayoutFileSet extends ZipFileSet {
    public LayoutFileSet() {
    }

    public LayoutFileSet(FileSet fileset) {
        super(fileset);
    }

    public LayoutFileSet(ZipFileSet fileset) {
        super(fileset);
    }
}
