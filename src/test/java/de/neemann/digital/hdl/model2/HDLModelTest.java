/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.hdl.model2.clock.ClockIntegratorGeneric;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.model2.optimizations.*;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

public class HDLModelTest extends TestCase {

    private HDLCircuit getCircuit(String filename, HDLClockIntegrator ci) throws Exception {
        ToBreakRunner br = new ToBreakRunner(filename);
        return new HDLCircuit(br.getCircuit(), "main", new HDLModel(br.getLibrary()), 0, ci);
    }

    public void testSimple() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/comb.dig", null)
                .apply(new MergeAssignments())
                .apply(new NodeSorterExpressionBased())
                .nameUnnamedSignals();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:1 defines (A->3), B:1 defines (B->2), C:1 defines (C->2))\n" +
                "  out(X:1 reads (X->1), Y:1 reads (Y_temp->2), Z:1 reads (Z_temp->2), Aident:1 reads (A->3))\n" +
                "  sig(Y_temp->2, s0->1, Z_temp->2, s1->1)\n" +
                "\n" +
                "  node Const\n" +
                "    in()\n" +
                "    out(out:1 defines (s1->1))\n" +
                "    s1->1 := 1:1\n" +
                "  node merged expression\n" +
                "    in(In_1:1 reads (B->2), in:1 reads (C->2))\n" +
                "    out(out:1 defines (Y_temp->2))\n" +
                "    Y_temp->2 := (B OR NOT C)\n" +
                "  node Not\n" +
                "    in(in:1 reads (A->3))\n" +
                "    out(out:1 defines (Z_temp->2))\n" +
                "    Z_temp->2 := NOT A\n" +
                "  node merged expression\n" +
                "    in(In_5:1 reads (Y_temp->2), In_1:1 reads (A->3), In_2:1 reads (C->2), In_1:1 reads (Z_temp->2), In_1:1 reads (B->2))\n" +
                "    out(out:1 defines (s0->1))\n" +
                "    s0->1 := ((A OR C) AND (Z_temp OR C) AND 1:1 AND NOT (B OR C) AND Y_temp)\n" +
                "  node D_FF\n" +
                "    in(D:1 reads (s0->1), C:1 reads (s1->1))\n" +
                "    out(Q:1 defines (X->1), ~Q:1 is not used)\n" +
                "\n" +
                "  Y:1 reads (Y_temp->2) := Y_temp->2\n" +
                "  Z:1 reads (Z_temp->2) := Z_temp->2\n" +
                "  Aident:1 reads (A->3) := A->3\n" +
                "end circuit main\n", cp.toString());
    }

    public void testSimple2() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/comb2.dig", null)
                .applyDefaultOptimizations();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:1 defines (A->1), B:1 defines (B->1), C:1 defines (C->1))\n" +
                "  out(Y:1 reads (Y->1))\n" +
                "  sig()\n" +
                "\n" +
                "  node merged expression\n" +
                "    in(In_2:1 reads (C->1), In_1:1 reads (A->1), In_2:1 reads (B->1))\n" +
                "    out(out:1 defines (Y->1))\n" +
                "    Y->1 := ((A AND B) OR C)\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testInputInvert() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/inputInvert.dig", null)
                .applyDefaultOptimizations();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:1 defines (A->1), B:1 defines (B->1), C:1 defines (C->1))\n" +
                "  out(Y:1 reads (Y->1))\n" +
                "  sig()\n" +
                "\n" +
                "  node merged expression\n" +
                "    in(In_2:1 reads (B->1), In_3:1 reads (C->1), In_1:1 reads (A->1))\n" +
                "    out(out:1 defines (Y->1))\n" +
                "    Y->1 := ((A AND NOT B) OR B OR C)\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testInputInvert2() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/inputInvert2.dig", null)
                .applyDefaultOptimizations();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:1 defines (A->1), B:1 defines (B->1), C:1 defines (C->1))\n" +
                "  out(Y:1 reads (Y->1))\n" +
                "  sig()\n" +
                "\n" +
                "  node merged expression\n" +
                "    in(In_2:1 reads (C->1), In_1:1 reads (A->1), In_2:1 reads (B->1))\n" +
                "    out(out:1 defines (Y->1))\n" +
                "    Y->1 := (NOT (A AND B) OR C)\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testSplitter() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/splitter.dig", null)
                .applyDefaultOptimizations();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:4 defines (A->1))\n" +
                "  out(X:2 reads (X->1))\n" +
                "  sig()\n" +
                "\n" +
                "  node merged expression\n" +
                "    in(in:4 reads (A->1))\n" +
                "    out(out:2 defines (X->1))\n" +
                "    X->1 := (A(1-0) AND NOT A(3-2))\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testSplitter2() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/splitter2.dig", null)
                .applyDefaultOptimizations();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:2 defines (A->1), B:2 defines (B->1))\n" +
                "  out(X:1 reads (X->1), Y:3 reads (Y->1))\n" +
                "  sig(s0->2)\n" +
                "\n" +
                "  node Splitter\n" +
                "    in(0,1:2 reads (A->1), 2,3:2 reads (B->1))\n" +
                "    out(single:4 defines (s0->2))\n" +
                "    s0(1-0) := A\n" +
                "    s0(3-2) := B\n" +
                "  node splitter\n" +
                "    in(in:4 reads (s0->2))\n" +
                "    out(0:1 defines (X->1))\n" +
                "    X->1 := s0(0-0)\n" +
                "  node splitter\n" +
                "    in(in:4 reads (s0->2))\n" +
                "    out(1-3:3 defines (Y->1))\n" +
                "    Y->1 := s0(3-1)\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testClock() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/clock.dig", new ClockIntegratorGeneric(10)).applyDefaultOptimizations();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:1 defines (A->1), C:1 defines (C->1))\n" +
                "  out(X:1 reads (X->1))\n" +
                "  sig(s0->1)\n" +
                "\n" +
                "  node simpleClockDivider\n" +
                "    in(cin:1 reads (C->1))\n" +
                "    out(cout:1 defines (s0->1))\n" +
                "  node D_FF\n" +
                "    in(D:1 reads (A->1), C:1 reads (s0->1))\n" +
                "    out(Q:1 defines (X->1), ~Q:1 is not used)\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testNaming() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/naming.dig", null).applyDefaultOptimizations();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(S0:1 defines (S0->2), S1:1 defines (S1->2))\n" +
                "  out(S2:1 reads (S2->1), S3:1 reads (S3->1))\n" +
                "  sig(s4->2)\n" +
                "\n" +
                "  node NOr\n" +
                "    in(In_1:1 reads (S0->2), In_2:1 reads (S1->2))\n" +
                "    out(out:1 defines (s4->2))\n" +
                "    s4->2 := NOT (S0 OR S1)\n" +
                "  node XOr\n" +
                "    in(In_1:1 reads (S0->2), In_2:1 reads (s4->2))\n" +
                "    out(out:1 defines (S2->1))\n" +
                "    S2->1 := (S0 XOR s4)\n" +
                "  node XOr\n" +
                "    in(In_1:1 reads (s4->2), In_2:1 reads (S1->2))\n" +
                "    out(out:1 defines (S3->1))\n" +
                "    S3->1 := (s4 XOR S1)\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testConstantMerge() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/constMerge.dig", null)
                .apply(new MergeAssignments())
                .apply(new MergeConstants())
                .apply(new NameConstantSignals())
                .apply(new NodeSorterExpressionBased())
                .nameUnnamedSignals();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:1 defines (A->1), B:1 defines (B->1))\n" +
                "  out(Y:2 reads (Y->1), X:1 reads (X->1), Z:2 reads (Z->1))\n" +
                "  sig(const2b1->1, const1b1->3, const1b0->1)\n" +
                "\n" +
                "  node Const\n" +
                "    in()\n" +
                "    out(out:2 defines (const2b1->1))\n" +
                "    const2b1->1 := 1:2\n" +
                "  node Const\n" +
                "    in()\n" +
                "    out(out:1 defines (const1b1->3))\n" +
                "    const1b1->3 := 1:1\n" +
                "  node Ground\n" +
                "    in()\n" +
                "    out(out:1 defines (const1b0->1))\n" +
                "    const1b0->1 := 0:1\n" +
                "  node D_FF\n" +
                "    in(D:2 reads (const2b1->1), C:1 reads (B->1))\n" +
                "    out(Q:2 defines (Y->1), ~Q:2 is not used)\n" +
                "  node D_FF\n" +
                "    in(D:1 reads (A->1), C:1 reads (const1b1->3))\n" +
                "    out(Q:1 defines (X->1), ~Q:1 is not used)\n" +
                "  node Counter\n" +
                "    in(en:1 reads (const1b1->3), C:1 reads (const1b1->3), clr:1 reads (const1b0->1))\n" +
                "    out(out:2 defines (Z->1), ovf:1 is not used)\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testCircular() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/circular.dig", null).applyDefaultOptimizations();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:1 defines (A->1), C:1 defines (C->3))\n" +
                "  out(X:1 reads (X->1))\n" +
                "  sig(s0->1, s1->1, Q_B->1, s2->1, s3->1, Q_A->1, s4->1, s5->1)\n" +
                "\n" +
                "  node XOr\n" +
                "    in(In_1:1 reads (s5->1), In_2:1 reads (A->1))\n" +
                "    out(out:1 defines (s4->1))\n" +
                "    s4->1 := (s5 XOR A)\n" +
                "  node D_FF\n" +
                "    in(D:1 reads (s4->1), C:1 reads (C->3))\n" +
                "    out(Q:1 defines (Q_A->1), ~Q:1 defines (s5->1))\n" +
                "  node XOr\n" +
                "    in(In_1:1 reads (s3->1), In_2:1 reads (Q_A->1))\n" +
                "    out(out:1 defines (s2->1))\n" +
                "    s2->1 := (s3 XOR Q_A)\n" +
                "  node D_FF\n" +
                "    in(D:1 reads (s2->1), C:1 reads (C->3))\n" +
                "    out(Q:1 defines (Q_B->1), ~Q:1 defines (s3->1))\n" +
                "  node XOr\n" +
                "    in(In_1:1 reads (s1->1), In_2:1 reads (Q_B->1))\n" +
                "    out(out:1 defines (s0->1))\n" +
                "    s0->1 := (s1 XOR Q_B)\n" +
                "  node D_FF\n" +
                "    in(D:1 reads (s0->1), C:1 reads (C->3))\n" +
                "    out(Q:1 defines (X->1), ~Q:1 defines (s1->1))\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }


    public void testSplitter3() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/splitter3.dig", null)
                .apply(new ReplaceOneToMany())
                .apply(new MergeAssignments())
                .apply(new NodeSorterExpressionBased())
                .nameUnnamedSignals();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:4 defines (A->2), B:4 defines (B->2))\n" +
                "  out(S:4 reads (S->1))\n" +
                "  sig(s0->1, s1->1)\n" +
                "\n" +
                "  node merged expression\n" +
                "    in(in:4 reads (A->2), in:4 reads (B->2))\n" +
                "    out(out:2 defines (s0->1))\n" +
                "    s0->1 := (A(1-0) AND B(1-0))\n" +
                "  node merged expression\n" +
                "    in(in:4 reads (A->2), in:4 reads (B->2))\n" +
                "    out(out:2 defines (s1->1))\n" +
                "    s1->1 := (A(3-2) OR B(3-2))\n" +
                "  node Splitter\n" +
                "    in(0,1:2 reads (s0->1), 2,3:2 reads (s1->1))\n" +
                "    out(0-3:4 defines (S->1))\n" +
                "    S(1-0) := s0\n" +
                "    S(3-2) := s1\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

    public void testSplitter4() throws Exception {
        HDLCircuit hdl = getCircuit("dig/hdl/model2/splitter4.dig", null)
                .apply(new ReplaceOneToMany())
                .apply(new MergeAssignments())
                .apply(new NodeSorterExpressionBased())
                .nameUnnamedSignals();

        CodePrinterStr cp = new CodePrinterStr();
        hdl.print(cp);
        assertEquals("circuit main\n" +
                "  in(A:4 defines (A->2))\n" +
                "  out(S:2 reads (S->1))\n" +
                "  sig(s0->1, s1->1)\n" +
                "\n" +
                "  node splitter\n" +
                "    in(in:4 reads (A->2))\n" +
                "    out(0,1:2 defines (s0->1))\n" +
                "    s0->1 := A(1-0)\n" +
                "  node splitter\n" +
                "    in(in:4 reads (A->2))\n" +
                "    out(2,3:2 defines (s1->1))\n" +
                "    s1->1 := A(3-2)\n" +
                "  node s_inc.dig\n" +
                "    in(A:2 reads (s0->1), B:2 reads (s1->1))\n" +
                "    out(C:2 defines (S->1))\n" +
                "\n" +
                "end circuit main\n", cp.toString());
    }

}