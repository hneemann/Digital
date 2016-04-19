package de.neemann.digital.gui.components.listing;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;

/**
 * @author hneemann
 */
public class ListingTest extends TestCase {
    private static final String listing1 =
            "8   | 0000: 1411                LDIs  R1,1             ; 0x1\n" +
                    "9   | 0001: 1400                LDIs  R0,0             ; 0x0\n" +
                    "                                INC R0\n" +
                    "11  | 0002: 1801      up:        ADDIs R0,1            ; 0x1\n" +
                    "12  | 0003: 3e10                LSL   R1\n" +
                    "13  | 0004: 7351                OUTs  LedAddr,R1       ; 0x15\n" +
                    "14  | 0005: 7c00                BRK   \n" +
                    "15  | 0006: 3c0f                CPIs  R0,15            ; 0xf\n" +
                    "16  | 0007: 65fa                BRNE  up               ; 0x2\n" +
                    "                                DEC R0\n" +
                    "18  | 0008: 2001      down:      SUBIs R0,1            ; 0x1\n" +
                    "19  | 0009: 4010                LSR   R1\n" +
                    "20  | 000a: 7351                OUTs  LedAddr,R1       ; 0x15\n" +
                    "21  | 000b: 7c00                BRK   \n" +
                    "22  | 000c: 3c00                CPIs  R0,0             ; 0x0\n" +
                    "23  | 000d: 65fa                BRNE  down             ; 0x8\n" +
                    "24  | 000e: 6ff3                JMPs  up               ; 0x2";

    public void testListing1() throws Exception {
        Listing l = new Listing(new ByteArrayInputStream(listing1.getBytes()));

        assertEquals(0, (int) l.getLine(0x0000));
        assertEquals(11, (int) l.getLine(0x0009));
        assertEquals(16, (int) l.getLine(0x000e));
    }

    private static final String listing2 =
            "8   | 0000: 1411                LDIs  R1,1             ; 0x1\n" +
                    "9   | 0001: 1400                LDIs  R0,0             ; 0x0\n" +
                    "                                INC R0\n" +
                    "11  | 0002: 1801      up:        ADDIs R0,1            ; 0x1\n" +
                    "12  | 0003: 3e10                LSL   R1\n" +
                    "13  | 0004: 7351                OUTs  LedAddr,R1       ; 0x15\n" +
                    " .data text1 \"the sum of %x and %x\\n\",  0\n" +
                    " .data text2 \"gives %x\\n\",  0\n" +
                    " .const TERMINAL_PORT 31\n" +
                    " .reg ARG_ADDR R4\n" +
                    " .reg TEXT_ADDR R3\n" +
                    " .reg DATA R0\n" +
                    " .reg DIGIT R1\n" +
                    "14  | 0005: 7c00                BRK   \n" +
                    "; full line comment: test\n" +
                    "15  | 0006: 3c0f                CPIs  R0,15            ; 0xf\n" +
                    "16  | 0007: 65fa                BRNE  up               ; 0x2\n" +
                    "                                DEC R0\n" +
                    "18  | 0008: 2001      down:      SUBIs R0,1            ; 0x1\n" +
                    "0009: 4010                LSR   R1\n" +
                    "000a: 7351                OUTs  LedAddr,R1       ; 0x15\n" +
                    "000b: 7c00                BRK   \n" +
                    "000c: 3c00                CPIs  R0,0             ; 0x0\n" +
                    "000d: 65fa                BRNE  down             ; 0x8\n" +
                    "000e: 6ff3                JMPs  up               ; 0x2";

    public void testListing2() throws Exception {
        Listing l = new Listing(new ByteArrayInputStream(listing2.getBytes()));

        assertEquals(0, (int) l.getLine(0x0000));
        assertEquals(19, (int) l.getLine(0x0009));
        assertEquals(24, (int) l.getLine(0x000e));
    }

}