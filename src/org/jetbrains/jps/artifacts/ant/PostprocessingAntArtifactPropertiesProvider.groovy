// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.artifacts.ant

/**
 * @author nik
 */
class PostprocessingAntArtifactPropertiesProvider extends AntArtifactPropertiesProvider {
  public static final String ID = "ant-postprocessing"

  PostprocessingAntArtifactPropertiesProvider() {
    super(ID)
  }
}
