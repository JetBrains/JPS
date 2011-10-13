package org.jetbrains.jps

/**
 * @author nik
 */
class GroovyTest extends JpsBuildTestCase {
  public void test() throws Exception {
    doTest("testData/groovyTest/groovyTest.ipr", {Project project ->
      project.createGlobalLibrary("groovy") {
        classpath "lib/groovy-all-1.7.1.jar"
      }
    }, {
        dir("production") {
          dir("groovyTest") {
            file("MyClass.class")
            file("GrClass.class")
          }
          dir("dep") {
            file("MyDep.class")
            file("GrDep.class")
          }
        }
    })
  }

}
