package org.jetbrains.jps

/**
 * @author nik
 */
class PathUtil {
  static String toSystemIndependentPath(String path) {
    return path.replace('\\', '/')
  }

  static String toSystemDependentPath(String path) {
    char s = '/'
    return path.replace(s, File.separatorChar)
  }

  static String relativeOrAbsolute(String basePath, String absPath) {
    def nb = normalizePath(basePath);
    def na = normalizePath(absPath);
    if (na.startsWith(nb)) {
      def res = na.substring(nb.length());
      if (res.length() > 0 && res.startsWith("/")) return res.substring(1);
      return res;
    };
    return absPath;
  }

  /**
   * Normalizes path, removes .. and .
   * @param path path to normalize
   */
  static String normalizePath(String path) {
    return new File(path).toURI().normalize().getPath()
  }
}
