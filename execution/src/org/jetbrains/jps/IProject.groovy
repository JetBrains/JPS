package org.jetbrains.jps

/**
 * @author Maxim.Manuylov
 *         Date: 14.03.13
 */
public interface IProject {
  def getAnt();

  IModule findModuleByName(String moduleName);

  IJavaSdk getJavaSdk();

  List<String> getTestRuntimeClasspath();

  Collection<IModule> getAllModules();

  def info(String text);

  def warning(String text);

  def error(String text);
}