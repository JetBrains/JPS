package org.jetbrains.jps.idea

import org.jetbrains.jps.artifacts.Artifact
import org.jetbrains.jps.*

 /**
 * @author max
 */
public class IdeaProjectLoader {
  private int libraryCount = 0
  Project project
  private String projectOutputPath
  private String projectLanguageLevel
  private Map<String, String> pathVariables
  private ProjectMacroExpander projectMacroExpander

  public static String guessHome(Script script) {
    File home = new File(script["gant.file"].substring("file:".length()))

    while (home != null) {
      if (home.isDirectory()) {
        if (new File(home, ".idea").exists()) return home.getCanonicalPath()
      }

      home = home.getParentFile()
    }

    return null
  }

  public static ProjectMacroExpander loadFromPath(Project project, String path, Map<String, String> pathVariables) {
    def loader = new IdeaProjectLoader(project, pathVariables)
    loader.doLoadFromPath(path);
    return loader.projectMacroExpander;
  }

  public static ProjectMacroExpander loadFromPath(Project project, String path) {
    return loadFromPath(project, path, [:])
  }

  private def IdeaProjectLoader(Project project, Map<String, String> pathVariables) {
    this.project = project;
    this.pathVariables = pathVariables
  }

  private def doLoadFromPath(String path) {
    def fileAtPath = new File(path)

    if (fileAtPath.isFile() && path.endsWith(".ipr")) {
      loadFromIpr(path)
      return
    }
    else {
      File directoryBased = null;
      if (".idea".equals(fileAtPath.getName())) {
        directoryBased = fileAtPath;
      }
      else {
        def child = new File(fileAtPath, ".idea")
        if (child.exists()) {
          directoryBased = child
        }
      }

      if (directoryBased != null) {
        loadFromDirectoryBased(directoryBased.getCanonicalFile())
        return
      }
    }

    project.error("Cannot find IntelliJ IDEA project files at $path")
  }

  def loadFromIpr(String path) {
    def iprFile = new File(path).getAbsoluteFile()
    projectMacroExpander = new ProjectMacroExpander(pathVariables, iprFile.parentFile.absolutePath)

    def root = new XmlParser(false, false).parse(iprFile)
    loadProjectJdkAndOutput(root)
    loadCompilerConfiguration(root)
    loadProjectFileEncodings(root)
    loadModules(getComponent(root, "ProjectModuleManager"))
    loadProjectLibraries(getComponent(root, "libraryTable"))
    loadArtifacts(getComponent(root, "ArtifactManager"))
    loadRunConfigurations(getComponent(root, "ProjectRunConfigurationManager"))
  }

  def loadFromDirectoryBased(File dir) {
    projectMacroExpander = new ProjectMacroExpander(pathVariables, dir.parentFile.absolutePath)
    def modulesXml = new File(dir, "modules.xml")
    if (!modulesXml.exists()) project.error("Cannot find modules.xml in $dir")

    def miscXml = new File(dir, "misc.xml")
    if (!miscXml.exists()) project.error("Cannot find misc.xml in $dir")
    loadProjectJdkAndOutput(new XmlParser(false, false).parse(miscXml))

    def encodingsXml = new File(dir, "encodings.xml")
    if (encodingsXml.exists()) {
      loadProjectFileEncodings(new XmlParser(false, false).parse(encodingsXml))
    }

    def compilerXml = new File(dir, "compiler.xml")
    if (compilerXml.exists()) {
      loadCompilerConfiguration(new XmlParser(false, false).parse(compilerXml))
    }

    Node modulesXmlRoot = new XmlParser(false, false).parse(modulesXml)
    loadModules(modulesXmlRoot.component[0])

    def librariesFolder = new File(dir, "libraries")
    if (librariesFolder.isDirectory()) {
      librariesFolder.eachFile {File file ->
        if (file.isFile()) {
          Node librariesComponent = new XmlParser(false, false).parse(file)
          loadProjectLibraries(librariesComponent)
        }
      }
    }

    def artifactsFolder = new File(dir, "artifacts")
    if (artifactsFolder.isDirectory()) {
      artifactsFolder.eachFile {File file ->
        if (file.isFile()) {
          def artifactsComponent = new XmlParser(false, false).parse(file)
          loadArtifacts(artifactsComponent)
        }
      }
    }

    def runConfFolder = new File(dir, "runConfigurations")
    if (runConfFolder.isDirectory()) {
      runConfFolder.eachFile {File file ->
        if (file.isFile()) {
          def runConfManager = new XmlParser(false, false).parse(file);
          loadRunConfigurations(runConfManager);
        }
      }
    }
  }

  private def loadCompilerConfiguration(Node root) {
    def componentTag = getComponent(root, "CompilerConfiguration")

    processCompilerResources(componentTag);
    processCompilerExcludes(componentTag);

    processJavacOptions(root); // it seems this must be done in Javac builder
  }

  private processJavacOptions(Node root) {
    def javacComponentTag = getComponent(root, "JavacSettings");
    project.props["compiler.javac.options"] = [:];
    javacComponentTag?.option?.each {Node optionTag ->
      project.props["compiler.javac.options"][optionTag."@name"] = optionTag."@value";
    }
  }

  private processCompilerResources(Node componentTag) {
    def includePatterns = []
    def excludePatterns = []
    componentTag?.wildcardResourcePatterns?.getAt(0)?.entry?.each {Node entryTag ->
      String pattern = entryTag."@name"
      if (pattern.startsWith("!")) {
        excludePatterns << convertPattern(pattern.substring(1))
      }
      else {
        includePatterns << convertPattern(pattern)
      }
    }
    if (!includePatterns.isEmpty() || !excludePatterns.isEmpty()) {
      project.binding.ant.patternset(id: "compiler.resources") {
        includePatterns.each { include(name: it)}
        excludePatterns.each { exclude(name: it)}
      }
      project.props["compiler.resources.id"] = "compiler.resources"
    }
  }

  private def processCompilerExcludes(Node compilerConfigurationTag) {
    def excludePatterns = [];
    compilerConfigurationTag?.excludeFromCompile?.getAt(0)?.directory?.each {Node dirTag ->
      def path = PathUtil.toSystemIndependentPath(projectMacroExpander.expandMacros(IdeaProjectLoadingUtil.pathFromUrl(dirTag."@url")));
      def recursive = Boolean.valueOf(dirTag."@includeSubdirectories");
      excludePatterns << path + (recursive ? "/**" : "")
      project.compilerExcludes.addExcludedDirectory(new File(PathUtil.toSystemDependentPath(path)), recursive)
    }
    compilerConfigurationTag?.excludeFromCompile?.getAt(0)?.file?.each {Node fileTag ->
      def path = PathUtil.toSystemIndependentPath(projectMacroExpander.expandMacros(IdeaProjectLoadingUtil.pathFromUrl(fileTag."@url")));
      excludePatterns << path
      project.compilerExcludes.addExcludedFile(new File(PathUtil.toSystemDependentPath(path)))
    }
    if (!excludePatterns.isEmpty()) {
      project.props["compiler.excludes"] = excludePatterns
    }
  }

  private String convertPattern(String pattern) {
    if (pattern.indexOf('/') == -1) {
      return "**/" + pattern
    }
    if (pattern.startsWith("/")) {
      return pattern.substring(1)
    }
    return pattern
  }

  private def loadProjectJdkAndOutput(Node root) {
    def componentTag = getComponent(root, "ProjectRootManager")
    def sdkName = componentTag?."@project-jdk-name"
    def sdk = project.sdks[sdkName]
    if (sdk == null) {
      project.info("Project SDK '$sdkName' is not defined. Embedded javac will be used")
    }
    def outputTag = componentTag?.output?.getAt(0)
    String outputPath = outputTag != null ? IdeaProjectLoadingUtil.pathFromUrl(outputTag.'@url') : null
    projectOutputPath = outputPath != null && outputPath.length() > 0 ? projectMacroExpander.expandMacros(outputPath) : null
    project.projectSdk = sdk
    projectLanguageLevel = componentTag?."@languageLevel"
  }

  private def loadProjectFileEncodings(Node root) {
    def componentTag = getComponent(root, "Encoding");
    if (componentTag == null) return;
    componentTag.file?.each {Node fileNode ->
      def url = fileNode."@url";
      def charset = fileNode."@charset";

      if ("PROJECT".equals(url)) {
        project.projectCharset = charset;
      }
    }
  }

  private NodeList loadProjectLibraries(Node librariesComponent) {
    return librariesComponent?.library?.each {Node libTag ->
      project.createLibrary(libTag."@name", libraryInitializer(libTag, projectMacroExpander))
    }
  }

  def loadArtifacts(Node artifactsComponent) {
    if (artifactsComponent == null) return;
    ArtifactLoader artifactLoader = new ArtifactLoader(project, projectMacroExpander)
    artifactsComponent.artifact.each {Node artifactTag ->
      def artifactName = artifactTag."@name"
      def outputPath = projectMacroExpander.expandMacros(artifactTag."output-path"[0]?.text())
      def root = artifactLoader.loadLayoutElement(artifactTag.root[0], artifactName)
      def options = artifactLoader.loadOptions(artifactTag, artifactName)
      def artifact = new Artifact(name: artifactName, rootElement: root, outputPath: outputPath, properties: options);
      project.artifacts[artifact.name] = artifact;
    }
  }

  def loadRunConfigurations(Node runConfManager) {
    if (runConfManager == null) return;

    runConfManager.configuration.each {Node confTag ->
      def name = confTag.'@name';
      RunConfiguration runConf = new RunConfiguration(project, projectMacroExpander, confTag);
      project.runConfigurations[name] = runConf;
    }
  }

  private def loadModules(Node modulesComponent) {
    modulesComponent?.modules?.module?.each {Node moduleTag ->
      loadModule(projectMacroExpander.expandMacros(moduleTag.@filepath))
    }
  }

  private Library loadLibrary(Project project, String name, Node libraryTag, MacroExpander macroExpander) {
    return new Library(project, name, libraryInitializer(libraryTag, macroExpander))
  }

  private Closure libraryInitializer(Node libraryTag, MacroExpander macroExpander) {
    return {
      Map<String, Boolean> jarDirs = [:]
      libraryTag.jarDirectory.each {Node dirNode ->
        jarDirs[dirNode.@url] = Boolean.parseBoolean(dirNode.@recursive)
      }

      libraryTag.CLASSES.root.each {Node rootTag ->
        def url = rootTag.@url
        def path = macroExpander.expandMacros(IdeaProjectLoadingUtil.pathFromUrl(url))
        if (url in jarDirs.keySet()) {
          def paths = []
          collectChildJars(path, jarDirs[url], paths)
          paths.each {
            classpath it
          }
        }
        else {
          classpath path
        }
      }

      libraryTag.SOURCES.root.each {Node rootTag ->
        src macroExpander.expandMacros(rootTag.@url)
      }

      libraryTag.ANNOTATIONS.root.each {Node rootTag ->
          annotationRoots IdeaProjectLoadingUtil.pathFromUrl(macroExpander.expandMacros(rootTag.@url))
      }
    }
  }

  private def collectChildJars(String path, boolean recursively, List<String> paths) {
    def dir = new File(path)
    dir.listFiles()?.each {File child ->
      if (child.isDirectory()) {
        if (recursively) {
          collectChildJars(path, recursively, paths)
        }
      }
      else if (child.name.endsWith(".jar")) {
        paths << child.absolutePath
      }
    }
  }

  private def loadModule(String imlPath) {
    def moduleFile = new File(imlPath)
    if (!moduleFile.exists()) {
      project.error("Module file $imlPath not found")
      return
    }

    def moduleBasePath = moduleFile.getParentFile().getAbsolutePath()
    MacroExpander moduleMacroExpander = new ModuleMacroExpander(projectMacroExpander, moduleBasePath)
    def currentModuleName = moduleName(imlPath)
    project.createModule(currentModuleName) {
      Module currentModule = project.modules[currentModuleName]
      currentModule.basePath = moduleBasePath
      def root = new XmlParser(false, false).parse(moduleFile)
      def componentTag = getComponent(root, "NewModuleRootManager")
      if (componentTag != null) {
        componentTag.orderEntry.each {Node entryTag ->
          String type = entryTag.@type
          DependencyScope scope = getScopeById(entryTag.@scope)
          switch (type) {
            case "module":
              def moduleName = entryTag.attribute("module-name")
              def module = project.modules[moduleName]
              if (module == null) {
                project.warning("Cannot resolve module $moduleName in $currentModuleName")
              }
              else {
                dependency(module, scope)
              }
              break

            case "module-library":
              def libraryTag = entryTag.library[0]
              def libraryName = libraryTag."@name"
              def moduleLibrary = loadLibrary(project, libraryName != null ? libraryName : "moduleLibrary#${libraryCount++}",
                                              libraryTag, moduleMacroExpander)
              dependency(moduleLibrary, scope)

              if (libraryName != null) {
                currentModule.libraries[libraryName] = moduleLibrary
              }
              break;

            case "library":
              def name = entryTag.attribute("name")
              def library = null
              if (entryTag.@level == "project") {
                library = project.libraries[name]
                if (library == null) {
                  project.warning("Cannot resolve project library '$name' in module '$currentModuleName'")
                }
              } else {
                library = project.globalLibraries[name]
                if (library == null) {
                  project.warning("Cannot resolve global library '$name' in module '$currentModuleName'")
                }
              }

              if (library != null) {
                dependency(library, scope)
              }
              break

            case "jdk":
              def name = entryTag.@jdkName
              def sdk = project.sdks[name]
              if (sdk == null) {
                project.warning("Cannot resolve SDK '$name' in module '$currentModuleName'. Embedded javac will be used")
              }
              else {
                currentModule.sdk = sdk
                dependency(sdk, PredefinedDependencyScopes.PROVIDED)
              }
              break

            case "inheritedJdk":
              def sdk = project.projectSdk
              if (sdk != null) {
                currentModule.sdk = sdk
                dependency(sdk, PredefinedDependencyScopes.PROVIDED)
              }
              break
          }
        }

        def srcFolderExists = componentTag.content.sourceFolder[0] != null;

        componentTag.content.sourceFolder.each {Node folderTag ->
          String path = moduleMacroExpander.expandMacros(IdeaProjectLoadingUtil.pathFromUrl(folderTag.@url))
          String prefix = folderTag.@packagePrefix

          if (folderTag.attribute("isTestSource") == "true") {
            testSrc path
          }
          else {
            src path
          }

          if (prefix != null && prefix != "") {
            currentModule.sourceRootPrefixes[path] = (prefix.replace('.', '/'))
          }
        }

        componentTag.content.excludeFolder.each {Node exTag ->
          String path = moduleMacroExpander.expandMacros(IdeaProjectLoadingUtil.pathFromUrl(exTag.@url))
          exclude path
        }

        def languageLevel = componentTag."@LANGUAGE_LEVEL"
        if (languageLevel == null) {
          languageLevel = projectLanguageLevel
        }
        if (languageLevel != null) {
          def ll = convertLanguageLevel(languageLevel)
          currentModule["sourceLevel"] = ll
          currentModule["targetLevel"] = ll
        }

        if (srcFolderExists) {
          if (componentTag."@inherit-compiler-output" == "true") {
            if (projectOutputPath == null) {
              project.error("Module '$currentModuleName' uses output path inherited from project but project output path is not specified")
            }
            currentModule.outputPath = PathUtil.toSystemIndependentPath(new File(new File(projectOutputPath, "production"), currentModuleName).absolutePath)
            currentModule.testOutputPath = PathUtil.toSystemIndependentPath(new File(new File(projectOutputPath, "test"), currentModuleName).absolutePath)
          }
          else {
            currentModule.outputPath = moduleMacroExpander.expandMacros(IdeaProjectLoadingUtil.pathFromUrl(componentTag.output[0]?.@url))
            currentModule.testOutputPath = moduleMacroExpander.expandMacros(IdeaProjectLoadingUtil.pathFromUrl(componentTag."output-test"[0]?.'@url'))
            if (currentModule.testOutputPath == null) {
              currentModule.testOutputPath = currentModule.outputPath
            }
          }
        }
      }

      def facetManagerTag = getComponent(root, "FacetManager")
      if (facetManagerTag != null) {
        def facetLoader = new FacetLoader(currentModule, moduleMacroExpander)
        facetLoader.loadFacets(facetManagerTag)
      }
    }
  }

  private String convertLanguageLevel(String imlPropertyText) {
    switch (imlPropertyText) {
      case "JDK_1_3": return "1.3"
      case "JDK_1_4": return "1.4"
      case "JDK_1_5": return "1.5"
      case "JDK_1_6": return "1.6"
      case "JDK_1_7": return "1.7"
      case "JDK_1_8": return "1.8"
    }

    return "1.6"
  }

  private DependencyScope getScopeById(String id) {
    switch (id) {
      case "COMPILE": return PredefinedDependencyScopes.COMPILE
      case "RUNTIME": return PredefinedDependencyScopes.RUNTIME
      case "TEST": return PredefinedDependencyScopes.TEST
      case "PROVIDED": return PredefinedDependencyScopes.PROVIDED
      default: return PredefinedDependencyScopes.COMPILE
    }
  }

  private String moduleName(String imlPath) {
    def fileName = new File(imlPath).getName()
    return fileName.substring(0, fileName.length() - ".iml".length())
  }

  Node getComponent(Node root, String name) {
    return root.component.find {it."@name" == name}
  }
}
