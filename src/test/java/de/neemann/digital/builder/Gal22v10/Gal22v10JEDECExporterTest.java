/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal22v10;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * Created by helmut.neemann on 06.06.2016.
 */
public class Gal22v10JEDECExporterTest extends TestCase {

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


        Gal22v10JEDECExporter gal = new Gal22v10JEDECExporter();

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

        assertEquals("\u0002Digital GAL22v10 assembler*\r\n" +
                "QF5892*\r\n" +
                "G0*\r\n" +
                "F0*\r\n" +
                "L1472 00000000000000000000000011111111*\r\n" +   // CUPL output
                "L1504 11111111111111111111111111111111*\r\n" +
                "L1536 11111111111111111111111111111111*\r\n" +
                "L1568 11101111111011111111111111111111*\r\n" +
                "L1600 11111111111111101110111111110000*\r\n" +
                "L2144 00000000000011111111111111111111*\r\n" +
                "L2176 11111111111111111111111111111111*\r\n" +
                "L2208 11111111111111111111110111101110*\r\n" +
                "L2240 11111111111111111111111111111111*\r\n" +
                "L2272 11101101111111110000000000000000*\r\n" +
                "L2880 00000000000000000000000011111111*\r\n" +
                "L2912 11111111111111111111111111111111*\r\n" +
                "L2944 11111111111111111111111111111111*\r\n" +
                "L2976 11011111111011111111111111111111*\r\n" +
                "L3008 11111111111111011110111111110000*\r\n" +
                "L3648 00001111111111111111111111111111*\r\n" +
                "L3680 11111111111111111111111111111111*\r\n" +
                "L3712 11111111111111101110111011111111*\r\n" +
                "L3744 11111111111111111111111111011101*\r\n" +
                "L3776 11111111000000000000000000000000*\r\n" +
                "L4288 00000000000000000000000011111111*\r\n" +
                "L4320 11111111111111111111111111111111*\r\n" +
                "L4352 11111111101111111111111111111111*\r\n" +

                "L4384 11011110111011111111101111111111*\r\n" +
                "L4416 11111111111111101101111111111111*\r\n" +
                "L4448 01111111111111111111111111011101*\r\n" +
                "L4480 11011111111101111111111111111111*\r\n" +
                "L4512 11111110111111101111111111111111*\r\n" +
                "L4544 11111111111111111110111011011111*\r\n" +

                /* CUPL uses other product term ordering
                "L4384 11011110111011111111011111111111*\r\n" +
                "L4416 11111111111111011101110111111111*\r\n" +
                "L4448 01111111111111111111111111101110*\r\n" +
                "L4480 11111111111110111111111111111111*\r\n" +
                "L4512 11111110111111011111111111111111*\r\n" +
                "L4544 11111111111111111110110111101111*\r\n" +*/

                "L4864 00000000000000000000111111111111*\r\n" +
                "L4896 11111111111111111111111111111111*\r\n" +
                "L4928 11111011111111111111111111111111*\r\n" +

                "L4960 11011110111111111011111111111111*\r\n" +
                "L4992 11111111111111101101111111110111*\r\n" +

                /* CUPL uses other product term ordering
                "L4960 11101101111111111011111111111111*\r\n" +
                "L4992 11111111111111011110111111110111*\r\n" +*/

                "L5024 11111111111111111111111111011101*\r\n" +
                "L5056 11111111011111111111111111111111*\r\n" +
                "L5088 11111110111011110000000000000000*\r\n" +
                "L5344 00000000000000000000000011111111*\r\n" +
                "L5376 11111111111111111111111111111111*\r\n" +
                "L5408 11111111111111111111111111111111*\r\n" +
                "L5440 11111111110111110000000000000000*\r\n" +
                "L5792 00000000000000000000001111111110*\r\n" +
                "L5824 10100000000000000000000000000000*\r\n" +
                /* CUPL writes some data to the signature
                "L5824 10100011000000110000001000000000*\r\n" +
                */
                "C8241*\r\n" +
                "\u000353C9", baos.toString());

    }


    public void testPin13() throws Exception {
        Variable Q0 = new Variable("Q0");
        Variable Q1 = new Variable("Q1");
        Variable Q2 = new Variable("Q2");

        Expression Y = and(Q0, Q1, Q2);

        Gal22v10JEDECExporter gal = new Gal22v10JEDECExporter();
        gal.getPinMapping()
                .assignPin("Q0", 10)
                .assignPin("Q1", 11)
                .assignPin("Q2", 13)
                .assignPin("Y", 23);
        gal.getBuilder().addCombinatorial("Y", Y);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        gal.writeTo(baos);


        assertEquals("\u0002Digital GAL22v10 assembler*\r\n" +
                "QF5892*\r\n" +
                "G0*\r\n" +
                "F0*\r\n" +
                "L32 00000000000011111111111111111111*\r\n" +  // fuses generated with WinCupl
                "L64 11111111111111111111111111111111*\r\n" +
                "L96 11111111111111111111111111110111*\r\n" +
                "L128 01010000000000000000000000000000*\r\n" +
                "L5792 00000000000000001100000000000000*\r\n" +
                //"L5824 00000011000000110000001000000000*\r\n" +  // CUPL writes data to the signature bytes, don't know why
                "C0AE3*\r\n" +
                "\u00033205", baos.toString());

    }


}
