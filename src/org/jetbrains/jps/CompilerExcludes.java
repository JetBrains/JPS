package org.jetbrains.jps;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author nik
 */
public class CompilerExcludes {
  private final Set<File> myFiles = new HashSet<File>();
  private final Set<File> myDirectories = new HashSet<File>();
  private final Set<File> myRecursivelyExcludedDirectories = new HashSet<File>();

  public void addExcludedFile(File file) {
    myFiles.add(file);
  }

  public void addExcludedDirectory(File dir, boolean recursively) {
    (recursively ? myRecursivelyExcludedDirectories : myDirectories).add(dir);
  }

  public boolean isExcluded(File file) {
    if (myFiles.contains(file)) {
      return true;
    }

    if (!myDirectories.isEmpty() || !myRecursivelyExcludedDirectories.isEmpty()) { // optimization
      File parent = getParentFile(file);
      if (myDirectories.contains(parent)) {
        return true;
      }

      while (parent != null) {
        if (myRecursivelyExcludedDirectories.contains(parent)) {
          return true;
        }
        parent = getParentFile(parent);
      }
    }
    return false;
  }

  public static File getParentFile(File file) {
    int skipCount = 0;
    File parentFile = file;
    while (true) {
      parentFile = parentFile.getParentFile();
      if (parentFile == null) {
        return null;
      }
      if (".".equals(parentFile.getName())) {
        continue;
      }
      if ("..".equals(parentFile.getName())) {
        skipCount++;
        continue;
      }
      if (skipCount > 0) {
        skipCount--;
        continue;
      }
      return parentFile;
    }
  }

}
