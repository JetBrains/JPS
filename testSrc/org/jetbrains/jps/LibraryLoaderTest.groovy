package org.jetbrains.jps

/**
 * @author natalia.ukhorskaya
 */
class LibraryLoaderTest extends JpsBuildTestCase {
    public void testLoadLibrary() {
        Project project = loadProject("testData/libraryTest/libraryTest.ipr", [:]);
        assertEquals(1, project.libraries.size());
        def library = project.libraries["test"]
        assertNotNull(library);
        assertEquals(1, library.annotationRoots.size());
        String annotationRoot = library.annotationRoots.get(0)
        assertTrue(annotationRoot.endsWith("/libraryTest/lib"))
    }
}
