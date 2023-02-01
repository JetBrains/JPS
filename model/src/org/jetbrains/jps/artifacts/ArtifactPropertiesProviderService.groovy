// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.artifacts

import org.jetbrains.jps.MacroExpander

/**
 * @author nik
 */
abstract class ArtifactPropertiesProviderService<P extends ArtifactProperties> {
  final String id

  ArtifactPropertiesProviderService(String id) {
    this.id = id
  }

  abstract P loadProperties(Node node, MacroExpander macroExpander)
}
