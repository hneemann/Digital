package de.neemann.digital.draw.library;

import junit.framework.TestCase;

/**
 * Created by hneemann on 15.05.17.
 */
public class NumStringComparatorTest extends TestCase {

    public void testSimple() {
        checkLess("a", "b");
        checkLess("12a", "b");
        checkLess("2a", "12b");
        checkLess("2a", "2b");
        checkLess("2a", "02b");
        checkLess(" 2a", "02b");
        checkLess("2a", "2B");
    }

    private void checkLess(String a, String b) {
        assertTrue(NumStringComparator.compareStr(a, b) < 0);
        assertTrue(NumStringComparator.compareStr(b, a) > 0);
        assertTrue(NumStringComparator.compareStr(a, a) == 0);
        assertTrue(NumStringComparator.compareStr(b, b) == 0);
    }

}