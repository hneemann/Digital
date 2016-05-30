package de.neemann.digital.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * @author hneemann
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


        Gal16v8JEDECExporter gal = new Gal16v8JEDECExporter()
                .assignPin("D", 2)
                .assignPin("Q0", 16)
                .assignPin("Q1", 17)
                .assignPin("Q2", 18)
                .assignPin("P0", 12)
                .assignPin("P1", 13)
                .assignPin("P2", 14)
                .assignPin("P3", 15);

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
                "L256 10111110110111011111111111111111*\r\n" + // fuses are created with WinCUPL 5.0
                "L288 10111101111011111111111111111111*\r\n" +
                "L320 01111110111011101111111111111111*\r\n" +
                "L352 01111101111111011111111111111111*\r\n" +
                "L384 11111101110111101111111111111111*\r\n" +
                "L512 10111111111011011111111111111111*\r\n" +
                "L544 10111111110111101111111111111111*\r\n" +
                "L576 01111111111011101111111111111111*\r\n" +
                "L608 01111111110111011111111111111111*\r\n" +
                "L768 11111111111111101111111111111111*\r\n" +
                "L1024 11111111111111111111111111111111*\r\n" +
                "L1056 11111101111111011111111111111111*\r\n" +
                "L1088 11111101110111111111111111111111*\r\n" +
                "L1280 11111111111111111111111111111111*\r\n" +
                "L1312 11111110110111011111111111111111*\r\n" +
                "L1344 11111101111011111111111111111111*\r\n" +
                "L1536 11111111111111111111111111111111*\r\n" +
                "L1568 11111110111111011111111111111111*\r\n" +
                "L1600 11111110110111111111111111111111*\r\n" +
                "L1792 11111111111111111111111111111111*\r\n" +
                "L1824 11111101110111011111111111111111*\r\n" +
                "L1856 11111110111011111111111111111111*\r\n" +
                "L2112 00000000100011111111111111111111*\r\n" +
                "L2144 11111111111111111111111111111111*\r\n" +
                "L2176 111111111111111101*\r\n" +
                "C56F7*\r\n" +
                "\u0003C6B3", baos.toString());

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

        Gal16v8JEDECExporter gal = new Gal16v8JEDECExporter()
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
              //"L2048 00000011001100000011000000100000*\r\n" + // WinCupl fills some bits to the signature! Don't know why!
                "L2048 00000011000000000000000000000000*\r\n" +
              //"L2112 00000000111111001111111111111111*\r\n" + // WinCupl sets AC1(n) for unused OLMC! Don't know why!
                "L2112 00000000000000001111111111111111*\r\n" +
                "L2144 11111111111111111111111111111111*\r\n" +
                "L2176 111111111111111110*\r\n" +
                "C240D*\r\n" +
                "\u00035E1B", baos.toString());

    }



}