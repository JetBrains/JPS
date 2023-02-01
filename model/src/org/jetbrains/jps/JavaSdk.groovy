// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

/**
 * @author nik
 */
class JavaSdk extends Sdk {
  String jdkPath

  def JavaSdk(Project project, String name, String jdkPath, Closure initializer) {
    super(project, name, initializer)
    this.jdkPath = jdkPath
  }

  String getJavacExecutable() {
    return jdkPath + File.separator + "bin" + File.separator + "javac";
  }

  String getJavaExecutable() {
    return jdkPath + File.separator + "bin" + File.separator + "java";
  }
}
