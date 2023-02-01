// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package jetbrains.antlayout.datatypes;

import jetbrains.antlayout.util.TempFileFactory;
import jetbrains.antlayout.util.LayoutFileSet;

import java.io.File;
import java.util.List;
import java.util.Collections;

import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.BuildException;

/**
 * @author max
 */
public class RootContainer extends Container {
    private File destDirectory;

    public RootContainer(File destDirectory) {
        this.destDirectory = destDirectory;
    }


    public List<LayoutFileSet> build(TempFileFactory temp) {
        List<LayoutFileSet> built = super.build(temp);
        for (LayoutFileSet set : built) {
            copySet(set);
        }

        return Collections.emptyList();
    }

    private void copySet(LayoutFileSet set) {
        Copy task = new Copy();
        task.setTaskName("copy");
        task.setProject(getProject());
        String prefix = set.getPrefix(getProject());
        File target = prefix.length() > 0 ? new File(destDirectory, prefix + "/") : destDirectory;

        target.mkdirs();

        task.setTodir(target);
        LayoutFileSet unprefixed = (LayoutFileSet) set.clone();
        unprefixed.setPrefix("");

        task.addFileset(unprefixed);
        task.perform();
    }

    public void validateArguments() throws BuildException {
        super.validateArguments();        
    }
}
