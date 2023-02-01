// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps;

import java.io.File;
import java.net.URI;
import java.util.Set;

/**
 * @author nik
 */
public class PathUtil {

  public static String toPath(URI uri) {
    if (uri.getScheme() == null) {
      return uri.getPath();
    }
    return new File(uri).getAbsolutePath();
  }

  public static boolean isUnder(Set<File> ancestors, File file) {
    File current = file;
    while (current != null) {
      if (ancestors.contains(current)) {
        return true;
      }
      current = current.getParentFile();
    }
    return false;
  }
}
