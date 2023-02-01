// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.server;

import java.util.List;

/**
 * @author Eugene Zhuravlev
 *         Date: 10/4/11
 */
public class SdkLibrary extends GlobalLibrary{

  private final String myHomePath;

  public SdkLibrary(String name, String homePath, List<String> paths) {
    super(name, paths);
    myHomePath = homePath;
  }

  public String getHomePath() {
    return myHomePath;
  }
}
