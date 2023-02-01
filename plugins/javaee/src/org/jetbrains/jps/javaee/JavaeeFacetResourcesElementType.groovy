// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.javaee

import org.jetbrains.jps.MacroExpander
import org.jetbrains.jps.Project
import org.jetbrains.jps.artifacts.LayoutElement
import org.jetbrains.jps.artifacts.LayoutElementTypeService
import org.jetbrains.jps.idea.ProjectLoadingErrorReporter

/**
 * @author nik
 */
class JavaeeFacetResourcesElementType extends LayoutElementTypeService {
  JavaeeFacetResourcesElementType() {
    super("javaee-facet-resources")
  }

  @Override
  LayoutElement createElement(Project project, Node tag, MacroExpander macroExpander, ProjectLoadingErrorReporter errorReporter) {
    return new JavaeeFacetResourcesElement(facetId: tag."@facet", errorReporter: errorReporter)
  }

}
