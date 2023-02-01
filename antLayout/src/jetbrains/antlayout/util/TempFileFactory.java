// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package jetbrains.antlayout.util;

import java.io.File;

/**
 * @author max
 */
public interface TempFileFactory {
    File allocateTempFile(String name);
}
