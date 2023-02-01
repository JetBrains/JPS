// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.ether;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: 09.08.11
 * Time: 12:57
 * To change this template use File | Settings | File Templates.
 */
public class TestClassRename extends IncrementalTestCase {
    public TestClassRename() throws Exception {
        super("changeName");
    }

    public void testChangeClassName() throws Exception {
        doTest();
    }
}
