package org.jetbrains.jps.runConf;

import com.sun.istack.internal.NotNull;
import org.jetbrains.jps.RunConfiguration;

import java.io.IOException;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class RunConfigurationLauncherService {
  @NotNull
  private final String typeId;

  public RunConfigurationLauncherService(@NotNull String typeId) {
    this.typeId = typeId;
  }

  @NotNull
  public String getTypeId() {
    return typeId;
  }

  public void beforeStart(@NotNull RunConfiguration runConf) {
  }

  public void afterFinish(@NotNull RunConfiguration runConf) {
  }

  public final void start(@NotNull RunConfiguration runConf) {
    beforeStart(runConf);
    try {
      startInternal(runConf);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      afterFinish(runConf);
    }
  }

  protected abstract void startInternal(@NotNull RunConfiguration runConf) throws IOException;
}
