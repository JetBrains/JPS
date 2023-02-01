// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

import org.jetbrains.jps.artifacts.Artifact

/**
 * @author max
 */
class Project {
  final Map<String, Library> globalLibraries = [:]
  final Map<String, Sdk> sdks = [:]

  Sdk projectSdk;
  final Map<String, Module> modules = [:]
  final Map<String, Library> libraries = [:]
  final Map<String, Artifact> artifacts = [:]
  final Map<String, RunConfiguration> runConfigurations = [:]
  final CompilerConfiguration compilerConfiguration = new CompilerConfiguration()

  String projectCharset; // contains project charset, if not specified default charset will be used (used by compilers)

  def Project() {
  }

  def Module createModule(String name, Closure initializer) {
    def module = new Module(this, name, initializer)
    modules.put(name, module)
    module
  }

  def Library createLibrary(String name, Closure initializer) {
    createLibrary(name, initializer, libraries, "project.library")
  }

  def Library createGlobalLibrary(String name, Closure initializer) {
    createLibrary(name, initializer, globalLibraries, "project.globalLibrary")
  }

  protected def Library createLibrary(String name, Closure initializer, Map<String, Library> libraries, String accessor) {
    Library lib = new Library(this, name, initializer)
    libraries.put(name, lib)
    lib
  }

  def JavaSdk createJavaSdk(String name, String path, Closure initializer) {
    def sdk = new JavaSdk(this, name, path, initializer)
    sdks[name] = sdk
    return sdk
  }

  def String toString() {
    return "Project with ${modules.size()} modules and ${libraries.size()} libraries"
  }

  def ClasspathItem resolve(Object dep) {
    assert dep instanceof ClasspathItem : dep
    return dep
  }
}
