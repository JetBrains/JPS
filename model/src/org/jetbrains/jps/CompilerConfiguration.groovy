// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps

/**
 * @author nik
 */
class CompilerConfiguration {
  List<String> resourcePatterns = []
  List<String> resourceIncludePatterns = "properties,xml,gif,png,jpeg,jpg,jtml,dtd,tld,ftl".split(",").collect {"**/?*.$it"}
  List<String> resourceExcludePatterns = []
  Map<String, String> javacOptions = [:]

}
