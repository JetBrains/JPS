package org.jetbrains.jps.builders

import org.jetbrains.jps.Project

 /**
 * @author nik
 */
class BuildUtil {
  static def deleteDir(Project project, String path) {
    if (path == null) return
    int attempts = 10;
    while (attempts-- > 0) {
      boolean lastAttempt = attempts == 0
      project.binding.ant.delete(dir: path, quiet: !lastAttempt)
      if (!new File(path).exists()) {
        return
      }
      project.info("Failed to delete $path, trying again")
      Thread.sleep(100)
    }
  }

  static String suggestFileName(String text) {
    String name = text.replaceAll(/(;|:|\s)/, "_")
    if (name.length() > 100) {
      name = name.substring(0, 100) + "_etc"
    }
    return name;
  }
}
