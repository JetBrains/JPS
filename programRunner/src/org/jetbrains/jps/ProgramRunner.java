package org.jetbrains.jps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nik
 */
public class ProgramRunner {
  public static void main(String[] args) throws Exception {
    List<String> classpath = readLines(args[0]);
    List<String> loadedArgs = readLines(args[1]);
    final String className = args[2];

    List<URL> urls = new ArrayList<URL>();
    for (String path : classpath) {
      urls.add(new File(path).toURI().toURL());
    }

    URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
    Thread.currentThread().setContextClassLoader(classLoader);
    final Class<?> aClass = classLoader.loadClass(className);
    aClass.getMethod("main", String[].class).invoke(null, new Object[]{loadedArgs.toArray(new String[loadedArgs.size()])});
  }

  private static List<String> readLines(String filePath) throws IOException {
    List<String> loadedArgs = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    try {
      String line;
      while ((line = reader.readLine()) != null) {
        loadedArgs.add(line);
      }
    }
    finally {
      reader.close();
    }
    return loadedArgs;
  }
}
