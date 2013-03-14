package org.jetbrains.jps

import org.jetbrains.jps.idea.IdeaPathUtil
import org.jetbrains.jps.idea.ModuleMacroExpander
import org.jetbrains.jps.idea.OwnServiceLoader
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
      this.allOptions[opt.'@name'] = value;
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

  private static OwnServiceLoader<RunConfigurationLauncherService> runConfLauncherServices = OwnServiceLoader.load(RunConfigurationLauncherService.class)

  def RunConfigurationLauncherService getLauncher() {
    for (RunConfigurationLauncherService service: runConfLauncherServices.iterator()) {
      if (service.typeId == type) {
        return service;
      }
    }

    return null;
  }
}