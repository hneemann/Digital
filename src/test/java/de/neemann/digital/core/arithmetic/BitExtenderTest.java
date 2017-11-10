package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

public class BitExtenderTest extends TestCase {

    public void testSignExtend() throws Exception {
        ObservableValue in = new ObservableValue("in", 4);

        BitExtender bitExtender = new BitExtender(new ElementAttributes().set(Keys.OUTPUT_BITS, 8));
        bitExtender.setInputs(in.asList());
        assertEquals(1, bitExtender.getOutputs().size());
        ObservableValue out = bitExtender.getOutputs().get(0);

        check(in, out, 0, 0);
        check(in, out, 1, 1);
        check(in, out, 7, 7);
        check(in, out, 8, -8);
        check(in, out, 9, -7);
        check(in, out, -1, -1);
        check(in, out, -2, -2);
    }

    public void testSignExtendInit() throws Exception {
        ObservableValue in = new ObservableValue("in", 4).setValue(1);

        BitExtender bitExtender = new BitExtender(new ElementAttributes().set(Keys.OUTPUT_BITS, 8));
        bitExtender.setInputs(in.asList());

        assertEquals(1, bitExtender.getOutputs().get(0).getValue());
    }


    private void check(ObservableValue in, ObservableValue out, int inVal, int outVal) {
        in.setValue(inVal);
        assertEquals(outVal, (byte) out.getValue());
    }

    public void testSignExtendError() throws Exception {
        try {
            ObservableValue in = new ObservableValue("in", 4);
            new BitExtender(new ElementAttributes().set(Keys.OUTPUT_BITS, 4)).setInputs(in.asList());
            fail();
        } catch (NodeException e) {
        }
    }

    public void testSignExtendError2() throws Exception {
        try {
            ObservableValue in = new ObservableValue("in", 5);
            new BitExtender(new ElementAttributes().set(Keys.OUTPUT_BITS, 4)).setInputs(in.asList());
            fail();
        } catch (NodeException e) {
        }
    }
}