package org.jetbrains.jps;

import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JDomUtil {
  public static String getCanonicalPath(String s) {
    final File file = new File(s);
    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      return file.getAbsolutePath();
    }
  }

  public static String getAttribute(Element node, String name) {
    if (node == null) return null;
    return node.getAttributeValue(name);
  }

  public static Element getFirstChildren(Element node, String name) {
    if (node == null) return null;
    return node.getChild(name);
  }

  public static List<Element> getChildren(Element node, String name) {
    if (node == null) return Collections.emptyList();
    return node.getChildren(name);
  }
}
