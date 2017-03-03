package org.jetbrains.jps.idea;

import org.jetbrains.jps.MacroExpander;
import org.jetbrains.jps.PathUtil;

import java.util.Map;

/**
 * @author nik
 */
public class ProjectMacroExpander implements MacroExpander {
  private Map<String, String> pathVariables;
  private String projectBasePath;

  public ProjectMacroExpander(Map<String, String> pathVariables, String projectBasePath) {
    this.pathVariables = pathVariables;
    this.projectBasePath = PathUtil.toSystemIndependentPath(projectBasePath);
  }

  @Override
  public String expandMacros(String path) {
    if (path == null) return null;

    path = path.replace("$PROJECT_DIR$", projectBasePath);
    for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
      final String name = entry.getKey();
      final String value = entry.getValue();
      path = path.replace("$" + name + "$", value);
    }
    return path;
  }
}
