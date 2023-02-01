// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.jpa

import org.jetbrains.jps.MacroExpander
import org.jetbrains.jps.Project
import org.jetbrains.jps.artifacts.LayoutElement
import org.jetbrains.jps.artifacts.LayoutElementTypeService
import org.jetbrains.jps.idea.ProjectLoadingErrorReporter

/**
 * @author nik
 */
class JpaFacetDescriptorsElementType extends LayoutElementTypeService {
  JpaFacetDescriptorsElementType() {
    super("jpa-descriptors")
  }

  @Override
  LayoutElement createElement(Project project, Node tag, MacroExpander macroExpander, ProjectLoadingErrorReporter errorReporter) {
    return new JpaFacetDescriptorsElement(facetId: tag."@facet", errorReporter: errorReporter)
  }
}
