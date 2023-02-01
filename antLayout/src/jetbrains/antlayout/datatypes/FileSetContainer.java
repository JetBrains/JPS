// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package jetbrains.antlayout.datatypes;

import jetbrains.antlayout.util.LayoutFileSet;
import jetbrains.antlayout.util.TempFileFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author nik
 */
public class FileSetContainer extends Content {
  private FileSet fileSet;

  public FileSetContainer(FileSet fileSet) {
    this.fileSet = fileSet;
  }

  @Override
  public List<LayoutFileSet> build(TempFileFactory temp) {
    return Collections.singletonList(Container.createCopy(fileSet));
  }

  @Override
  public void validateArguments() throws BuildException {
    if (fileSet instanceof IdeaModuleBase) {
      ((IdeaModuleBase) fileSet).validateArguments();
    }
  }
}
