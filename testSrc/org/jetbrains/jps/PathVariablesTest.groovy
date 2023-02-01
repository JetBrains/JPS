// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

/**
 * @author nik
 */
class PathVariablesTest extends JpsBuildTestCase {
  public void testInArtifact() throws Exception {
    doTest("testData/pathVariables/pathVariables.ipr", ['EXTERNAL_DIR':'testData/pathVariables/external'], null, {
      dir("artifacts") {
        dir("fileCopy") {
          dir("dir") {
            file("file.txt", "xxx")
          }
        }
      }
    })
  }
}
