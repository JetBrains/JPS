// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.ether;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: 22.09.11
 * Time: 3:13
 * To change this template use File | Settings | File Templates.
 */
public class TestCommon extends IncrementalTestCase {
    public TestCommon() throws Exception {
        super("common");
    }

    public void testAnonymous() throws Exception {
        doTest();
    }

    public void testChangeDefinitionToClass() throws Exception {
        doTest();
    }

    public void testChangeDefinitionToClass2() throws Exception {
        doTest();
    }

    public void testDeleteClass() throws Exception {
        doTest();
    }

    public void testDeleteClass1() throws Exception {
        doTest();
    }

    public void testDeleteClass2() throws Exception {
        doTest();
    }

    public void testDeleteClassPackageDoesntMatchRoot() throws Exception {
        doTest();
    }

    public void testInner() throws Exception {
        doTest();
    }

    public void testNoResourceDelete() throws Exception {
        doTest();
    }

    public void testNoSecondFileCompile() throws Exception {
        doTest();
    }

    public void testNoSecondFileCompile1() throws Exception {
        doTest();
    }
}
