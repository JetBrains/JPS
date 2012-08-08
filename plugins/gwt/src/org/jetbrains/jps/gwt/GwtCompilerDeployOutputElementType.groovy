package org.jetbrains.jps.gwt

import org.jetbrains.jps.MacroExpander
import org.jetbrains.jps.Project
import org.jetbrains.jps.artifacts.LayoutElement
import org.jetbrains.jps.artifacts.LayoutElementTypeService

 /**
 * @author nik
 */
public class GwtCompilerDeployOutputElementType extends LayoutElementTypeService {
  public GwtCompilerDeployOutputElementType() {
    super("gwt-compiler-deploy-output")
  }

  @Override
  public LayoutElement createElement(Project project, Node tag, MacroExpander macroExpander) {
    return new GwtCompilerDeployOutputElement(facetId: tag."@facet")
  }
}
