// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.gwt

import org.jetbrains.jps.builders.ModuleBuilderService
import org.jetbrains.jps.ProjectBuilder

/**
 * @author nik
 */
class GwtModuleBuilderService extends ModuleBuilderService {
  @Override
  registerBuilders(ProjectBuilder builder) {
    builder.weavingBuilders << new GwtModuleBuilder()
  }

}
