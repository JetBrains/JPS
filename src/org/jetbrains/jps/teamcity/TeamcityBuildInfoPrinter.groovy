// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.teamcity

import org.jetbrains.jps.listeners.BuildInfoPrinter

import org.jetbrains.jps.ProjectBuilder

/**
 * @author nik
 */
class TeamcityBuildInfoPrinter implements BuildInfoPrinter {
  def escapedChar(char c) {
    switch (c) {
      case '\n': return 'n';
      case '\r': return 'r';
      case '\u0085': return 'x'; // next-line character
      case '\u2028': return 'l'; // line-separator character
      case '\u2029': return 'p'; // paragraph-separator character
      case '|': return '|';
      case '\'': return '\'';
      case '[': return '[';
      case ']': return ']';
    }
    
    return 0;
  }

  def escape(String text) {
    StringBuilder escaped = new StringBuilder();
    for (char c: text.toCharArray()) {
      Character escChar = escapedChar(c);
      if (escChar == 0) {
        escaped.append(c);
      } else {
        escaped.append('|').append(escChar);
      }
    }

    return escaped.toString();
  }

  def printProgressMessage(ProjectBuilder projectBuilder, String message) {
    def escapedMessage = escape(message)
    projectBuilder.info("##teamcity[progressMessage '$escapedMessage']");
  }

  def printCompilationErrors(ProjectBuilder projectBuilder, String compilerName, String messages) {
    def escapedCompiler = escape(compilerName)
    def escapedOutput = escape(messages)
    projectBuilder.info("##teamcity[compilationStarted compiler='$escapedCompiler']");
    projectBuilder.info("##teamcity[message text='$escapedOutput' status='ERROR']");
    projectBuilder.info("##teamcity[compilationFinished compiler='$escapedCompiler']");
  }
}
