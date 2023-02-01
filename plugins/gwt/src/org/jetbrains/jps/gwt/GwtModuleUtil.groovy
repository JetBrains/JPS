// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.gwt

import org.xml.sax.SAXParseException;

/**
 * @author nik
 */
class GwtModuleUtil {
  public static boolean hasEntryPoints(File child) {
    try {
      def root = new XmlParser(false, false).parse(child)
      return !root."entry-point".isEmpty()
    }
    catch (IOException e) {
      return true;
    }
    catch (SAXParseException e) {
      return true;
    }
  }
}
