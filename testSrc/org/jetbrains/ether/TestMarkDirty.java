// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.ether;

/**
 * Created by IntelliJ IDEA.
 * User: db
 * Date: 04.10.11
 * Time: 14:41
 * To change this template use File | Settings | File Templates.
 */
public class TestMarkDirty extends IncrementalTestCase {
    public TestMarkDirty() throws Exception {
        super("markDirty");
    }

    public void testRecompileDependent () throws Exception {
        doTest();
    }
}
