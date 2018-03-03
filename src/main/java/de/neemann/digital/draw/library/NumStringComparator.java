/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import java.util.Comparator;

import static java.lang.Character.isDigit;

/**
 * String comparator.
 * If the string contains a digit, the numbers are taken to compare the two strings.
 * Used to ensure the 74xx components appear in the correct numerical order instead of lexical order.
 */
public final class NumStringComparator implements Comparator<String> {

    private static final class InstanceHolder {
        private static final NumStringComparator INSTANCE = new NumStringComparator();
    }

    /**
     * Returns a comparator instance
     *
     * @return the singleton instance
     */
    public static NumStringComparator getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private NumStringComparator() {
    }

    @Override
    public int compare(String a, String b) {
        return compareStr(a, b);
    }

    /**
     * Compare two strings
     *
     * @param a a string
     * @param b a string
     * @return the comparison result
     */
    public static int compareStr(String a, String b) {
        int pa = 0;
        int pb = 0;
        while (true) {
            final boolean ae = pa == a.length();
            final boolean be = pb == b.length();
            if (ae && be) return 0;
            else if (ae) return -1;
            else if (be) return 1;

            char ca = Character.toLowerCase(a.charAt(pa));
            char cb = Character.toLowerCase(b.charAt(pb));

            if (isDigit(ca) && isDigit(cb)) {
                ParseNumber da = new ParseNumber(a, pa);
                ParseNumber db = new ParseNumber(b, pb);
                int c = Integer.compare(da.num, db.num);
                if (c != 0)
                    return c;
                else {
                    pa = da.p;
                    pb = db.p;
                }
            } else {
                int c = Character.compare(ca, cb);
                if (c != 0) {
                    return c;
                } else {
                    pa++;
                    pb++;
                }
            }
        }
    }

    private static final class ParseNumber {
        private int num;
        private int p;

        private ParseNumber(String a, int sp) {
            p = sp;
            while (p < a.length() && isDigit(a.charAt(p))) {
                num = num * 10 + (a.charAt(p) - '0');
                p++;
            }
        }
    }

}
