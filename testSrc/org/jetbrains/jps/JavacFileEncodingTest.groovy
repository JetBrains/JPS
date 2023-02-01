// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

/**
 * @author nik
 */
class JavacFileEncodingTest extends JpsBuildTestCase {
  public void test() {
    doTest("testData/javacFileEncoding/javacFileEncoding.ipr", null, {
      dir("production") {
        dir("javacFileEncoding") {
          file("MyClass.class")
        }
      }
    })
  }
}
