// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps;

import groovy.lang.MissingPropertyException;
import groovy.util.Expando;

/**
 * @author max
 */
class InitializingExpando extends Expando {
  public Object getProperty(String property) {
    Object result = super.getProperty(property);
    if (result == null) throw new MissingPropertyException(property, getClass());
    return result;
  }
}
