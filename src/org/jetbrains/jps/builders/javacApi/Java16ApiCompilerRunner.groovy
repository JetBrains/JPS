// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.builders.javacApi

import org.jetbrains.jps.ModuleBuildState
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.ProjectBuilder

/**
 * @author nik
 */
class Java16ApiCompilerRunner {
  private static boolean notAvailable

  static boolean compile(ModuleChunk chunk, ProjectBuilder projectBuilder, ModuleBuildState state, String sourceLevel, String targetLevel, String customArgs) {
    if (notAvailable) {
      return false
    }

    try {
      Java16ApiCompiler compiler = Java16ApiCompiler.getInstance()
      compiler.compile(chunk, projectBuilder, state, sourceLevel, targetLevel, customArgs)
      return true
    }
    catch (NoClassDefFoundError error) {
      projectBuilder.warning("Java 1.6 API compiler is not available")
      notAvailable = true
    }
    catch (Exception e) {
      e.printStackTrace()
      projectBuilder.warning("Compilation failed with exception for '${chunk.name}'")
      throw e
    }
    return false
  }
}
