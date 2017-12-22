package org.jetbrains.jps.kotlin;

import org.jetbrains.jps.RunConfiguration;
import org.jetbrains.jps.runConf.java.JavaBasedRunConfigurationLauncher;

import java.util.Collections;
import java.util.List;

class KotlinAppLauncher extends JavaBasedRunConfigurationLauncher {
  public KotlinAppLauncher() {
    super("JetRunConfigurationType");
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
