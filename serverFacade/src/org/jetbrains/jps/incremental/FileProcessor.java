// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.incremental;

import org.jetbrains.jps.Module;

import java.io.File;

/**
* @author Eugene Zhuravlev
*         Date: 9/21/11
*/
public interface FileProcessor {
  /**
   * @return true if processing should continue, false if should stop
   */
  boolean apply(Module module, File file, String sourceRoot) throws Exception;
}
