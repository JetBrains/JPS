package org.jetbrains.jps.runConf.java;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainClassLauncher {
  public static void main(String[] args) throws Throwable {
    if (args.length < 4) {
      return;
    }

    String className = args[0];
    String testRunnerCp = args[1];
    String testsRuntimeCp = args[2];
    String tempArgsFileName = args[3];

    URL[] testsRuntimeClasspath = getClasspathElems(testsRuntimeCp);
    URL[] tcRuntimeClasspath = getClasspathElems(testRunnerCp);
    List<String> arguments = getArguments(tempArgsFileName);

    System.out.println("Runtime classpath: " + Arrays.asList(testsRuntimeClasspath));

    ClassLoader testsRuntimeClassloader = createTestsRuntimeClassloader(testsRuntimeClasspath);
    ClassLoader mainClassLoader = new MainClassClassLoader(tcRuntimeClasspath, testsRuntimeClassloader);

    Class<?> main = mainClassLoader.loadClass(className);
    Method mainMethod = main.getMethod("main", String[].class);

    // we must not set mainClassLoader to context classloader, because it allows access to our classes, which we want to hide
    Thread.currentThread().setContextClassLoader(testsRuntimeClassloader);

    try {
      mainMethod.invoke(null, new Object[] { arguments.toArray(new String[arguments.size()])});
    }
    catch (Throwable e) {
      reportBuildProblem(e.toString());
      throw new RuntimeException("Exception occurred in main class: " + main.getName() + ", error: " + e.toString(), e);
    }

    System.exit(0);
  }

  // creates classloader which does not load classes from the current classpath
  private static URLClassLoader createTestsRuntimeClassloader(URL[] classpath) {
    ClassLoader parent = null;
    try {
      Method platformClassLoader = ClassLoader.class.getMethod("getPlatformClassLoader");
      parent = (ClassLoader) platformClassLoader.invoke(null);
    } catch (Exception e) {
      // ignore, wrong Java
    }
    return new URLClassLoader(classpath, parent);
  }

  private static void reportBuildProblem(final String message) { // todo reuse TeamCity code
    System.err.println("##teamcity[buildProblem type='jps-runtime-error' identity='" +
                         ("jps-runtime-error" + message).hashCode() +
                         "' description='" + escape(message) + "']");
  }

  private static String escape(final String message) {
    return message
      .replace("|", "||")
      .replace("\n", "|n")
      .replace("\r", "|r")
      .replace("\u0085", "|0x0085")
      .replace("\u2028", "|0x2028")
      .replace("\u2029", "|0x2029")
      .replace("'", "|'")
      .replace("[", "|[")
      .replace("]", "|]");
  }

  private static URL[] getClasspathElems(String tempFileName) throws IOException {
    List<URL> result = new ArrayList<URL>();
    for (String line: loadLines(tempFileName)) {
      URL url = new File(line).toURL();
      result.add(url);
    }

    //System.out.println("DEBUG INFO: libraries: " + result);
    URL[] array = new URL[result.size()];
    result.toArray(array);
    return array;
  }

  private static List<String> getArguments(String tempFileName) throws IOException {
    return loadLines(tempFileName);
  }

  private static List<String> loadLines(final String tempFileName) throws IOException {
    List<String> result = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new FileReader(tempFileName));
    try {
      while(true) {
        String line = reader.readLine();
        if (line == null) break;
        result.add(line);
      }
      //System.out.println("DEBUG INFO: arguments: " + result.toString());
      return result;
    } finally {
      reader.close();
    }
  }

  /**
   * This classloader makes classes from the specified URLs more preferable than classes from the parent classloader.
   * Specified main class is loaded by this classloader.
   */
  private static class MainClassClassLoader extends URLClassLoader {
    public MainClassClassLoader(final URL[] urls, final ClassLoader parent) {
      super(urls, parent);
    }

    public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
      Class c = findLoadedClass(name);
      if (c == null) {
        try {
          c = findClass(name);
        }
        catch (ClassNotFoundException e) {
          c = super.loadClass(name, resolve);
        }
      }
      if (resolve) {
        resolveClass(c);
      }
      return c;
    }
  }
}
