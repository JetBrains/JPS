// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.incremental;

/**
 * @author Eugene Zhuravlev
 *         Date: 9/20/11
 */
public class ProjectBuildException extends Exception{

  public ProjectBuildException() {
  }

  public ProjectBuildException(String message) {
    super(message);
  }

  public ProjectBuildException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProjectBuildException(Throwable cause) {
    super(cause);
  }
}
