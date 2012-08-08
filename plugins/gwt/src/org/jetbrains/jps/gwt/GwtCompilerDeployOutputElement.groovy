package org.jetbrains.jps.gwt
import org.jetbrains.jps.Project
import org.jetbrains.jps.artifacts.ComplexLayoutElement
import org.jetbrains.jps.artifacts.LayoutElement
/**
 * @author nik
 */
class GwtCompilerDeployOutputElement extends ComplexLayoutElement {
  String facetId

  @Override
  List<LayoutElement> getSubstitution(Project project) {
    return []
  }
}
