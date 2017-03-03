package org.jetbrains.jps;

import org.codehaus.gant.GantBuilder;

import java.util.Collection;
import java.util.List;

/**
 * @author Maxim.Manuylov
 *         Date: 14.03.13
 */
public interface IProject {
  GantBuilder getAnt();

  IModule findModuleByName(String moduleName);

  IJavaSdk getJavaSdk();

  IJavaSdkProvider getJavaSdkProvider();

  List<String> getTestRuntimeClasspath();

  Collection<IModule> getAllModules();

  void info(String text);

  void warning(String text);

  void error(String text);
}