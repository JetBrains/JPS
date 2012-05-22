package org.jetbrains.jps.kotlin

import org.jetbrains.jps.RunConfiguration
import org.jetbrains.jps.runConf.java.JavaBasedRunConfigurationLauncher

class KotlinAppLauncher extends JavaBasedRunConfigurationLauncher {
  public KotlinAppLauncher() {
    super("JetRunConfigurationType")
  }

  @Override
  String getMainClassName(RunConfiguration runConf) {
    return runConf.allOptions["MAIN_CLASS_NAME"]
  }

  @Override
  String getMainClassArguments(RunConfiguration runConf) {
    return runConf.allOptions["PROGRAM_PARAMETERS"]
  }

  @Override
  List<String> getMainClassClasspath(RunConfiguration runConf) {
    return Collections.emptyList()
  }
}
