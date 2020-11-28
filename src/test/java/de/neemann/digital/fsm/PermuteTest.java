/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.TreeSet;

import static de.neemann.digital.fsm.Optimizer.fac;
import static de.neemann.digital.fsm.Permute.permute;

public class PermuteTest extends TestCase {

    public void testPermuteSimple() throws Permute.PermListenerException {
        for (int i = 3; i <= 7; i++) {
            TestListener testlistener = new TestListener();
            permute(i, testlistener);
            assertEquals("i=" + i, fac(i), testlistener.set.size());
        }
    }

    public void testPermuteRange() throws Permute.PermListenerException {
        for (int r = 1; r <= 3; r++)
            for (int i = 3; i <= 6; i++) {
                TestListener testlistener = new TestListener();
                permute(i, i + r, testlistener);
                assertEquals("i=" + i + ", r=" + r, fac(i + r) / fac(r), testlistener.set.size());
            }
    }


    public void testPull() {
        Permute.PermPull pp = new Permute.PermPull(4, 4);

        System.out.println(Arrays.toString(pp.next()));
        pp.stop();

        int[] p;
        while ((p = pp.next()) != null)
            System.out.println(Arrays.toString(p));
    }

    private static class TestListener implements Permute.PermListener {
        private final TreeSet<Perm> set = new TreeSet<>();

        @Override
        public void perm(int[] perm) {
            Perm p = new Perm(perm);
            assertFalse(set.contains(p));
            set.add(p);
        }
    }

    private static class Perm implements Comparable<Perm> {
        private final int[] perm;

        public Perm(int[] perm) {
            this.perm = Arrays.copyOf(perm, perm.length);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Perm perm1 = (Perm) o;
            return Arrays.equals(perm, perm1.perm);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(perm);
        }

        @Override
        public String toString() {
            return "Perm{" +
                    "perm=" + Arrays.toString(perm) +
                    '}';
        }

        @Override
        public int compareTo(Perm other) {
            for (int i = 0; i < perm.length; i++) {
                int r = Integer.compare(this.perm[i], other.perm[i]);
                if (r != 0)
                    return r;
            }
            return 0;
        }
    }

}