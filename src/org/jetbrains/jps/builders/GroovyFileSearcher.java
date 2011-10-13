package org.jetbrains.jps.builders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nik
 */
public class GroovyFileSearcher {
  public static boolean containGroovyFiles(List<? extends CharSequence> paths) {
    return !collectGroovyFiles(paths).isEmpty();
  }

  public static List<String> collectGroovyFiles(List<? extends CharSequence> paths) {
    List<String> result = new ArrayList<String>();
    for (CharSequence path : paths) {
      collectGroovyFiles(new File(path.toString()), result);
    }
    return result;
  }

  private static void collectGroovyFiles(File file, List<String> result) {
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      if (files != null) {
        for (File child : files) {
          collectGroovyFiles(child, result);
        }
      }
    }
    else {
      if (file.getName().endsWith(".groovy")) {
        result.add(file.getAbsolutePath());
      }
    }
  }
}
