// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.listeners

import org.jetbrains.jps.ModuleBuilder
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.ProjectBuilder

/**
 * @author nik
 */
interface JpsBuildListener {
  
  def onBuildStarted(ProjectBuilder projectBuilder)
  def onBuildFinished(ProjectBuilder projectBuilder)

  def onCompilationStarted(ModuleChunk moduleChunk)
  def onCompilationFinished(ModuleChunk moduleChunk)

  def onModuleBuilderStarted(ModuleBuilder builder, ModuleChunk chunk)
  def onModuleBuilderFinished(ModuleBuilder builder, ModuleChunk chunk)

  def onJavaFilesCompiled(ModuleChunk moduleChunk, int filesCount)
}
