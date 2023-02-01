// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.resolvers

import org.jetbrains.jps.ClasspathItem
import org.jetbrains.jps.Project

/**
 * @author max
 */
class ModuleResolver implements Resolver {
  Project project;

  def ClasspathItem resolve(String classpathItem) {
    if (classpathItem.startsWith("module:")) classpathItem = classpathItem.substring("module:".length())

    return project.modules[classpathItem]
  }

}
