// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

import org.codehaus.gant.GantBinding

/**
 * @author max
 */
final class Jps {

  def Jps(GantBinding binding) {
    GantBasedProject project = new GantBasedProject(binding)
    ProjectBuilder projectBuilder = project.builder
    binding.setVariable("project", project)
    binding.setVariable("projectBuilder", projectBuilder)
    binding.setVariable("module", {String name, Closure initializer ->
      return project.createModule(name, initializer)
    })

    binding.setVariable("library", {String name, Closure initializer ->
      return project.createLibrary(name, initializer)
    })

    binding.setVariable("globalLibrary", {String name, Closure initializer ->
      return project.createGlobalLibrary(name, initializer)
    })

    binding.setVariable("jdk", {Object[] args ->
      if (!(args.length in [2,3])) {
        projectBuilder.error("expected 2 to 3 parameters for jdk() but ${args.length} found")
      }
      Closure initializer = args.length > 2 ? args[2] : {}
      return project.createJavaSdk((String)args[0], (String)args[1], initializer)
    })

    binding.setVariable("moduleTests", {String name ->
      def module = project.modules[name]
      if (module == null) projectBuilder.error("cannot find module ${name}")
      return projectBuilder.moduleTestsOutput(module)
    })

    binding.setVariable("layout", {String dir, Closure body ->
      def old = binding.getVariable("module")
      def layoutInfo = new LayoutInfo()

      ["module", "moduleTests", "zip", "dir"].each {tag ->
        binding.setVariable(tag, {Object[] args ->
          if (args.length == 1) {
            binding.ant."$tag"(name: args[0])
          }
          else if (args.length == 2) {
            binding.ant."$tag"(name: args[0], args[1])
          }
          else {
            projectBuilder.error("unexpected number of parameters for $tag")
          }
          if (tag == "module") {
            layoutInfo.usedModules << args[0].toString()
          }
        })
      }
      binding.setVariable("jar", {Object[] args ->
        if (args.length == 2) {
          def param0 = args[0]
          String name;
          String duplicate = null;
          if (param0 instanceof Map) {
            name = param0.name;
            duplicate = param0.duplicate;
          }
          else {
            name = (String)param0;
          }
          if (duplicate != null) {
            binding.ant.jar(name: name, compress: projectBuilder.compressJars, duplicate: duplicate, args[1])
          }
          else {
            binding.ant.jar(name: name, compress: projectBuilder.compressJars, args[1])
          }
        }
        else {
          projectBuilder.error("unexpected number of parameters for 'jar' task: $args.length")
        }
      })

      binding.setVariable("renamedFile", {Object[] args ->
        if (args.length != 2) {
          projectBuilder.error("unexpected number of parameters for renamedFile")
        }
        binding.ant."renamedFile"(filePath: args[0], newName: args[1])
      })
      binding.setVariable("extractedDir", {Object[] args ->
        if (args.length != 2) {
          projectBuilder.error("unexpected number of parameters for extractedDir")
        }
        binding.ant."extractedDir"(jarPath: args[0], pathInJar: args[1])
      })

      try {
        def meta = new Expando()
        body.delegate = meta
        binding.ant.layout(toDir: dir, body)
      } finally {
        binding.setVariable("module", old)
      }
      return layoutInfo
    })

    binding.ant.taskdef(name: "layout", classname: "jetbrains.antlayout.tasks.LayoutTask")
  }

  def Jps(GantBinding binding, Map map) {
    this(binding)
  }
}
