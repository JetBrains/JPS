// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.incremental;

import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.storage.TimestampStorage;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Zhuravlev
 *         Date: 9/17/11
 */
public abstract class Builder {

  public static boolean isFileDirty(File file, CompileContext context, TimestampStorage tsStorage) throws IOException {
    return !context.isMake() || tsStorage.getStamp(file) != file.lastModified();
  }

  public static enum ExitCode {
    OK, ABORT, ADDITIONAL_PASS_REQUIRED
  }

  public abstract ExitCode build(CompileContext context, ModuleChunk chunk) throws ProjectBuildException;

  public abstract String getDescription();

  public void cleanupResources() {

  }
}
