package org.jetbrains.jps

class JavacSettingsTest extends JpsBuildTestCase {
  public void test_javac_settings() throws Exception {
    Project project = loadProject("testData/resourceCopying/resourceCopying.ipr", [:]);
    assertNotNull(project.props["compiler.javac.options"])
    assertEquals("512", project.props["compiler.javac.options"]["MAXIMUM_HEAP_SIZE"])
    assertEquals("false", project.props["compiler.javac.options"]["DEBUGGING_INFO"])
    assertEquals("true", project.props["compiler.javac.options"]["GENERATE_NO_WARNINGS"])
  }

  public void test_compiler_excludes() throws Exception {
    def projectPath = "testData/javacExcludes/javacExcludes.ipr";
    Project project = loadProject(projectPath, [:]);
    
    assertNotNull(project.props["compiler.excludes"])
    def excludes = project.props["compiler.excludes"];
    assertFalse(excludes.isEmpty())
    
    for (String expected: ["src/MyClass2.java", "src/MyClass3.java", "src/pkg2/**"]) {
      def found = false;
      for (String exclude: excludes) {
        if (exclude.endsWith(expected)) {
          found = true;
          break;
        }
      }

      assertTrue("Expected: " + expected + ", actual: " + excludes.toString(), found);
    }
  }

  public void test_javac_excludes_classes() throws Exception {
    Project project = loadProject("testData/javacExcludes/javacExcludes.ipr", [:]);
    project.clean();
    project.makeAll(); // must compile without errors
  }
}
