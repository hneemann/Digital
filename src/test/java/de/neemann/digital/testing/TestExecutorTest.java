/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.util.List;

public class TestExecutorTest extends TestCase {

    private Model m;
    private TestCaseDescription tcd;

    @Override
    public void setUp() throws Exception {
        ToBreakRunner tbr = new ToBreakRunner("dig/setStateToTestResult.dig");
        List<Circuit.TestCase> tl = tbr.getCircuit().getTestCases();
        assertEquals(1, tl.size());
        tcd = tl.get(0).getTestCaseDescription();
        m = tbr.getModel();
    }

    public void testSetStateToTestResult0() throws Exception {
        new TestExecutor(tcd, m).executeTo(0);
        assertEquals(0, m.getOutput("S").getValue());
        assertEquals(0, m.getOutput("O").getValue());
    }

    public void testSetStateToTestResult12() throws Exception {
        new TestExecutor(tcd, m).executeTo(12);
        assertEquals(6, m.getOutput("S").getValue());
        assertEquals(0, m.getOutput("O").getValue());
    }

    public void testSetStateToTestResultCarry() throws Exception {
        new TestExecutor(tcd, m).executeTo(256 * 2 - 1);
        assertEquals(0, m.getOutput("S").getValue());
        assertEquals(1, m.getOutput("O").getValue());
    }

    public void testSetStateToTestResultError() throws Exception {
        new TestExecutor(tcd, m).executeTo(256 * 256 * 2 - 1);
        assertEquals(255, m.getOutput("S").getValue());
        assertEquals(0, m.getOutput("O").getValue());
    }

}