package org.jetbrains.jps

/**
 * @author nik
 */
class PathUtil {
  static String toSystemIndependentPath(String path) {
    return path.replace('\\', '/')
  }
  
  static String relativeOrAbsolute(String basePath, String absPath) {
    def nb = toSystemIndependentPath(basePath);
    def na = toSystemIndependentPath(absPath);
    if (na.startsWith(nb)) {
      def res = na.substring(nb.length());
      if (res.length() > 0 && res.startsWith("/")) return res.substring(1);
      return res;
    };
    return absPath;
  }
}
