/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.ModelAnalyserInfo;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.TreeMap;

public class ValueParserTest extends TestCase {

    public void testValues() throws FiniteStateMachineException {
        TreeMap<String, Integer> v = new ValueParser("A=1").parse();
        assertEquals(1, v.size());
        assertEquals(1, (long) v.get("A"));

        v = new ValueParser("A=1, B=X").parse();
        assertEquals(2, v.size());
        assertEquals(1, (long) v.get("A"));
        assertEquals(2, (long) v.get("B"));
    }

    public void testMultiBitValues() throws FiniteStateMachineException {
        TreeMap<String, Integer> v = new ValueParser("A=1101").parse();
        assertEquals(4, v.size());
        assertEquals(1, (long) v.get("A0"));
        assertEquals(0, (long) v.get("A1"));
        assertEquals(1, (long) v.get("A2"));
        assertEquals(1, (long) v.get("A3"));
    }

    public void testMultiBitValuesMAI() throws FiniteStateMachineException {
        ModelAnalyserInfo mai = new ModelAnalyserInfo(null);
        TreeMap<String, Integer> v = new ValueParser("A=1101").setModelAnalyzerInfo(mai).parse();
        assertEquals(4, v.size());
        ArrayList<ModelAnalyserInfo.Bus> om = mai.getOutputBusList();
        assertEquals(1, om.size());
        ModelAnalyserInfo.Bus bus = om.get(0);
        assertEquals("A", bus.getBusName());
        ArrayList<String> list = bus.getSignalNames();
        assertEquals(4, list.size());
        assertEquals("A0", list.get(0));
        assertEquals("A1", list.get(1));
        assertEquals("A2", list.get(2));
        assertEquals("A3", list.get(3));
    }


}