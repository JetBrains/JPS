package org.jetbrains.jps.kotlin;

import org.jetbrains.jps.RunConfiguration;
import org.jetbrains.jps.runConf.java.JavaBasedRunConfigurationLauncher;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*
<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="Script" type="KotlinStandaloneScriptRunConfigurationType" factoryName="Kotlin script" nameIsGenerated="true">
    <extension name="coverage" enabled="false" merge="false" sample_coverage="true" runner="idea" />
    <option name="filePath" value="$PROJECT_DIR$/src/script.kts" />
    <option name="vmParameters" value="-Da=b" />
    <option name="alternativeJrePath" value="" />
    <option name="programParameters" value="arg1" />
    <option name="passParentEnvs" value="true" />
    <option name="workingDirectory" value="$PROJECT_DIR$/src" />
    <option name="isAlternativeJrePathEnabled" value="false" />
    <envs />
    <method />
  </configuration>
</component>
 */

public class KotlinStandaloneScriptLauncher extends JavaBasedRunConfigurationLauncher {
    KotlinStandaloneScriptLauncher() {
        super("KotlinStandaloneScriptRunConfigurationType");
    }
/*
Expected command line:
/opt/jdks/1.6/bin/java
  -Da=b
  -Dfile.encoding=UTF-8
  -classpath
    ~/.IntelliJIdea2016.3/config/plugins/Kotlin/kotlinc/lib/kotlin-compiler.jar
   :~/.IntelliJIdea2016.3/config/plugins/Kotlin/kotlinc/lib/kotlin-reflect.jar
   :~/.IntelliJIdea2016.3/config/plugins/Kotlin/kotlinc/lib/kotlin-runtime.jar
  org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
  -script
  /media/data/devel/test-run-configs/src/script.kts
  arg1
 */

    @Override
    public String getMainClassName(RunConfiguration runConf) {
        return "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler";
    }

    @Override
    public String getMainClassArguments(RunConfiguration runConf) {
        StringBuilder sb = new StringBuilder();
        sb.append("-script ")
            .append('"')
            .append(expand(runConf, runConf.getOption("FILE_PATH")))
            .append('"');
        String pps = runConf.getOption("PROGRAM_PARAMETERS");
        if (pps != null) {
            sb.append(' ').append(expand(runConf, pps));
        }
        return sb.toString();
    }

    @Override
    public String getJVMArguments(RunConfiguration runConf) {
        return super.getJVMArguments(runConf) + " -Dfile.encoding=UTF-8";
    }

    @Override
    public List<String> getMainClassClasspath(RunConfiguration runConf) {
        return getKotlinRuntimeJars(runConf);
    }

    private List<String> getKotlinRuntimeJars(RunConfiguration runConf) {
        Map<String, String> properties = getSystemProperties(runConf);
        String kotlin_home = properties.get("jps.kotlin.home"); // Usually 'kotlinc' directory
        if (kotlin_home == null) throw new IllegalStateException("jps.kotlin.home is not set");
        return Arrays.asList("$kotlin_home/lib/kotlin-compiler.jar", "$kotlin_home/lib/kotlin-stdlib.jar", "$kotlin_home/lib/kotlin-reflect.jar");
    }

    private static String expand(RunConfiguration runConf, String string) {
        return runConf.getMacroExpander().expandMacros(string);
    }
}
