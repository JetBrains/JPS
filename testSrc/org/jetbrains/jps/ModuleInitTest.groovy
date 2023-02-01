// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

class ModuleInitTest extends JpsBuildTestCase {
  public void testBasePath() {
    Project project = loadProject("testData/moduleCycle/moduleCycle.ipr", [:]);
    for (def name: ['module1', 'module2']) {
      assertTrue(project.modules[name].basePath.endsWith(File.separator + name));
    }
  }
}
