// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.runConf.java

import org.jetbrains.jps.RunConfiguration
import org.jetbrains.jps.runConf.RunConfigurationLauncherService
import org.jetbrains.jps.ProjectBuilder

/**
 * This launcher is able can be used to start Java main class.
 */
public abstract class JavaBasedRunConfigurationLauncher extends RunConfigurationLauncherService {
  private File myOutputFile;
  private File myErrorFile;
  private Map<String, String> mySystemProperties = [:];

  public JavaBasedRunConfigurationLauncher(String typeId) {
    super(typeId)
  }

  /**
   * @return FQN of the main class to execute
   */
  public abstract String getMainClassName(RunConfiguration runConf);

  /**
   * @return main class arguments
   */
  public abstract String getMainClassArguments(RunConfiguration runConf);

  /**
   * @return additional JVM arguments
   */
  public String getJVMArguments(RunConfiguration runConf) {
    return runConf.macroExpander.expandMacros(runConf.allOptions["VM_PARAMETERS"]);
  }

  /**
   * @return system properties (can be specified in JVM arguments too, but this call is more convenient)
   */
  public Map<String, String> getSystemProperties(RunConfiguration runConf) { return mySystemProperties; };

  /**
   * @return classpath required to launch specified main class
   */
  public abstract List<String> getMainClassClasspath(RunConfiguration runConf);

  /**
   * Sets file where to write output of the process.
   */
  public void setOutputFile(File outputFile) {
    myOutputFile = outputFile;
  }

  /**
   * Sets file where to write error output of the process.
   */
  public void setErrorFile(File errFile) {
    myErrorFile = errFile;
  }

  /**
   * Adds system properties.
   */
  public void addSystemProperties(Map<String, String> props) {
    mySystemProperties.putAll(props);
  }

  final void startInternal(RunConfiguration runConf, ProjectBuilder projectBuilder) {
    def project = runConf.project;

    def ant = projectBuilder.binding.ant;
    def params = [
      mainClass: getMainClassName(runConf),
      jvmArgs: getJVMArguments(runConf),
      classArgs: getMainClassArguments(runConf)
    ];

    def module = runConf.module;
    def runConfRuntimeCp = getRuntimeClasspath(runConf, projectBuilder);

    def attrs = [:];
    def sdk = module?.sdk ? module.sdk : project.projectSdk;
    if (sdk != null) {
      attrs["jvm"] = sdk.getJavaExecutable();
    } else {
      projectBuilder.warning("Cannot find java executable, will use java of the current process.");
    }

    attrs["classname"] = MainClassLauncher.class.getName();
    attrs["classpath"] = ClasspathUtil.composeClasspath([MainClassLauncher] as Class[]);
    attrs["fork"] = "true";
    attrs["dir"] = runConf.workingDir;
    attrs["logError"] = "true";
    attrs["failonerror"] = "true";

    if (myOutputFile != null) {
      attrs["output"] = myOutputFile.absolutePath;
    }

    if (myErrorFile != null) {
      attrs["error"] = myErrorFile.absolutePath;
    }

    def runConfRuntimeCpFile = createTempFile(runConfRuntimeCp);
    def mainClassCpFile = createTempFile(getMainClassClasspath(runConf));
    def tmpArgs = createTempFile(splitCommandArgumentsAndUnquote(params.classArgs));
    projectBuilder.info("Starting run configuration $runConf.name ...");

    ant.java(attrs) {
      arg(line: "$params.mainClass \"$mainClassCpFile\" \"$runConfRuntimeCpFile\" \"$tmpArgs\"");
      jvmarg(line: params.jvmArgs);
      for (Map.Entry<String, String> envVar: runConf.envVars.entrySet()) {
        env(key: envVar.getKey(), value: envVar.getValue());
      }
      for (Map.Entry<String, String> propEntry: getSystemProperties(runConf).entrySet()) {
        sysproperty(key: propEntry.getKey(), value: propEntry.getValue());
      }
    };
  }

  /** This utility differs from splitHonorQuote: it considers quote in sequence 'ddd\" -' as boundary quote.
   * So it can split "-Dffoo=c:\some\path\ddd\" -Dfff=sss correctly.
   * */
  private static List<String> splitCommandArgumentsAndUnquote(String line) {
    final ArrayList<String> result = new ArrayList<String>();
    final StringBuilder builder = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      final char c = line.charAt(i);
      if (c == ' ' && !inQuotes) {
        if (builder.length() > 0) {
          result.add(builder.toString());
          builder.setLength(0);
        }
        continue;
      }

      if ((c == '"' || c == '\'') && isNotEscapedQuote(line, i)) {
        inQuotes = !inQuotes;
      }
      builder.append(c);
    }

    if (builder.length() > 0) {
      result.add(builder.toString());
    }
    return removeQuotes(result);
  }

  private static List<String> removeQuotes(final List<String> result) {
    for (int i = 0; i < result.size(); i++) {
      String value = result.get(i);
      if (value.length() > 1 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
        value = value.substring(1, value.length()-1);
      }
      result.set(i, value);
    }
    return result;
  }

  private static boolean isNotEscapedQuote(final String line, final int i) {
    if (i == 0) return true;
    if (line.charAt(i - 1) == '\\') {  // Previous character is escaping one

      int j;
      for(j = i + 1; j < line.length(); j ++) { // inspect chars after the quote
        if (line.charAt(j) == ' ') continue;
        return (line.charAt(j) == '-') || (line.charAt(j) == '\"' && j + 1 < line.length() && line.charAt(j + 1) == '-');             // next option started, so quote is not escaped actually
      }
      return j == line.length();
    }
    return true;
  }

  protected String createTempFile(Collection<String> runtimeClasspath) {
    def tmp = File.createTempFile("runConf", "suffix");
    def writer = new BufferedWriter(new FileWriter(tmp));

    try {
      for (String item: runtimeClasspath) {
        if (item == null) continue;
        writer.writeLine(item);
      }
    } finally {
      writer.close();
    }
    return tmp.getCanonicalPath();
  }

  protected Collection<String> splitClasspath(String classpathStr) {
    def result = new LinkedHashSet<String>();
    if (classpathStr != null) {
      result.addAll(Arrays.asList(classpathStr.split(File.pathSeparator)));
    }
    return result;
  }

  private Collection<String> getRuntimeClasspath(RunConfiguration runConf, ProjectBuilder projectBuilder) {
    def runConfRuntimeCp = new LinkedHashSet<String>();
    if (runConf.module != null) {
      runConfRuntimeCp.addAll(projectBuilder.moduleRuntimeClasspath(runConf.module, true));
    } else {
      runConfRuntimeCp.addAll(projectBuilder.getProjectPaths().getProjectRuntimeClasspath(true));
    }

    return runConfRuntimeCp;
  }
}
