package org.jetbrains.jps.builders

import org.jetbrains.jps.*
import org.jetbrains.jps.builders.javacApi.Java16ApiCompilerRunner
import org.codehaus.groovy.tools.FileSystemCompiler

/**
 * @author max
 */
class JavacBuilder implements ModuleBuilder, ModuleCycleBuilder {
  def preprocessModuleCycle(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
    doBuildModule(moduleChunk, state)
  }

  def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
    doBuildModule(moduleChunk, state)
  }

  def doBuildModule(ModuleChunk module, ModuleBuildState state) {
    if (state.sourceRoots.isEmpty()) return;

    String sourceLevel = module["sourceLevel"]
    String targetLevel = module["targetLevel"]
    String customArgs = module["javac_args"]; // it seems javac_args property is not set, can we drop it?
    if (module.project.builder.useInProcessJavac) {
      String version = System.getProperty("java.version")
      if (sourceLevel == null || version.startsWith(sourceLevel)) {
        if (Java16ApiCompilerRunner.compile(module, state, sourceLevel, targetLevel, customArgs)) {
          return
        }
      }
      else {
        module.project.info("In-process Javac won't be used for '${module.name}', because Java version ($version) doesn't match to source level ($sourceLevel)")
      }
    }

    def params = [:]
    params.destdir = state.targetFolder
    if (sourceLevel != null) params.source = sourceLevel
    if (targetLevel != null) params.target = targetLevel

    def javacOpts = module.project.props["compiler.javac.options"] ?: [:];
    def memHeapSize = javacOpts["MAXIMUM_HEAP_SIZE"] == null ? "512m" : javacOpts["MAXIMUM_HEAP_SIZE"] + "m";
    def boolean debugInfo = !"false".equals(javacOpts["DEBUGGING_INFO"]);
    def boolean nowarn = "true".equals(javacOpts["GENERATE_NO_WARNINGS"]);
    def boolean deprecation = !"false".equals(javacOpts["DEPRECATION"]);
    customArgs = javacOpts["ADDITIONAL_OPTIONS_STRING"];
    if ((customArgs == null || customArgs.indexOf("-encoding") == -1) && module.project.projectCharset != null) {
      params.encoding = module.project.projectCharset;
    }

    params.fork = "true"
    params.memoryMaximumSize = memHeapSize;
    params.debug = String.valueOf(debugInfo);
    params.nowarn = String.valueOf(nowarn);
    params.deprecation = String.valueOf(deprecation);

    def javacExecutable = getJavacExecutable(module)
    if (javacExecutable != null) {
      params.executable = javacExecutable
    }

    def ant = module.project.binding.ant;

    def globalExcludes = module.project.props["compiler.excludes"] ?: [];

    ant.javac(params) {
      if (customArgs) {
        compilerarg(line: customArgs)
      }

      state.sourceRoots.each {
        src(path: it)
      }

      state.excludes.each { String root ->
        state.sourceRoots.each {String src ->
          def relPath = PathUtil.relativeOrAbsolute(src, root);
          if (!relPath.equals(root)) {
            exclude(name: "${relPath}/**")
          }
        }
      }

      globalExcludes.each { String name ->
        state.sourceRoots.each {String src ->
          def relPath = PathUtil.relativeOrAbsolute(src, name);
          if (!relPath.equals(name)) {
            exclude(name: relPath)
          }
        }
      }

      classpath {
        state.classpath.each {
          pathelement(location: it)
        }
      }
    }
  }

  private String getJavacExecutable(ModuleChunk module) {
    def customJavac = module["javac"]
    if (customJavac != null) {
      return customJavac
    }

    JavaSdk sdk = getSdk(module);
    if (sdk != null) return sdk.getJavacExecutable();
    return null;
  }

  static JavaSdk getSdk(ModuleChunk module) {
    def jdk = module.getSdk()
    if (jdk instanceof JavaSdk) {
      return jdk;
    }
    return null
  }
}

class ResourceCopier implements ModuleBuilder {

  def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
    if (state.sourceRoots.isEmpty()) return;

    def ant = project.binding.ant

    state.sourceRoots.each {String root ->
      if (new File(root).exists()) {
        def target = state.targetFolder
        def prefix = moduleChunk.modules.collect { it.sourceRootPrefixes[root] }.find {it != null}
        if (prefix != null) {
          if (!(target.endsWith("/") || target.endsWith("\\"))) {
            target += "/"
          }
          target += prefix
        }

        ant.copy(todir: target) {
          fileset(dir: root) {
            patternset(refid: moduleChunk["compiler.resources.id"])
            type(type: "file")
          }
        }
      }
      else {
        project.warning("$root doesn't exist")
      }
    }
  }
}

class GroovycBuilder implements ModuleBuilder {
  def GroovycBuilder(Project project) {
    project.taskdef (name: "groovyc", classname: "org.codehaus.groovy.ant.Groovyc")
  }

  def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
    List<String> groovyFiles = GroovyFileSearcher.collectGroovyFiles(state.sourceRoots)
    if (groovyFiles.isEmpty()) return

    def ant = project.binding.ant

    final String destDir = state.targetFolder

    ant.touch(millis: 239) {
      fileset(dir: destDir) {
        include(name: "**/*.class")
      }
    }

    List<String> classpathList = new ArrayList<String>(state.classpath.size() + 1)
    classpathList.addAll(state.classpath)
    classpathList.add(destDir)// Includes classes generated there by javac compiler

    if (project.builder.runGroovyCompilerInSeparateProcess) {
      // unfortunately we cannot use standard groovyc task with fork=true because of a bug in Groovyc task: it creates too long command line if classpath is large
      int size = groovyFiles.size()
      project.stage("Compiling ${size} groovy file${size > 1 ? "s" : ""}")

      def classpathString = classpathList.join(File.pathSeparator)

      String tempDirPath = project.builder.getTempDirectoryPath("groovyc_commandline_${moduleChunk.name}")
      ant.mkdir(dir: tempDirPath)
      def commandLineFile = new File(tempDirPath, "command-line.txt")
      commandLineFile.withPrintWriter {PrintWriter out ->
        out.println("-classpath")
        out.println(classpathString)
        out.println("-d")
        out.println(destDir)
        groovyFiles.each {
          out.println(it)
        }
      }
      def classpathFile = new File(tempDirPath, "classpath.txt")
      classpathFile.withPrintWriter {PrintWriter out ->
        classpathList.each {
          out.println(it)
        }
      }

      def attrs = [:];
      attrs["classname"] = ProgramRunner.class.name;
      attrs["fork"] = "true";
      attrs["failonerror"] = "true";

      def sdk = JavacBuilder.getSdk(moduleChunk)
      if (sdk != null) attrs["jvm"] = sdk.getJavaExecutable();

      ant.java(attrs) {
        classpath {
          pathelement(location: new File(ProgramRunner.class.protectionDomain.codeSource.location.toURI()).absolutePath)
        }
        arg(value: classpathFile.absolutePath)
        arg(value: commandLineFile.absolutePath)
        arg(value: FileSystemCompiler.class.name)
      }
    }
    else {
      ant.groovyc(destdir: destDir) {
        state.sourceRoots.each {
          src(path: it)
        }

        include(name: "**/*.groovy")

        classpath {
          classpathList.each {
            pathelement(location: it)
          }
        }
      }

    }

    ant.touch() {
      fileset(dir: destDir) {
        include(name: "**/*.class")
      }
    }
  }
}

class GroovyStubGenerator implements ModuleBuilder {

  def GroovyStubGenerator(Project project) {
    project.taskdef (name: "generatestubs", classname: "org.codehaus.groovy.ant.GenerateStubsTask")
  }

  def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
    if (!GroovyFileSearcher.containGroovyFiles(state.sourceRoots)) return

    def ant = project.binding.ant

    String targetFolder = project.targetFolder
    File dir = new File(targetFolder != null ? targetFolder : ".", "___temp___")
    BuildUtil.deleteDir(project, dir.absolutePath)
    ant.mkdir(dir: dir)

    def stubsRoot = dir.getAbsolutePath()
    ant.generatestubs(destdir: stubsRoot) {
      state.sourceRoots.each {
        src(path: it)
      }

      include (name: "**/*.groovy")
      include (name: "**/*.java")

      classpath {
        state.classpath.each {
          pathelement(location: it)
        }
      }
    }

    state.sourceRoots << stubsRoot
    state.tempRootsToDelete << stubsRoot
  }

}

class JetBrainsInstrumentations implements ModuleBuilder {

  def JetBrainsInstrumentations(Project project) {
    project.taskdef(name: "jb_instrumentations", classname: "com.intellij.ant.InstrumentIdeaExtensions")
  }

  def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
    def ant = project.binding.ant

    ant.jb_instrumentations(destdir: state.targetFolder, failonerror: "false", includeAntRuntime: "false") {
      state.sourceRoots.each {
        src(path: it)
      }

      def sourceRootProcessor = {String root ->
        def prefix = project.modules.values().collect { it.sourceRootPrefixes[root] }.find {it != null}
        nestedformdirs(prefix: prefix ?: "") {
          pathelement(location: root)
        }
      }
      state.sourceRoots.each sourceRootProcessor
      state.moduleDependenciesSourceRoots.each sourceRootProcessor

      classpath {
        state.classpath.each {
          pathelement(location: it)
        }
      }
    }
  }
}

class CustomTasksBuilder implements ModuleBuilder {
  List<ModuleBuildTask> tasks = []

  def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
    moduleChunk.modules.each {Module module ->
      tasks*.perform(module, state.targetFolder)
    }
  }

  def registerTask(String moduleName, Closure task) {
    tasks << ({Module module, String outputFolder ->
      if (module.name == moduleName) {
        task(module, outputFolder)
      }
    } as ModuleBuildTask)
  }
}

class CommonCompilerOptions {

}