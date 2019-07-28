/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import junit.framework.TestCase;

public class LabelNumberingTest extends TestCase {

    public void testCreate() {
        assertEquals("test", new LabelNumbering("test").create(name -> false));
        assertEquals("test2", new LabelNumbering("test").create(new TestCheck()));
        assertEquals("test2n", new LabelNumbering("testn").create(new TestCheck()));
        assertEquals("test_2^n", new LabelNumbering("test^n").create(new TestCheck()));
        assertEquals("test_2n", new LabelNumbering("test_n").create(new TestCheck()));
    }

    private class TestCheck implements LabelNumbering.Exists {
        private int n;

        @Override
        public boolean exits(String name) {
            return n++ < 2;
        }
    }
}