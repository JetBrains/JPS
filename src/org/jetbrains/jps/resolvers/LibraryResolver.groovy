// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.resolvers

import org.jetbrains.jps.ClasspathItem
import org.jetbrains.jps.Project

/**
 * @author max
 */

class LibraryResolver implements Resolver {
  private static def GLOBAL_LIB_PREFIX = "globalLib:"
  private static def PROJECT_LIB_PREFIX = "lib:"
  Project project;

  def ClasspathItem resolve(String classpathItem) {
    if (classpathItem.startsWith(GLOBAL_LIB_PREFIX)) {
      return project.globalLibraries[classpathItem.substring(GLOBAL_LIB_PREFIX.length())]
    }
    if (classpathItem.startsWith(PROJECT_LIB_PREFIX)) {
      classpathItem = classpathItem.substring(PROJECT_LIB_PREFIX.length())
    }

    return project.libraries[classpathItem]
  }
}
