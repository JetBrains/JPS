// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package jetbrains.antlayout.datatypes;

import org.apache.tools.ant.types.ZipFileSet;

import java.io.File;

/**
 * @author nik
 */
public class IdeaModuleTests extends IdeaModuleBase {
  public IdeaModuleTests() {
  }

  public IdeaModuleTests(ZipFileSet fileset) {
    super(fileset);
  }

  @Override
  protected String getOutputDirProperty() {
    return "module." + getName() + ".output.test";
  }

  @Override
  protected String getKind() {
    return "test";
  }
}
