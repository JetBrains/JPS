package org.jetbrains.jps;

import groovy.util.Node;
import org.jetbrains.jps.idea.IdeaPathUtil;
import org.jetbrains.jps.idea.ModuleMacroExpander;
import org.jetbrains.jps.idea.ProjectMacroExpander;
import org.jetbrains.jps.runConf.RunConfigurationLauncherService;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static org.jetbrains.jps.GroovyUtil.*;

/**
 * Represents IntelliJ IDEA run configuration.
 * @author pavel.sher
 */
public class RunConfiguration {
  final IProject project;
  final String name;
  private final String type;
  private final IModule module;
  private final String workingDir;
  private final Map<String, String> allOptions;
  private final Map<String, String> envVars;
  private final Node node;
  private final MacroExpander macroExpander;

  private final static Map<String, String> ourOptionAliases = new HashMap<String, String>();

  public RunConfiguration(IProject project, ProjectMacroExpander macroExpander, Node confTag) {
    MacroExpander macroExpander1;
    this.project = project;
    this.node = confTag;
    this.name = getAttribute(node, "name");
    this.type = getAttribute(node, "type");

    this.allOptions = new HashMap<String, String>();

    for (Node opt : getChildren(node, "option")) {
      String value = getAttribute(opt, "value");
      if (value == null) {
        value = getAttribute(getFirstChildren(opt, "value"), "defaultName");
      }
      String name = getAttribute(opt, "name");
      this.allOptions.put(name, value);
      String alias = ourOptionAliases.get(name);
      if (alias != null) {
        this.allOptions.put(alias, value);
      }
    }

    Node moduleNode = getFirstChildren(node, "module");
    if (moduleNode != null && !"wholeProject".equals(this.allOptions.get("TEST_SEARCH_SCOPE"))) {
      this.module = project.findModuleByName(getAttribute(moduleNode, "name"));
    } else {
      this.module = null;
    }

    macroExpander1 = macroExpander;
    if (this.module != null) {
      macroExpander1 = new ModuleMacroExpander(macroExpander, this.module.getBasePath());
    }
    this.macroExpander = macroExpander1;

    String workDirUrl = this.allOptions.get("WORKING_DIRECTORY");
    if (workDirUrl == null) workDirUrl = "";
    if (!workDirUrl.isEmpty()) {
      workDirUrl = this.macroExpander.expandMacros(IdeaPathUtil.pathFromUrl(workDirUrl));
    }

    this.workingDir = getCanonicalPath(workDirUrl.isEmpty() ? "." : workDirUrl);

    this.envVars = new HashMap<String, String>();
    for (Node el : getChildren(getFirstChildren(node, "envs"), "env")) {
      envVars.put(getAttribute(el, "name"), getAttribute(el, "value"));
    }
  }

  private static ServiceLoader<RunConfigurationLauncherService> runConfLauncherServices = ServiceLoader.load(RunConfigurationLauncherService.class);

  public RunConfigurationLauncherService getLauncher() {
    for (RunConfigurationLauncherService service : runConfLauncherServices) {
      if (service.getTypeId().equals(type)) {
        return service;
      }
    }

    return null;
  }

  public IProject getProject() {
    return project;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public IModule getModule() {
    return module;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  public Map<String, String> getAllOptions() {
    return allOptions;
  }

  public Map<String, String> getEnvVars() {
    return envVars;
  }

  public Node getNode() {
    return node;
  }

  public MacroExpander getMacroExpander() {
    return macroExpander;
  }

  public String getOption(String name) {
    return allOptions.get(name);
  }

  static {
    // Thanks to KotlinStandaloneScriptRunConfigurationType
    ourOptionAliases.put("vmParameters", "VM_PARAMETERS");
    ourOptionAliases.put("programParameters", "PROGRAM_PARAMETERS");
    ourOptionAliases.put("passParentEnvs", "PASS_PARENT_ENVS");
    ourOptionAliases.put("workingDirectory", "WORKING_DIRECTORY");
    ourOptionAliases.put("alternativeJrePath", "ALTERNATIVE_JRE_PATH");
    ourOptionAliases.put("isAlternativeJrePathEnabled", "ALTERNATIVE_JRE_PATH_ENABLED");
    ourOptionAliases.put("mainClassName", "MAIN_CLASS_NAME");
    ourOptionAliases.put("filePath", "FILE_PATH");
  }
}