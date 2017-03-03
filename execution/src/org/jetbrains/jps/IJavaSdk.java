package org.jetbrains.jps;

import java.util.List;

/**
 * @author Maxim.Manuylov
 *         Date: 14.03.13
 */
public interface IJavaSdk {
  String getJavaExecutable();

  List<String> getRuntimeRoots();
}