/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.Model;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

public class CycleDetectorTest extends TestCase {

    private static final String[] nameTableSequential = {
            "D.dig",
            "D-MS.dig",
            "D_NAND.dig",
            "D-T2.dig",
            "D-T.dig",
            "D-TransmissionGate.dig",
            "JK-MS.dig",
            "JK-T.dig",
            "multip_D_notWorking.dig",
            "multip_D_working_detail.dig",
            "multip_D_working.dig",
            "multip_D_working_T.dig",
            "RS-C.dig",
            "RS.dig",
            "RS-MS.dig",
            "RS-MS-Simp.dig",
            "RS-T.dig",
            "T.dig",
            "T-MS.dig"};

    private static final String[] nameTableCombinatorial = {
            "Adder8bit.dig",
            "Comp7485.dig",
            "Comp.dig",
            "CompEN.dig",
            "CompRC.dig",
            "DemuxCas.dig",
            "demux.dig",
            "FullAdderCLA.dig",
            "FullAdder.dig",
            "FullAdderNaive.dig",
            "FullAdderRC.dig",
            "FullAdderRCSig.dig",
            "FullAddSub_RC.dig",
            "FullSub2.dig",
            "FullSub.dig",
            "FullSubNaive.dig",
            "FullSubRC.dig",
            "HalfAdder.dig",
            "HalfSub.dig",
            "LUT.dig",
            "Multiply8Bit.dig",
            "Multiply.dig",
            "mux.dig",
            "Xor1.dig",
            "Xor2.dig",
            "Xor3.dig"};


    public void testCycles() throws Exception {
        for (String name : nameTableSequential) {
            Model model = new ToBreakRunner("../../main/dig/sequential/" + name, false).getModel();
            try {
                new ModelAnalyser(model).analyse();
                fail("in " + name + " cycle detection failes!");
            } catch (CycleDetector.CycleException e) {
                assertTrue(true);
            }
        }
    }

    public void testCyclesOk() throws Exception {
        for (String name : nameTableCombinatorial) {
            try {
                Model model = new ToBreakRunner("../../main/dig/combinatorial/" + name, false).getModel();
                new ModelAnalyser(model).analyse();
            } catch (CycleDetector.CycleException e) {
                fail("cycle detected in " + name);
            }
        }
    }

}
