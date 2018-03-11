/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 */
public class Gal16V8JEDECExporterTest extends TestCase {

    // stepper control; sequential and combinatorial
    public void testSequential() throws Exception {
        Variable D = new Variable("D");

        Variable Q0 = new Variable("Q0");
        Variable Q1 = new Variable("Q1");
        Variable Q2 = new Variable("Q2");

        //Q0.d = !Q0;
        Expression Q0d = not(Q0);
        //Q1.d = !D & !Q1 & Q0 # !D & Q1 & !Q0 # D & !Q1 & !Q0 # D & Q1 & Q0;
        Expression Q1d = or(and(not(D), not(Q1), Q0), and(not(D), Q1, not(Q0)), and(D, not(Q1), not(Q0)), and(D, Q1, Q0));
        //Q2.d = !D & !Q2 & Q1 & Q0 #
        //       !D & Q2 & !Q1 #
        //       Q2 & Q1 & !Q0 #
        //       D & !Q2 & !Q1 & !Q0 #
        //       D & Q2 & Q0;
        Expression Q2d = or(
                and(not(D), not(Q2), Q1, Q0),
                and(not(D), Q2, not(Q1)),
                and(Q2, Q1, not(Q0)),
                and(D, not(Q2), not(Q1), not(Q0)),
                and(D, Q2, Q0));

        //P0 = !Q2 & !Q1 # Q2 & Q1 & Q0;
        Expression P0 = or(
                and(not(Q2), not(Q1)),
                and(Q2, Q1, Q0));
        //P1 = !Q2 & Q0 # !Q2 & Q1;
        Expression P1 = or(
                and(not(Q2), Q0),
                and(not(Q2), Q1));
        //P2 = !Q2 & Q1 & Q0 # Q2 & !Q1;
        Expression P2 = or(
                and(not(Q2), Q1, Q0),
                and(Q2, not(Q1)));
        //P3 = Q2 & Q0 # Q2 & Q1;
        Expression P3 = or(
                and(Q2, Q0),
                and(Q2, Q1));


        Gal16v8JEDECExporter gal = new Gal16v8JEDECExporter();
        gal.getPinMapping()
                .assignPin("D", 2)
                .assignPin("Q0", 12)
                .assignPin("Q1", 13)
                .assignPin("Q2", 14)
                .assignPin("P0", 15)
                .assignPin("P1", 16)
                .assignPin("P2", 17)
                .assignPin("P3", 18);

        gal.getBuilder()
                .addSequential("Q0", Q0d)
                .addSequential("Q1", Q1d)
                .addSequential("Q2", Q2d)
                .addCombinatorial("P0", P0)
                .addCombinatorial("P1", P1)
                .addCombinatorial("P2", P2)
                .addCombinatorial("P3", P3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gal.writeTo(baos);

        assertEquals("\u0002Digital GAL16v8 assembler*\r\n" +
                "QF2194*\r\n" +
                "G0*\r\n" +
                "F0*\r\n" +
                "L256 11111111111111111111111111111111*\r\n" +
                "L288 11111111111111111111110111111101*\r\n" +
                "L320 11111111111111111111110111011111*\r\n" +
                "L512 11111111111111111111111111111111*\r\n" +
                "L544 11111111111111111111111011011101*\r\n" +
                "L576 11111111111111111111110111101111*\r\n" +
                "L768 11111111111111111111111111111111*\r\n" +
                "L800 11111111111111111111111011111101*\r\n" +
                "L832 11111111111111111111111011011111*\r\n" +
                "L1024 11111111111111111111111111111111*\r\n" +
                "L1056 11111111111111111111110111011101*\r\n" +
                "L1088 11111111111111111111111011101111*\r\n" +
                "L1280 10111111111111111111111011011101*\r\n" +

                "L1312 10111111111111111111110111101111*\r\n" +
                "L1344 01111111111111111111111011101110*\r\n" +
                "L1376 01111111111111111111110111111101*\r\n" +
                "L1408 11111111111111111111110111011110*\r\n"+

              //"L1312 01111111111111111111111011101110*\r\n" + // two solutions possible, WinCupl chooses the other on
              //"L1344 01111111111111111111110111011111*\r\n" +
              //"L1376 10111111111111111111110111111110*\r\n" +
              //"L1408 11111111111111111111110111101101*\r\n" +

                "L1536 10111111111111111111111111101101*\r\n" +
                "L1568 10111111111111111111111111011110*\r\n" +
                "L1600 01111111111111111111111111101110*\r\n" +
                "L1632 01111111111111111111111111011101*\r\n" +
                "L1792 11111111111111111111111111111110*\r\n" +
              //"L2048 01111111001100000011000000100000*\r\n" +  // WinCupl fills some bits to the signature! Don't know why!
                "L2048 01111111000000000000000000000000*\r\n" +
                "L2112 00000000111110001111111111111111*\r\n" +
                "L2144 11111111111111111111111111111111*\r\n" +
                "L2176 111111111111111101*\r\n" +
                "C5723*\r\n" +
                "\u0003CE43", baos.toString());

    }

    // BitCount
    public void testCombinatorial() throws Exception {
        Variable A = new Variable("A");
        Variable B = new Variable("B");
        Variable C = new Variable("C");

        //Y_0 = (!A & !B & C) #
        //      (!A & B & !C) #
        //      (A & !B & !C) #
        //      (A & B & C);
        Expression Y_0 = or(
                and(not(A), not(B), C),
                and(not(A), B, not(C)),
                and(A, not(B), not(C)),
                and(A, B, C));

        //Y_1 = (A & C) # (A & B) # (B & C);
        Expression Y_1 = or(and(A, C), and(A, B), and(B, C));

        Gal16v8JEDECExporter gal = new Gal16v8JEDECExporter();
        gal.getPinMapping()
                .assignPin("A", 2)
                .assignPin("B", 3)
                .assignPin("C", 4)
                .assignPin("Y_1", 12)
                .assignPin("Y_0", 13);

        gal.getBuilder()
                .addCombinatorial("Y_0", Y_0)
                .addCombinatorial("Y_1", Y_1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gal.writeTo(baos);

        assertEquals("\u0002Digital GAL16v8 assembler*\r\n" +
                "QF2194*\r\n" +
                "G0*\r\n" +
                "F0*\r\n" +
                "L1536 10111011011111111111111111111111*\r\n" +
                "L1568 10110111101111111111111111111111*\r\n" +
                "L1600 01111011101111111111111111111111*\r\n" +
                "L1632 01110111011111111111111111111111*\r\n" +
                "L1792 01111111011111111111111111111111*\r\n" +
                "L1824 01110111111111111111111111111111*\r\n" +
                "L1856 11110111011111111111111111111111*\r\n" +
              //"L2048 00000011001100000011000000100000*\r\n" + // this is cupls line; WinCupl fills some bits to the signature! Don't know why!
                "L2048 00000011000000000000000000000000*\r\n" +
                "L2112 00000000111111001111111111111111*\r\n" +
                "L2144 11111111111111111111111111111111*\r\n" +
                "L2176 111111111111111110*\r\n" +
                "C244C*\r\n" +
                "\u00035E24", baos.toString());

    }

    /**
     * Simple test that uses most of the pins.
     * In test/resources/testGAL/cupl/GALTest.jed you can find the original CUPL generated jed file.
     * Here only the fuses are taken from that CUPL generated file to compare them with the Digital
     * generated fuses.
     * @throws Exception
     */
    public void testCombinatorial2() throws Exception {
        Gal16v8JEDECExporter gal = new Gal16v8JEDECExporter();
        gal.getBuilder()
                .addCombinatorial("Y_1", and(v("A_1"),v("A_2")))
                .addCombinatorial("Y_2", and(v("A_2"),v("A_3")))
                .addCombinatorial("Y_3", and(v("A_3"),v("A_4")))
                .addCombinatorial("Y_4", and(v("A_4"),v("A_5")))
                .addCombinatorial("Y_5", and(v("A_5"),v("A_6")))
                .addCombinatorial("Y_6", and(v("A_6"),v("A_7")))
                .addCombinatorial("Y_7", and(v("A_7"),v("A_8")));

        gal.getPinMapping()
                .assignPin("A_1", 2)
                .assignPin("A_2", 3)
                .assignPin("A_3", 4)
                .assignPin("A_4", 5)
                .assignPin("A_5", 6)
                .assignPin("A_6", 7)
                .assignPin("A_7", 8)
                .assignPin("A_8", 9)
                .assignPin("Y_1", 12)
                .assignPin("Y_2", 13)
                .assignPin("Y_3", 14)
                .assignPin("Y_4", 15)
                .assignPin("Y_5", 16)
                .assignPin("Y_6", 17)
                .assignPin("Y_7", 18);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gal.writeTo(baos);

        assertEquals("\u0002Digital GAL16v8 assembler*\r\n" +
                "QF2194*\r\n" +
                "G0*\r\n" +
                "F0*\r\n" +
                "L256 11111111111111111111111101110111*\r\n" +
                "L512 11111111111111111111011101111111*\r\n" +
                "L768 11111111111111110111011111111111*\r\n" +
                "L1024 11111111111101110111111111111111*\r\n" +
                "L1280 11111111011101111111111111111111*\r\n" +
                "L1536 11110111011111111111111111111111*\r\n" +
                "L1792 01110111111111111111111111111111*\r\n" +
                //"L2048 01111111001100000011000000100000*\r\n" + // this is cupls line, WinCupl fills some bits to the signature! Don't know why!
                "L2048 01111111000000000000000000000000*\r\n" +
                "L2112 00000000100000001111111111111111*\r\n" +
                "L2144 11111111111111111111111111111111*\r\n" +
                "L2176 111111111111111110*\r\n" +
                "C2465*\r\n" +
                "\u00035D7E", baos.toString());

    }


}
