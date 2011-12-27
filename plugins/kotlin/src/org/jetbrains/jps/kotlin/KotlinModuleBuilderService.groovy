package org.jetbrains.jps.kotlin

import org.jetbrains.jps.builders.ModuleBuilderService
import org.jetbrains.jps.ProjectBuilder

/**
 * @author max
 */
class KotlinModuleBuilderService extends ModuleBuilderService {
  @Override
  registerBuilders(ProjectBuilder builder) {
    builder.sourceModifyingBuilders << new KotlinModuleBuilder();
  }
}
