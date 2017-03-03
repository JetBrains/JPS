package org.jetbrains.jps;

import groovy.util.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GroovyUtil {
  public static String getCanonicalPath(String s) {
    final File file = new File(s);
    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      return file.getAbsolutePath();
    }
  }

  public static String getAttribute(Node node, String name) {
    if (node == null) return null;
    final Object attribute = node.attribute(name);
    if (attribute != null) return attribute.toString();
    return null;
  }

  public static Node getFirstChildren(Node node, String name) {
    if (node == null) return null;
    final Iterator<Node> iterator = getChildren(node, name).iterator();
    if (iterator.hasNext()) return iterator.next();
    return null;
  }

  public static List<Node> getChildren(Node node, String name) {
    if (node == null) return Collections.emptyList();
    final ArrayList<Node> result = new ArrayList<Node>();
    for (Object child : node.children()) {
      if (!(child instanceof Node)) {
        continue;
      }
      final Node n = (Node) child;
      if (!name.equals(n.name())) continue;
      result.add(n);
    }
    return result;
  }
}
