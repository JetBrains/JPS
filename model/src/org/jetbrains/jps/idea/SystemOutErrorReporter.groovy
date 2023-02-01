// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.idea

/**
 * @author nik
 */
class CannotLoadProjectException extends RuntimeException {
  CannotLoadProjectException(String message) {
    super(message)
  }
}

class SystemOutErrorReporter implements ProjectLoadingErrorReporter {
  void error(String message) {
    throw new CannotLoadProjectException(message)
  }

  void warning(String message) {
    println("warn: " + message)
  }

  void info(String message) {
    println("info: " + message)
  }
}
