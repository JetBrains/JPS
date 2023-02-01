// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.builders

import org.jetbrains.jps.ModuleBuildState
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.ProjectBuilder

/**
 * @author nik
 */
public interface ModuleCycleBuilder {
  def preprocessModuleCycle(ModuleBuildState state, ModuleChunk moduleChunk, ProjectBuilder projectBuilder)
}