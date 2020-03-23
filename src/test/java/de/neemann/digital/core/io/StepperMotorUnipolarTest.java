/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import junit.framework.TestCase;

public class StepperMotorUnipolarTest extends TestCase {

    public void testTheTest() {
        assertEquals(0b1000, rotRight(0b0001));
        assertEquals(0b0010, rotRight(0b0100));
        assertEquals(0b1001, rotRight(0b0011));
        assertEquals(0b1100, rotRight(0b1001));
    }

    public int rotRight(int p) {
        int pp = p >> 1;
        if ((p & 1) != 0) {
            pp |= 8;
        }
        return pp;
    }

    private int[][] phase = new int[16][16];

    // This is is not a test!
    // It's used to create the step-table
    public void testCreateIncrement() {
        // wave drive
        fill(0b0001, 0b0010, 2);
        // full step
        fill(0b0011, 0b0110, 2);
        // half step
        fill(0b0001, 0b0011, 1);
        fill(0b0011, 0b0010, 1);

        for (int i = 0; i < 16; i++) {
            System.out.print('{');
            for (int j = 0; j < 16; j++) {
                if (j != 0)
                    System.out.print(",");
                System.out.print(phase[i][j]);
            }
            System.out.println("},");
        }

    }

    void fill(int pp, int pn, int step) {
        for (int i = 0; i < 4; i++) {
            phase[pp][pn] = step;
            phase[pn][pp] = -step;
            pp = rotRight(pp);
            pn = rotRight(pn);
        }
    }

}