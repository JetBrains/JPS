package org.jetbrains.jps.runConf.java;

import org.jetbrains.jps.RunConfiguration;

import java.util.Collections;
import java.util.List;

public class JavaAppLauncher extends JavaBasedRunConfigurationLauncher {
  public JavaAppLauncher() {
    super("Application");
  }

  @Override
  public String getMainClassName(RunConfiguration runConf) {
    return runConf.getOption("MAIN_CLASS_NAME");
  }

  @Override
  public String getMainClassArguments(RunConfiguration runConf) {
    return runConf.getOption("PROGRAM_PARAMETERS");
  }

  @Override
  public List<String> getMainClassClasspath(RunConfiguration runConf) {
    return Collections.emptyList();
  }
}
