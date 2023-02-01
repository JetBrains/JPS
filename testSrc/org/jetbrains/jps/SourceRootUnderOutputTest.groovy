// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

 /**
 * @author nik
 */
public class SourceRootUnderOutputTest extends JpsBuildTestCase {
  public void test() throws Exception {
    Project project = loadProject("testData/sourceFolderUnderOutput/sourceFolderUnderOutput.ipr", [:])
    try {
      createBuilder(project).clean()
      fail("Cleaning should fail")
    }
    catch (Exception e) {
    }
  }
}
