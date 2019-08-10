/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.tt2;

import de.neemann.digital.analyse.expression.Constant;
import de.neemann.digital.builder.jedec.FuseMapFillerException;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 */
public class TT2ExporterTest extends TestCase {

    public void testCombinatorial() throws Exception {
        TT2Exporter tt2 = new TT2Exporter("unknown");
        tt2.getPinMapping().setAvailBidirectional(4, 5, 6, 8, 20, 21);
        tt2.getBuilder().addCombinatorial("Y", and(v("A"), v("B")));
        tt2.getBuilder().addCombinatorial("X", or(v("A1"), v("B1")));
        tt2.getPinMapping().parseString("X=21;Y=20");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tt2.writeTo(baos);

        assertEquals("#$ TOOL CUPL\r\n" +
                "# Berkeley PLA format generated using Digital\r\n" +
                "#$ TITLE  unknown\r\n" +
                "#$ DEVICE  f1502ispplcc44\r\n" +
                "#$ PINS 6 A+:4 A1+:5 B+:6 B1+:8 Y+:20 X+:21\r\n" +
                ".i 4\r\n" +
                ".o 2\r\n" +
                ".type f\r\n" +
                ".ilb A A1 B B1\r\n" +
                ".ob Y X\r\n" +
                ".phase 11\r\n" +
                ".p 3\r\n" +
                "1-1- 10\r\n" +
                "-1-- 01\r\n" +
                "---1 01\r\n" +
                ".e\r\n", baos.toString());
    }

    public void testSequential() throws Exception {
        TT2Exporter tt2 = new TT2Exporter("unknown");
        tt2.getPinMapping().setAvailBidirectional(4, 5, 6, 8, 20, 21);
        tt2.getBuilder().addSequential("Yn", and(v("A"), not(v("Yn"))));
        tt2.getPinMapping().parseString("Yn=5");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tt2.writeTo(baos);

        assertEquals("#$ TOOL CUPL\r\n" +
                "# Berkeley PLA format generated using Digital\r\n" +
                "#$ TITLE  unknown\r\n" +
                "#$ DEVICE  f1502ispplcc44\r\n" +
                "#$ PINS 3 A+:4 CLK+:43 Yn+:5\r\n" +
                ".i 3\r\n" +
                ".o 3\r\n" +
                ".type f\r\n" +
                ".ilb A CLK Yn.Q\r\n" +
                ".ob Yn.REG Yn.AR Yn.C\r\n" +
                ".phase 111\r\n" +
                ".p 3\r\n" +
                "1-0 100\r\n" +
                "-1- 001\r\n" +
                "--- 000\r\n" +
                ".e\r\n", baos.toString());
    }

    public void testSequential2() throws Exception {
        TT2Exporter tt2 = new TT2Exporter("unknown");
        tt2.getPinMapping().setAvailBidirectional(4, 5, 6, 8, 20, 21);
        tt2.getBuilder().addSequential("Yn", and(v("A"), not(v("Yn"))));
        tt2.getBuilder().addSequential("Xn", or(v("B"), not(v("Xn"))));
        tt2.getPinMapping().parseString("Xn=8;Yn=6");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tt2.writeTo(baos);

        assertEquals("#$ TOOL CUPL\r\n" +
                "# Berkeley PLA format generated using Digital\r\n" +
                "#$ TITLE  unknown\r\n" +
                "#$ DEVICE  f1502ispplcc44\r\n" +
                "#$ PINS 5 A+:4 B+:5 CLK+:43 Yn+:6 Xn+:8\r\n" +
                ".i 5\r\n" +
                ".o 6\r\n" +
                ".type f\r\n" +
                ".ilb A B CLK Xn.Q Yn.Q\r\n" +
                ".ob Yn.REG Yn.AR Yn.C Xn.REG Xn.AR Xn.C\r\n" +
                ".phase 111111\r\n" +
                ".p 5\r\n" +
                "1---0 100000\r\n" +
                "-1--- 000100\r\n" +
                "--1-- 001001\r\n" +
                "---0- 000100\r\n" +
                "----- 000000\r\n" +
                ".e\r\n", baos.toString());
    }

    public void testConstantsSeq() throws Exception {
        TT2Exporter tt2 = new TT2Exporter("unknown");
        tt2.getPinMapping().setAvailBidirectional(4, 5, 6, 8, 20, 21);
        tt2.getBuilder().addSequential("Yn", Constant.ONE);
        tt2.getBuilder().addSequential("Xn", Constant.ZERO);
        tt2.getPinMapping().parseString("Xn=8;Yn=6");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tt2.writeTo(baos);
        assertEquals("#$ TOOL CUPL\r\n" +
                "# Berkeley PLA format generated using Digital\r\n" +
                "#$ TITLE  unknown\r\n" +
                "#$ DEVICE  f1502ispplcc44\r\n" +
                "#$ PINS 3 CLK+:43 Yn+:6 Xn+:8\r\n" +
                ".i 3\r\n" +
                ".o 6\r\n" +
                ".type f\r\n" +
                ".ilb CLK Xn.Q Yn.Q\r\n" +
                ".ob Yn.REG Yn.AR Yn.C Xn.REG Xn.AR Xn.C\r\n" +
                ".phase 111111\r\n" +
                ".p 2\r\n" +
                "1-- 001001\r\n" +
                "--- 100000\r\n" +
                ".e\r\n", baos.toString());
    }

    public void testConstantsComb() throws Exception {
        TT2Exporter tt2 = new TT2Exporter("unknown");
        tt2.getPinMapping().setAvailBidirectional(4, 5, 6, 8, 20, 21);
        tt2.getBuilder().addCombinatorial("Yn", Constant.ZERO);
        tt2.getBuilder().addCombinatorial("Xn", Constant.ONE);
        tt2.getPinMapping().parseString("Xn=8;Yn=6");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tt2.writeTo(baos);
        assertEquals("#$ TOOL CUPL\r\n" +
                "# Berkeley PLA format generated using Digital\r\n" +
                "#$ TITLE  unknown\r\n" +
                "#$ DEVICE  f1502ispplcc44\r\n" +
                "#$ PINS 2 Yn+:6 Xn+:8\r\n" +
                ".i 0\r\n" +
                ".o 2\r\n" +
                ".type f\r\n" +
                ".ob Yn Xn\r\n" +
                ".phase 11\r\n" +
                ".p 1\r\n" +
                " 01\r\n" +
                ".e\r\n", baos.toString());
    }

    public void testCombinatorialRenaming() throws Exception {
        TT2Exporter tt2 = new TT2Exporter("unknown");
        tt2.getPinMapping().setAvailBidirectional(4, 5, 6, 8, 20, 21);
        tt2.getBuilder().addCombinatorial("Y^l", and(v("A"), v("B")));
        tt2.getBuilder().addCombinatorial("X^l", or(v("A1"), v("B1")));
        tt2.getPinMapping().parseString("X^l=21;Y^l=20");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tt2.writeTo(baos);

        assertEquals("#$ TOOL CUPL\r\n" +
                "# Berkeley PLA format generated using Digital\r\n" +
                "#$ TITLE  unknown\r\n" +
                "#$ DEVICE  f1502ispplcc44\r\n" +
                "#$ PINS 6 A+:4 A1+:5 B+:6 B1+:8 Yl+:20 Xl+:21\r\n" +
                ".i 4\r\n" +
                ".o 2\r\n" +
                ".type f\r\n" +
                ".ilb A A1 B B1\r\n" +
                ".ob Yl Xl\r\n" +
                ".phase 11\r\n" +
                ".p 3\r\n" +
                "1-1- 10\r\n" +
                "-1-- 01\r\n" +
                "---1 01\r\n" +
                ".e\r\n", baos.toString());
    }

    public void testSequentialRenaming() throws Exception {
        TT2Exporter tt2 = new TT2Exporter("unknown");
        tt2.getPinMapping().setAvailBidirectional(4, 5, 6, 8, 20, 21);
        tt2.getBuilder().addSequential("Y^n", and(v("A"), not(v("Y^n"))));
        tt2.getPinMapping().parseString("Y^n=5");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tt2.writeTo(baos);

        assertEquals("#$ TOOL CUPL\r\n" +
                "# Berkeley PLA format generated using Digital\r\n" +
                "#$ TITLE  unknown\r\n" +
                "#$ DEVICE  f1502ispplcc44\r\n" +
                "#$ PINS 3 A+:4 CLK+:43 Yn+:5\r\n" +
                ".i 3\r\n" +
                ".o 3\r\n" +
                ".type f\r\n" +
                ".ilb A CLK Yn.Q\r\n" +
                ".ob Yn.REG Yn.AR Yn.C\r\n" +
                ".phase 111\r\n" +
                ".p 3\r\n" +
                "1-0 100\r\n" +
                "-1- 001\r\n" +
                "--- 000\r\n" +
                ".e\r\n", baos.toString());
    }

    public void testNames() throws FuseMapFillerException {
        TT2Exporter.checkName("a0");
        TT2Exporter.checkName("b_0");
        TT2Exporter.checkName("c");
        TT2Exporter.checkName("Cc");
        checkNameFail(" a");
        checkNameFail("a ");
        checkNameFail("0a");
        checkNameFail("a+2");
        checkNameFail("a,2");
    }

    private void checkNameFail(String name) {
        try {
            TT2Exporter.checkName(name);
            fail();
        } catch (FuseMapFillerException e) {
            assertTrue(true);
        }
    }

}
