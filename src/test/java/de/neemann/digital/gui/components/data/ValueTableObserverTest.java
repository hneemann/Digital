/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Signal;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.util.ArrayList;

public class ValueTableObserverTest extends TestCase {

    private static final class Case {
        private final String name;
        private final boolean microStep;
        private final int clocks;
        private final int expectedSize;
        private final String expectedData;

        public Case(String name, boolean microStep, int clocks, int expectedSize, String expectedData) {
            this.name = name;
            this.microStep = microStep;
            this.clocks = clocks;
            this.expectedSize = expectedSize;
            this.expectedData = expectedData;
        }
    }

    private final Case[] cases = new Case[]{
            new Case("dig/graph/simple.dig", false, 4, 4,
                    "C Y X Z\n" +
                            "1 1 0 1\n" +
                            "0 0 1 0\n" +
                            "1 1 0 1\n" +
                            "0 0 1 0\n"),
            new Case("dig/graph/simple.dig", true, 4, 12,
                    "C Y X Z\n" +
                            "1 1 1 0\n" +
                            "1 1 0 0\n" +
                            "1 1 0 1\n" +
                            "0 0 0 1\n" +
                            "0 0 1 1\n" +
                            "0 0 1 0\n" +
                            "1 1 1 0\n" +
                            "1 1 0 0\n" +
                            "1 1 0 1\n" +
                            "0 0 0 1\n" +
                            "0 0 1 1\n" +
                            "0 0 1 0\n"),
            new Case("dig/graph/simple2.dig", false, 4, 4,
                    "C Y\n" +
                            "1 1\n" +
                            "0 0\n" +
                            "1 1\n" +
                            "0 0\n"),
            new Case("dig/graph/simple2.dig", true, 4, 4,
                    "C Y\n" +
                            "1 1\n" +
                            "0 0\n" +
                            "1 1\n" +
                            "0 0\n"),
    };


    public void testTable() throws Exception {
        for (Case c : cases) {
            Model m = new ToBreakRunner(c.name).getModel();
            ArrayList<Signal> list = m.getSignals();
            ValueTableObserver o = new ValueTableObserver(c.microStep, list, 100);
            m.addObserver(o);

            ObservableValue clock = m.getClocks().get(0).getClockOutput();
            for (int i = 0; i < c.clocks; i++)
                m.modify(() -> clock.setBool(!clock.getBool()));

            ValueTable logData = o.getLogData();
            assertEquals(c.expectedSize, logData.getRows());
            assertEquals(c.expectedData, logData.toString());
        }
    }
}