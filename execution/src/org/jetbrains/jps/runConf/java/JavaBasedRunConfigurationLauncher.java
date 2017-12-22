package org.jetbrains.jps.runConf.java;

import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.jdom.Element;
import org.jetbrains.jps.*;
import org.jetbrains.jps.idea.IdeaPathUtil;
import org.jetbrains.jps.runConf.RunConfigurationLauncherService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.jetbrains.jps.JDomUtil.*;

/**
 * This launcher is able can be used to start Java main class.
 */
public abstract class JavaBasedRunConfigurationLauncher extends RunConfigurationLauncherService {
  private File myOutputFile;
  private File myErrorFile;
  private Map<String, String> mySystemProperties = new HashMap<String, String>();

  public JavaBasedRunConfigurationLauncher(String typeId) {
    super(typeId);
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
    return runConf.getMacroExpander().expandMacros(runConf.getOption("VM_PARAMETERS"));
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

  @Override
  public void beforeStart(RunConfiguration runConf) {
    super.beforeStart(runConf);

    final Element node = runConf.getNode();
    final Element method = getFirstChildren(node, "method");
    if (method == null) return;

    Element antOption = null;
    for (Element opt : getChildren(method, "option")) {
      final String name = getAttribute(opt, "name");
      final String enabled = getAttribute(opt, "enabled");
      if ("true".equals(enabled) && "AntTarget".equals(name)) {
        antOption = opt;
      }
    }

    if (antOption == null) return;

    String antfile = getAttribute(antOption, "antfile");
    if (antfile == null) return;

    antfile = runConf.getMacroExpander().expandMacros(IdeaPathUtil.pathFromUrl(antfile));

    final String target = getAttribute(antOption, "target");

    final IProject project = runConf.getProject();

    final Ant task = new Ant();
    task.setProject(project.getAntProject());
    task.setAntfile(antfile);
    task.setDir(new File(runConf.getWorkingDir()));
    if (target != null) {
      task.setTarget(target);
    }
    if (myOutputFile != null) {
      task.setOutput(myOutputFile.getAbsolutePath());
    }

    project.info("Starting Ant before launching run configuration " + runConf.getName() + "...");
    task.perform();
  }


  protected final void startInternal(RunConfiguration runConf) throws IOException {
    final IProject project = runConf.getProject();
    final IModule module = runConf.getModule();


    final Java task = new Java();
    task.setProject(project.getAntProject());

    IJavaSdk moduleJre = (module != null) ? module.getJavaSdk() : null;

    IJavaSdk sdk = null;

    // Run Configuration specified jre
    if (Boolean.parseBoolean(runConf.getOption("ALTERNATIVE_JRE_PATH_ENABLED"))) {
      String jreNameOrPath = runConf.getOption("ALTERNATIVE_JRE_PATH");
      IJavaSdkProvider provider = project.getJavaSdkProvider();
      sdk = provider.findByName(jreNameOrPath);
      if (sdk == null) provider.findByPath(jreNameOrPath);
      if (sdk == null) {
        project.warning("Cannot find run configuration specified jre '" + jreNameOrPath + "', will use jre from " + (moduleJre != null ? "module" : "project"));
      }
    }
    if (sdk == null) {
      sdk = moduleJre != null ? moduleJre : project.getJavaSdk();
    }
    if (sdk != null) {
      task.setJvm(sdk.getJavaExecutable());
    } else {
      project.warning("Cannot find java executable, will use java of the current process.");
    }

    task.setClassname(MainClassLauncher.class.getName());
    task.createClasspath().setPath(ClasspathUtil.composeClasspath(MainClassLauncher.class));
    task.setFork(true);
    task.setDir(new File(runConf.getWorkingDir()));
    task.setLogError(true);
    task.setFailonerror(true);

    if (myOutputFile != null) {
      task.setOutput(myOutputFile);
      task.setAppend(true);
    }

    if (myErrorFile != null) {
      task.setError(myErrorFile);
      task.setAppend(true);
    }

    final String mainClass = getMainClassName(runConf);
    final String jvmArgs = getJVMArguments(runConf);
    final String classArgs = getMainClassArguments(runConf);
    final Collection<String> runConfRuntimeCp = getRuntimeClasspath(runConf);

    final String runConfRuntimeCpFile = createTempFile(runConfRuntimeCp);
    final String mainClassCpFile = createTempFile(getMainClassClasspath(runConf));
    final String tmpArgs = createTempFile(splitCommandArgumentsAndUnquote(classArgs));

    project.info("Starting run configuration " + runConf.getName() + "...");

    task.createArg().setLine("" + mainClass + " \"" + mainClassCpFile + "\" \"" + runConfRuntimeCpFile + "\" \"" + tmpArgs + "\"");
    task.createJvmarg().setLine(jvmArgs);

    for (Map.Entry<String, String> envVar : runConf.getEnvVars().entrySet()) {
      final Environment.Variable var = new Environment.Variable();
      var.setKey(envVar.getKey());
      var.setValue(envVar.getValue());
      task.addEnv(var);
    }
    for (Map.Entry<String, String> propEntry : getSystemProperties(runConf).entrySet()) {
      final Environment.Variable var = new Environment.Variable();
      var.setKey(propEntry.getKey());
      var.setValue(propEntry.getValue());
      task.addSysproperty(var);
    }

    task.perform();
  }

  /** This utility differs from splitHonorQuote: it considers quote in sequence 'ddd\" -' as boundary quote.
   * So it can split "-Dffoo=c:\some\path\ddd\" -Dfff=sss correctly.
   * */
  private static List<String> splitCommandArgumentsAndUnquote(String line) {
    final ArrayList<String> result = new ArrayList<String>();
    if (line == null) return result;

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

  protected String createTempFile(Collection<String> runtimeClasspath) throws IOException {
    File tmp = File.createTempFile("runConf", "suffix");
    BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));

    try {
      for (String item: runtimeClasspath) {
        if (item == null) continue;
        writer.write(item);
        writer.newLine();
      }
    } finally {
      writer.close();
    }
    return tmp.getCanonicalPath();
  }

  protected Collection<String> splitClasspath(String classpathStr) {
    Set<String> result = new LinkedHashSet<String>();
    if (classpathStr != null) {
      result.addAll(Arrays.asList(classpathStr.split(File.pathSeparator)));
    }
    return result;
  }

  private Collection<String> getRuntimeClasspath(RunConfiguration runConf) {
    final IProject project = runConf.getProject();
    final IModule module = runConf.getModule();

    final Set<String> runConfRuntimeCp = new LinkedHashSet<String>();

    if (module != null) {
      runConfRuntimeCp.addAll(module.getTestRuntimeClasspath());
    } else {
      runConfRuntimeCp.addAll(project.getTestRuntimeClasspath());
    }

    final IJavaSdk sdk = (module != null && module.getJavaSdk() != null) ? module.getJavaSdk() : project.getJavaSdk();
    if (sdk != null) {
      runConfRuntimeCp.addAll(sdk.getRuntimeRoots());
    }

    return runConfRuntimeCp;
  }
}
