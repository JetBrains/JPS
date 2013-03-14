package org.jetbrains.jps

/**
 * @author Maxim.Manuylov
 *         Date: 14.03.13
 */
public interface IModule {
  IJavaSdk getJavaSdk();

  List<String> getTestRuntimeClasspath();

  String getBasePath();
}