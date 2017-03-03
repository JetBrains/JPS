package org.jetbrains.jps;

import java.util.List;

/**
 * @author Maxim.Manuylov
 *         Date: 14.03.13
 */
public interface IModule {
  IJavaSdk getJavaSdk();

  List<String> getTestRuntimeClasspath();

  String getBasePath();

  List<String> getModuleOutputFolders(boolean includeTests);

  List<IModule> getTestModuleDependencies();
}