// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.idea;

/**
 * @author nik
 */
public interface ProjectLoadingErrorReporter {
  void error(String message);

  void warning(String message);

  void info(String message);
}
