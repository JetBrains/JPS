// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.incremental.java;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Eugene Zhuravlev
 *         Date: 9/24/11
 */
class JavacFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

  private final Context myContext;
  private Map<File, Set<File>> myOutputsMap = Collections.emptyMap();

  static interface Context {
    StandardJavaFileManager getStandardFileManager();

    void consumeOutputFile(OutputFileObject obj);

    void reportMessage(final Diagnostic.Kind kind, String message);

    void ensurePendingTasksCompleted();
  }

  public JavacFileManager(Context context) {
    super(context.getStandardFileManager());
    myContext = context;
  }

// todo: check if reading source files can be optimized

  public boolean setOutputDirectories(final Map<File, Set<File>> outputDirToSrcRoots) {
    for (File outputDir : outputDirToSrcRoots.keySet()) {
      // this will validate output dirs
      if (!setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(outputDir))) {
        return false;
      }
    }
    myOutputsMap = outputDirToSrcRoots;
    return true;
  }

  public boolean setLocation(Location location, Iterable<? extends File> path) {
    try {
      getStdManager().setLocation(location, path);
    }
    catch (IOException e) {
      myContext.reportMessage(Diagnostic.Kind.ERROR, e.getMessage());
      return false;
    }
    return true;
  }

  public boolean isSameFile(FileObject a, FileObject b) {
    if (a instanceof OutputFileObject && b instanceof OutputFileObject) {
      return a.equals(b);
    }
    return super.isSameFile(a, b);
  }

  public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
    return super.list(location, packageName, kinds, recurse);
  }

  public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
    if (kind != JavaFileObject.Kind.SOURCE && kind != JavaFileObject.Kind.CLASS) {
      throw new IllegalArgumentException("Invalid kind " + kind);
    }
    return getFileForOutput(location, kind, externalizeFileName(className, kind), className, sibling);
  }

  public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
    final StringBuilder name = new StringBuilder();
    if (packageName.isEmpty()) {
      name.append(relativeName);
    }
    else {
      name.append(externalizeFileName(packageName)).append(File.separatorChar).append(relativeName);
    }
    final String fileName = name.toString();
    return getFileForOutput(location, getKind(fileName), fileName, null, sibling);
  }

  private OutputFileObject getFileForOutput(Location location, JavaFileObject.Kind kind, String fileName, @Nullable String className, FileObject sibling) throws IOException {
    JavaFileObject src = null;
    if (sibling instanceof JavaFileObject) {
      final JavaFileObject javaFileObject = (JavaFileObject)sibling;
      if (javaFileObject.getKind() == JavaFileObject.Kind.SOURCE) {
        src = javaFileObject;
      }
    }

    File dir = getSingleOutputDirectory(location, src);

    if (location == StandardLocation.CLASS_OUTPUT) {
      if (dir == null) {
        throw new IOException("Output directory is not specified");
      }
    }
    else if (location == StandardLocation.SOURCE_OUTPUT) {
      if (dir == null) {
        dir = getSingleOutputDirectory(StandardLocation.CLASS_OUTPUT, src);
        if (dir == null) {
          throw new IOException("Neither class output directory nor source output are specified");
        }
      }
    }
    final File file = (dir == null? new File(fileName) : new File(dir, fileName));
    return new OutputFileObject(myContext, file, kind, className, src);
  }

  private File getSingleOutputDirectory(final Location loc, final JavaFileObject sourceFile) {
    if (loc == StandardLocation.CLASS_OUTPUT) {
      if (myOutputsMap.size() > 1 && sourceFile != null) {
        // multiple outputs case
        final File outputDir = findOutputDir(new File(sourceFile.toUri()));
        if (outputDir != null) {
          return outputDir;
        }
      }
    }

    final Iterable<? extends File> location = getStdManager().getLocation(loc);
    if (location != null) {
      final Iterator<? extends File> it = location.iterator();
      if (it.hasNext()) {
        return it.next();
      }
    }
    return null;
  }

  private File findOutputDir(File src) {
    File file = src.getParentFile();
    while (file != null) {
      for (Map.Entry<File, Set<File>> entry : myOutputsMap.entrySet()) {
        if (entry.getValue().contains(file)) {
          return entry.getKey();
        }
      }
      file = file.getParentFile();
    }
    return null;
  }

  @NotNull
  private StandardJavaFileManager getStdManager() {
    return fileManager;
  }

  public Iterable<? extends JavaFileObject> toJavaFileObjects(Iterable<? extends File> files) {
    return getStdManager().getJavaFileObjectsFromFiles(files);
  }

  public void cleanupResources() {
    try {
      fileManager.close();
    }
    catch (IOException e) {
      e.printStackTrace(); // todo
    }
  }

  private static URI toURI(String outputDir, String name, JavaFileObject.Kind kind) {
    return createUri("file:///" + outputDir.replace('\\','/') + "/" + name.replace('.', '/') + kind.extension);
  }

  private static URI createUri(String url) {
    return URI.create(url.replaceAll(" ","%20"));
  }

  private static JavaFileObject.Kind getKind(String name) {
    if (name.endsWith(JavaFileObject.Kind.CLASS.extension)){
      return JavaFileObject.Kind.CLASS;
    }
    if (name.endsWith(JavaFileObject.Kind.SOURCE.extension)) {
      return JavaFileObject.Kind.SOURCE;
    }
    if (name.endsWith(JavaFileObject.Kind.HTML.extension)) {
      return JavaFileObject.Kind.HTML;
    }
    return JavaFileObject.Kind.OTHER;
  }

  private static String externalizeFileName(CharSequence cs, JavaFileObject.Kind kind) {
    return externalizeFileName(cs) + kind.extension;
  }

  private static String externalizeFileName(CharSequence name) {
    return name.toString().replace('.', File.separatorChar);
  }

}
