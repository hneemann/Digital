package de.neemann.digital.core.arithmetic;

import static de.neemann.digital.core.ObservableValues.ovs;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

/**
 * Created by heintz on 05.07.17.
 */
public class BarrelShifterTest extends TestCase {

    public void testNormalUnsignedLeft() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.normal, false, LeftRightFormat.left, 6, 3);
        bsTest.check(0b001100, 0, 0b001100);
        bsTest.check(0b001100, 1, 0b011000);
        bsTest.check(0b001100, 2, 0b110000);
        bsTest.check(0b010001, 2, 0b000100);
        bsTest.check(0b001100, 3, 0b100000);
        bsTest.check(0b001100, 5, 0b000000);
        bsTest.check(0b001100, -1, 0b000000);
        bsTest.check(0b001100, -4, 0b000000);
        bsTest.check(0b001100, -5, 0b100000);
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
    }

    public void testNormalSignedLeft() throws Exception {
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.normal, true, LeftRightFormat.left, 6, 4);
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
        TestExecuter bsTest = getTestExecuter(BarrelShifterMode.normal, false, LeftRightFormat.right, 6, 3);
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

    public void testShiftSizeCalculationTest() throws Exception {
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 7, 3);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 7, 4);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.right, 7, 4);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 8, 3);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 8, 4);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.right, 8, 4);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 9, 4);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 9, 5);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.right, 9, 5);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 16, 4);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 16, 5);
        getTestExecuter(BarrelShifterMode.rotate, false, LeftRightFormat.left, 17, 5);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.left, 17, 6);
        getTestExecuter(BarrelShifterMode.rotate, true, LeftRightFormat.right, 17, 6);
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