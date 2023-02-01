// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.artifacts

/**
 * @author nik
 */
class LayoutElementFactory {

  static LayoutElement createParentDirectories(String path, LayoutElement element) {
    def result = element
    path.split("/").reverseEach {
      if (it != "") {
        result = new DirectoryElement(it, [result])
      }
    }
    return result
  }

}
