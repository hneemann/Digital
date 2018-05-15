/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 * Created by heintz on 05.07.17.
 */
public class BarrelShifterTest extends TestCase {

    public void testNormalUnsignedLeft() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.logical, false, LeftRightFormat.left, 6, 3);
        bsTest.check(0b001100, 0, 0b001100);
        bsTest.check(0b001100, 1, 0b011000);
        bsTest.check(0b001100, 2, 0b110000);
        bsTest.check(0b010001, 2, 0b000100);
        bsTest.check(0b001100, 3, 0b100000);
        bsTest.check(0b001100, 5, 0b000000);
        bsTest.check(0b001100, -1, 0b000000);
        bsTest.check(0b001100, -4, 0b000000);
        bsTest.check(0b001100, -5, 0b100000);

        for (int s = 0; s < 7; s++)
            bsTest.check(1, s, (1 << s) & 0b111111);
    }

    public void testRotateUnsignedLeft() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 6, 3);
        bsTest.check(0b001100, 0, 0b001100);
        bsTest.check(0b001100, 1, 0b011000);
        bsTest.check(0b001100, 2, 0b110000);
        bsTest.check(0b010001, 2, 0b000101);
        bsTest.check(0b001100, 3, 0b100001);
        bsTest.check(0b001100, 5, 0b000110);
        bsTest.check(0b001100, -1, 0b011000);
        bsTest.check(0b001100, -4, 0b000011);
        bsTest.check(0b001100, -5, 0b100001);

        for (int s = 0; s < 7; s++)
            bsTest.check(0b10101010, s, (s & 1) == 0 ? 0b10101010 : 0b01010101);
    }

    public void testNormalSignedLeft() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.logical, true, LeftRightFormat.left, 6, 4);
        bsTest.check(0b001100, 0, 0b001100);
        bsTest.check(0b001100, 1, 0b011000);
        bsTest.check(0b001100, 2, 0b110000);
        bsTest.check(0b010001, 2, 0b000100);
        bsTest.check(0b001100, 3, 0b100000);
        bsTest.check(0b001100, 5, 0b000000);
        bsTest.check(0b001100, -1, 0b000110);
        bsTest.check(0b001100, -4, 0b000000);
        bsTest.check(0b001100, -5, 0b000000);
    }

    public void testRotateSignedLeft() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 6, 4);
        bsTest.check(0b001100, 0, 0b001100);
        bsTest.check(0b001100, 1, 0b011000);
        bsTest.check(0b001100, 2, 0b110000);
        bsTest.check(0b010001, 2, 0b000101);
        bsTest.check(0b001100, 3, 0b100001);
        bsTest.check(0b001100, 5, 0b000110);
        bsTest.check(0b001100, -1, 0b000110);
        bsTest.check(0b001100, -4, 0b110000);
        bsTest.check(0b001100, -5, 0b011000);
    }

    public void testNormalUnsignedRight() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.logical, false, LeftRightFormat.right, 6, 3);
        bsTest.check(0b001100, 0, 0b001100);
        bsTest.check(0b001100, 1, 0b000110);
        bsTest.check(0b001100, 2, 0b000011);
        bsTest.check(0b010001, 2, 0b000100);
        bsTest.check(0b001100, 3, 0b000001);
        bsTest.check(0b001100, 5, 0b000000);
        bsTest.check(0b001100, -1, 0b000000);
        bsTest.check(0b001100, -4, 0b000000);
        bsTest.check(0b001100, -5, 0b000001);
    }

    public void testRotateUnsignedRight() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.right, 6, 3);
        bsTest.check(0b001100, 0, 0b001100);
        bsTest.check(0b001100, 1, 0b000110);
        bsTest.check(0b001100, 2, 0b000011);
        bsTest.check(0b010001, 2, 0b010100);
        bsTest.check(0b001100, 3, 0b100001);
        bsTest.check(0b001100, 5, 0b011000);
        bsTest.check(0b001100, -1, 0b000110);
        bsTest.check(0b001100, -4, 0b110000);
        bsTest.check(0b001100, -5, 0b100001);
    }

    public void testArithmeticUnsignedRight() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.arithmetic, false, LeftRightFormat.right, 6, 3);
        bsTest.check(0b001100, 0, 0b001100);
        bsTest.check(0b001100, 1, 0b000110);
        bsTest.check(0b001100, 2, 0b000011);
        bsTest.check(0b010001, 2, 0b000100);
        bsTest.check(0b001100, 3, 0b000001);
        bsTest.check(0b001100, 5, 0b000000);
        bsTest.check(0b001100, -1, 0b000000);
        bsTest.check(0b001100, -4, 0b000000);
        bsTest.check(0b001100, -5, 0b000001);
        bsTest.check(0b101000, 0, 0b101000);
        bsTest.check(0b101000, 1, 0b110100);
        bsTest.check(0b101000, 2, 0b111010);
        bsTest.check(0b101000, 3, 0b111101);
        bsTest.check(0b101000, 5, 0b111111);
        bsTest.check(0b101000, -1, 0b111111);
        bsTest.check(0b101000, -4, 0b111110);
        bsTest.check(0b101000, -5, 0b111101);
    }

    public void testArithmeticUnsignedRight32() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.arithmetic, false, LeftRightFormat.right, 32, 6);
        bsTest.check(0x80000000L, 0, 0x80000000L);
        bsTest.check(0x80000000L, 1, 0xc0000000L);
        bsTest.check(0x80000000L, 2, 0xe0000000L);
        bsTest.check(0x80000000L, 3, 0xf0000000L);
        bsTest.check(0x80000000L, 4, 0xf8000000L);
    }

    public void testArithmeticUnsignedRight33() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.arithmetic, false, LeftRightFormat.right, 33, 6);
        bsTest.check(0x100000000L, 0, 0x100000000L);
        bsTest.check(0x100000000L, 1, 0x180000000L);
        bsTest.check(0x100000000L, 2, 0x1c0000000L);
        bsTest.check(0x100000000L, 3, 0x1e0000000L);
        bsTest.check(0x100000000L, 4, 0x1f0000000L);
    }

    public void test64Bit() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 64, 7);
        bsTest.check(0xC000000000000000L, 0, 0xC000000000000000L);
        bsTest.check(0xC000000000000000L, 1, 0x8000000000000001L);
        bsTest.check(0x8000000000000001L, 1, 0x0000000000000003L);
        bsTest.check(0x4000000000000001L, 1, 0x8000000000000002L);

        bsTest = getTestExecuter(BarrelShifterMode.arithmetic, false, LeftRightFormat.left, 64, 7);
        bsTest.check(0xC000000000000000L, 0, 0xC000000000000000L);
        bsTest.check(0x4000000000000000L, 1, 0x8000000000000000L);
        bsTest.check(0x2000000000000000L, 1, 0x4000000000000000L);

        bsTest = getTestExecuter(BarrelShifterMode.logical, false, LeftRightFormat.left, 64, 7);
        bsTest.check(0xC000000000000000L, 0, 0xC000000000000000L);
        bsTest.check(0x4000000000000000L, 1, 0x8000000000000000L);
        bsTest.check(0x2000000000000000L, 1, 0x4000000000000000L);

        bsTest = getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.right, 64, 7);
        bsTest.check(0xC000000000000001L, 0, 0xC000000000000001L);
        bsTest.check(0x8000000000000001L, 1, 0xC000000000000000L);
        bsTest.check(0x4000000000000001L, 1, 0xA000000000000000L);

        bsTest = getTestExecuter(BarrelShifterMode.arithmetic, false, LeftRightFormat.right, 64, 7);
        bsTest.check(0xC000000000000000L, 0, 0xC000000000000000L);
        bsTest.check(0x8000000000000000L, 1, 0xC000000000000000L);
        bsTest.check(0x4000000000000000L, 1, 0x2000000000000000L);

        bsTest = getTestExecuter(BarrelShifterMode.logical, false, LeftRightFormat.right, 64, 7);
        bsTest.check(0xC000000000000000L, 0, 0xC000000000000000L);
        bsTest.check(0x8000000000000000L, 1, 0x4000000000000000L);
        bsTest.check(0x4000000000000000L, 1, 0x2000000000000000L);
    }

    public void testShiftSizeCalculationTest() throws Exception {
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 1, 1);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 2, 2);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 3, 2);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 4, 3);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 7, 3);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 8, 4);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 15, 4);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 16, 5);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 31, 5);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 32, 6);

        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 1, 2);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 2, 3);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 3, 3);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 4, 4);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 7, 4);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 8, 5);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 15, 5);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 16, 6);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 31, 6);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 32, 7);
    }

    private TestExecuter getTestExecuter(BarrelShifterMode mode, boolean signed, LeftRightFormat direction, int valueWidth, int shiftWidth) throws Exception {
        ObservableValue value = new ObservableValue("value", valueWidth);
        ObservableValue shift = new ObservableValue("shift", shiftWidth);

        ElementAttributes attributes = new ElementAttributes()
                .set(Keys.BARREL_SHIFTER_MODE, mode)
                .set(Keys.BARREL_SIGNED, signed)
                .set(Keys.DIRECTION, direction)
                .set(Keys.BITS, valueWidth);

        Model model = new Model();
        BarrelShifter bs = new BarrelShifter(attributes);

        bs.setInputs(ovs(value, shift));
        model.add(bs);

        ObservableValues outputs = bs.getOutputs();
        assertEquals(1, outputs.size());
        assertEquals(value.getBits(), outputs.get(0).getBits());

        return new TestExecuter(model).setInputs(value, shift).setOutputs(outputs);
    }


}
