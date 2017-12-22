package org.jetbrains.jps;

import java.io.File;

/**
 * @author nik
 */
public class PathUtil {
  public static String toSystemIndependentPath(String path) {
    return path.replace('\\', '/');
  }

  public static String toSystemDependentPath(String path) {
    char s = '/';
    return path.replace(s, File.separatorChar);
  }

  public static String relativeOrAbsolute(String basePath, String absPath) {
    String nb = normalizePath(basePath);
    String na = normalizePath(absPath);
    if (na.startsWith(nb)) {
      String res = na.substring(nb.length());
      if (res.length() > 0 && res.startsWith("/")) return res.substring(1);
      return res;
    }
    return absPath;
  }

  /**
   * Normalizes path, removes .. and .
   * @param path path to normalize
   */
  public static String normalizePath(String path) {
    return new File(path).toURI().normalize().getPath();
  }
}
