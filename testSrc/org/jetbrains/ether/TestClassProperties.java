// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.ether;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: 09.08.11
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class TestClassProperties extends IncrementalTestCase {
    public TestClassProperties() throws Exception {
        super("classProperties");
    }

    public void testAddExtends () throws Exception {
        doTest();
    }

    public void testAddImplements () throws Exception {
        doTest();
    }

     public void testChangeExtends () throws Exception {
        doTest();
    }

    public void testRemoveExtends () throws Exception {
        doTest();
    }

    public void testRemoveImplements () throws Exception {
        doTest();
    }

    public void testRemoveImplements2 () throws Exception {
        doTest();
    }

    public void testRemoveImplements3 () throws Exception {
        doTest();
    }
}
