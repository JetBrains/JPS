// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.listeners

import org.jetbrains.jps.ProjectBuilder

/**
 * @author nik
 */
class DefaultBuildInfoPrinter implements BuildInfoPrinter {
  def printProgressMessage(ProjectBuilder projectBuilder, String message) {
    projectBuilder.info(message)
  }

  def printCompilationErrors(ProjectBuilder projectBuilder, String compilerName, String messages) {
    projectBuilder.error(messages)
  }

}
