// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

import org.jetbrains.jps.util.FileUtil

 /**
 * @author nik
 */
public class CleanArchiveArtifactTest extends JpsBuildTestCase {
  public void test() throws Exception {
    def outDir = FileUtil.createTempDirectory("output").absolutePath
    Project project = loadProject("testData/cleanArchiveArtifact/cleanArchiveArtifact.ipr", ["OUTPUT_DIR":outDir])
    ProjectBuilder builder = createBuilder(project)
    builder.tempFolder = FileUtil.createTempDirectory("tmp").absolutePath
    builder.clean()
    builder.buildArtifact("jar")
    builder.deleteTempFiles()
    assertOutput(outDir, {
      archive("jar.jar") {
        dir("META-INF") {
          file("MANIFEST.MF")
        }
        file("a.txt")
      }
    })

    builder.clean()
    assertOutput(outDir, {
    })
  }
}
