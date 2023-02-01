// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

import org.jetbrains.jps.dag.DagBuilder

/**
 * @author nik
 */
class ProjectChunks {
  private List<ModuleChunk> chunks = null
  private final Map<Module, ModuleChunk> mapping = [:]
  private final ClasspathKind classpathKind
  private final Project project

  ProjectChunks(Project project, ClasspathKind classpathKind) {
    this.classpathKind = classpathKind
    this.project = project
  }

  List<ModuleChunk> getChunkList() {
    initializeChunks()
    return chunks
  }

  private def initializeChunks() {
    if (chunks != null) {
      return
    }

    def iterator = { Module module, Closure processor ->
      module.getClasspath(classpathKind).each {entry ->
        if (entry instanceof Module) {
          processor(entry)
        }
      }
    }
    def dagBuilder = new DagBuilder<Module>({new ModuleChunk()}, iterator)
    chunks = dagBuilder.build(project, project.modules.values())
    chunks.each { ModuleChunk chunk ->
      chunk.modules.each {
        mapping[it] = chunk
      }
    }
  }

  ModuleChunk findChunk(Module module) {
    initializeChunks()
    return mapping[module]
  }
}
