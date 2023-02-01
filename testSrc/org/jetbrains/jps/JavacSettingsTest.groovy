// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

class JavacSettingsTest extends JpsBuildTestCase {
  public void testLoadJavacSettings() throws Exception {
    Project project = loadProject("testData/resourceCopying/resourceCopying.ipr", [:]);
    Map<String, String> options = project.compilerConfiguration.javacOptions
    assertNotNull(options)
    assertEquals("512", options["MAXIMUM_HEAP_SIZE"])
    assertEquals("false", options["DEBUGGING_INFO"])
    assertEquals("true", options["GENERATE_NO_WARNINGS"])
  }
}
