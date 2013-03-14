package org.jetbrains.jps.idea

import org.jetbrains.jps.Project

/**
 * @author nik
 */
public class IdeaProjectLoadingUtil {
  static Facet findFacetByIdWithAssertion(Project project, String facetId) {
    Facet facet = findFacetById(project, facetId)
    if (facet == null) {
      project.error("Facet not found: id=$facetId")
    }
    return facet
  }

  static findFacetById(Project project, String facetId) {
    def moduleName = facetId.substring(0, facetId.indexOf('/'))
    def facet = project.modules[moduleName]?.facets[facetId]
    return facet
  }
}
