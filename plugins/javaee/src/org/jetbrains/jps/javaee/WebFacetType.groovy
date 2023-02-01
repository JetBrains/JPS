// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.javaee

/**
 * @author nik
 */
public class WebFacetType extends JavaeeFacetTypeBase {
  public WebFacetType() {
    super("web");
  }

  @Override
  protected String getDescriptorOutputPath(String descriptorId) {
    if (descriptorId == "context.xml") return "META-INF"
    return "WEB-INF"
  }


}
