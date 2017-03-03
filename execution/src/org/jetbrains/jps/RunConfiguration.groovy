package org.jetbrains.jps

import org.jetbrains.jps.idea.IdeaPathUtil
import org.jetbrains.jps.idea.ModuleMacroExpander
import org.jetbrains.jps.idea.ProjectMacroExpander
import org.jetbrains.jps.runConf.RunConfigurationLauncherService

/**
 * Represents IntelliJ IDEA run configuration.
 * @author pavel.sher
 */
public class RunConfiguration {
  final IProject project;
  final String name;
  final String type;
  final IModule module;
  final String workingDir;
  final Map<String, String> allOptions;
  final Map<String, String> envVars;
  final Node node;
  final MacroExpander macroExpander;

  private final static Map<String, String> ourOptionAliases = new HashMap<String, String>();

  def RunConfiguration(IProject project, ProjectMacroExpander macroExpander, Node confTag) {
    this.project = project;
    this.name = confTag.'@name';
    this.type = confTag.'@type';
    this.node = confTag;

    this.allOptions = [:];
    confTag.option.each{ opt ->
      def value = opt.'@value';
      if (value == null) {
        value = opt.value ? opt.value[0].'@defaultName' : null;
      }
      String name = opt.'@name'
      this.allOptions[name] = value
      def alias = ourOptionAliases.get(name)
      if (alias != null) {
        this.allOptions[alias] = value
      }
    }

    def moduleNode = confTag.module[0];
    if (moduleNode != null && !"wholeProject".equals(this.allOptions['TEST_SEARCH_SCOPE'])) {
      this.module = project.findModuleByName(moduleNode.'@name');
    } else {
      this.module = null;
    }

    this.macroExpander = macroExpander;
    if (this.module != null) {
      this.macroExpander = new ModuleMacroExpander(macroExpander, this.module.basePath);
    }

    def String workDirUrl = this.allOptions['WORKING_DIRECTORY'];
    if (workDirUrl == null) workDirUrl = "";
    if (workDirUrl != '') {
      workDirUrl = this.macroExpander.expandMacros(IdeaPathUtil.pathFromUrl(workDirUrl));
    }

    this.workingDir = workDirUrl == '' ? new File(".").getCanonicalPath() : new File(workDirUrl).getCanonicalPath();

    this.envVars = [:];
    confTag.envs.env.each{ el ->
      this.envVars[el.'@name'] = el.'@value';
    }
  }

  private static ServiceLoader<RunConfigurationLauncherService> runConfLauncherServices = ServiceLoader.load(RunConfigurationLauncherService.class)

  def RunConfigurationLauncherService getLauncher() {
    for (RunConfigurationLauncherService service: runConfLauncherServices.iterator()) {
      if (service.typeId == type) {
        return service;
      }
    }

    return null;
  }

  static {
    // Thanks to KotlinStandaloneScriptRunConfigurationType
    ourOptionAliases.put('vmParameters', 'VM_PARAMETERS')
    ourOptionAliases.put('programParameters', 'PROGRAM_PARAMETERS')
    ourOptionAliases.put('passParentEnvs', 'PASS_PARENT_ENVS')
    ourOptionAliases.put('workingDirectory', 'WORKING_DIRECTORY')
    ourOptionAliases.put('alternativeJrePath', 'ALTERNATIVE_JRE_PATH')
    ourOptionAliases.put('isAlternativeJrePathEnabled', 'ALTERNATIVE_JRE_PATH_ENABLED')
    ourOptionAliases.put('mainClassName', 'MAIN_CLASS_NAME')
    ourOptionAliases.put('filePath', 'FILE_PATH')
  }
}