// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.artifacts.ant

import org.jetbrains.jps.artifacts.ArtifactProperties

/**
 * @author nik
 */
class AntArtifactProperties implements ArtifactProperties {
  boolean enabled
  String filePath
  String target
  List<List<String>> buildProperties
}
