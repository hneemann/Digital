/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 */
public class OrderMergerTest extends TestCase {
    public void testOrder() throws Exception {
        ArrayList<String> oldList = new ArrayList<>();
        oldList.add("b");
        oldList.add("d");

        ArrayList<String> newList = new ArrayList<>();
        newList.add("a");
        newList.add("b");
        newList.add("c");
        newList.add("d");

        new OrderMerger<String, String>(oldList).order(newList);

        assertEquals(4, newList.size());
        assertEquals("b", newList.get(0));
        assertEquals("d", newList.get(1));
        assertEquals("a", newList.get(2));
        assertEquals("c", newList.get(3));
    }

}
