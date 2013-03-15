package org.jetbrains.jps

/**
 * @author Maxim.Manuylov
 *         Date: 14.03.13
 */
public interface IJavaSdk {
  def getJavaExecutable();

  def getRuntimeRoots();
}