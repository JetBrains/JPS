// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

/**
 * @author nik
 */
class JpaTest extends JpsBuildTestCase {
  public void testOverwriteArtifacts() throws Exception {
    doTest("testData/jpaTest/jpaTest.ipr", null, {
      dir("artifacts") {
        dir("jpaTest") {
          dir("WEB-INF") {
            dir("classes") {
              dir("META-INF") {
                file("persistence.xml")
              }
            }
          }
        }
      }
      dir("production") {
        dir("jpaTest") {
          dir("META-INF") {
            file("persistence.xml")
          }
        }
      }
    })
  }
}
