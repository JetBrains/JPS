package org.jetbrains.jps.gwt

import org.jetbrains.jps.XmlUtil
import org.xml.sax.SAXParseException

/**
 * @author nik
 */
class GwtModuleUtil {
  public static boolean hasEntryPoints(File child) {
    try {
      def root = XmlUtil.createNonValidatingXmlParser().parse(child)
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
