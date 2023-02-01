// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

import com.intellij.ant.InstrumentationUtil.FormInstrumenter
import com.intellij.ant.PseudoClassLoader
import org.jetbrains.ether.ProjectWrapper
import org.jetbrains.ether.dependencyView.Callbacks.Backend

/**
 * @author max
 */
class ModuleBuildState {
  boolean iterated
  boolean tests
  Backend callback
  FormInstrumenter formInstrumenter
  PseudoClassLoader loader
  ProjectWrapper projectWrapper
  List<File> sourceFiles
  List<String> sourceRoots
  List<String> excludes
  List<String> classpath
  List<String> tempRootsToDelete = []
  List<String> sourceRootsFromModuleWithDependencies
  String targetFolder
  boolean incremental = false

  def print() {
    println "Sources: $sourceRoots"
    println "Excludes: $excludes"
    println "Classpath: $classpath"
  }
}
