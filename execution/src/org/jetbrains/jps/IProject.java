package org.jetbrains.jps;

import org.apache.tools.ant.Project;

import java.util.Collection;
import java.util.List;

/**
 * @author Maxim.Manuylov
 *         Date: 14.03.13
 */
public interface IProject {
  Project getAntProject();

  IModule findModuleByName(String moduleName);

  IJavaSdk getJavaSdk();

  IJavaSdkProvider getJavaSdkProvider();

  List<String> getTestRuntimeClasspath();

  Collection<IModule> getAllModules();

  void info(String text);

  void warning(String text);

  void error(String text);
}