package org.jetbrains.jps.idea

/**
 * @author nik
 */
public class IdeaPathUtil {
  static String pathFromUrl(String url) {
    if (url == null) return null
    if (url.startsWith("file://")) {
      return url.substring("file://".length())
    }
    else if (url.startsWith("jar://")) {
      url = url.substring("jar://".length())
      if (url.endsWith("!/"))
        url = url.substring(0, url.length() - "!/".length())
    }
    url
  }
}
