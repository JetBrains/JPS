// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.idea

import org.jetbrains.jps.MacroExpander
import org.jetbrains.jps.Module

/**
 * @author nik
 */
public abstract class FacetTypeService {
  final String typeId

  FacetTypeService(String typeId) {
    this.typeId = typeId
  }

  public abstract Facet createFacet(Module module, String name, Node facetConfiguration, MacroExpander macroExpander)

}