// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

/**
 * @author nik
 */
class ModuleTestOutputElementTest extends JpsBuildTestCase {
  public void test() {
    doTest("testData/moduleTestOutput/moduleTestOutput.ipr", null, {
      dir("artifacts") {
        dir("tests") {
          file("MyTest.class")
        }
      }
      dir("production") {
        dir("moduleTestOutput") {
          file("MyClass.class")
        }
      }
      dir("test") {
        dir("moduleTestOutput") {
          file("MyTest.class")
        }
      }
    })
  }
}
