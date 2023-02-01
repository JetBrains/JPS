// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.ether;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: 05.10.11
 * Time: 20:43
 * To change this template use File | Settings | File Templates.
 */
public class TestMethodProperties extends IncrementalTestCase {
    public TestMethodProperties() throws Exception {
        super("methodProperties");
    }

    public void testAddThrows() throws Exception {
        doTest();
    }

    public void testChangeReturnType() throws Exception {
        doTest();
    }

    public void testChangeReturnType1() throws Exception {
        doTest();
    }

    public void testChangeSignature() throws Exception {
        doTest();
    }

    public void testChangeSignature1() throws Exception {
        doTest();
    }
}
