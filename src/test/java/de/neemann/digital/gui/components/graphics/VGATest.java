/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.graphics;

import junit.framework.TestCase;

public class VGATest extends TestCase {

    private static final class TestData {
        private int high;
        private int low;
        private boolean neg;
        private int sync;
        private boolean rising;
        private boolean falling;

        private TestData(int high, int low, boolean neg, int sync, boolean rising, boolean falling) {
            this.high = high;
            this.low = low;
            this.neg = neg;
            this.sync = sync;
            this.rising = rising;
            this.falling = falling;
        }
    }

    private static final TestData[] TEST_DATA = new TestData[]{
            new TestData(100, 10, true, 10, true, false),
            new TestData(10, 100, false, 10, false, true),
    };

    public void testSyncDetector() {
        for (TestData td : TEST_DATA) {
            VGA.SyncDetector sd = new VGA.SyncDetector();
            boolean risingEdge = false;
            boolean fallingEdge = false;
            for (int n = 0; n < 3; n++) {
                risingEdge = sd.add(true);
                for (int i = 0; i < td.high - 1; i++)
                    sd.add(true);
                fallingEdge = sd.add(false);
                for (int i = 0; i < td.low - 1; i++)
                    sd.add(false);
            }

            assertEquals(td.neg, sd.isNegPolarity());
            assertEquals(td.sync, sd.syncPulse());
            assertEquals(td.rising, risingEdge);
            assertEquals(td.falling, fallingEdge);
        }
    }

}