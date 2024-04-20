/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.ValueFormatter;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The different outputs
 */
public class Out implements Element {

    /**
     * The Input description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("Out", attributes -> new Out(attributes).enforceName(), input("in")) {
        @Override
        public String getDescription(ElementAttributes elementAttributes) {
            String d = Lang.evalMultilingualContent(elementAttributes.get(Keys.DESCRIPTION));
            if (d.length() > 0)
                return d;
            else
                return super.getDescription(elementAttributes);
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DESCRIPTION)
            .addAttribute(Keys.INT_FORMAT)
            .addAttribute(Keys.PINNUMBER)
            .addAttribute(Keys.ADD_VALUE_TO_GRAPH)
            .addAttribute(Keys.IN_OUT_SMALL)
            .supportsHDL();

    /**
     * The LED description
     */
    public static final ElementTypeDescription LEDDESCRIPTION
            = new ElementTypeDescription("LED", Out.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.LED_SIZE)
            .addAttribute(Keys.ADD_VALUE_TO_GRAPH)
            .addAttribute(Keys.COLOR);

    /**
     * The polarity aware LED description
     */
    public static final ElementTypeDescription POLARITYAWARELEDDESCRIPTION
            = new ElementTypeDescription("PolarityAwareLED",
            attributes -> new Out(attributes, 1, 1), input("A"), input("C"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.ADD_VALUE_TO_GRAPH)
            .addAttribute(Keys.COLOR);

    /**
     * The seven segment display description
     */
    public static final ElementTypeDescription SEVENDESCRIPTION = new SevenSegTypeDescription();

    /**
     * The seven segment hex display description
     */
    public static final ElementTypeDescription SEVENHEXDESCRIPTION
            = new ElementTypeDescription("Seven-Seg-Hex",
            attributes -> new Out(attributes, 4, 1), input("d"), input("dp"))
            .addAttribute(Keys.COLOR)
            .addAttribute(Keys.SEVEN_SEG_SIZE);

    /**
     * Sixteen Segment Display
     */
    public static final ElementTypeDescription SIXTEENDESCRIPTION
            = new ElementTypeDescription("SixteenSeg",
            attributes -> new Out(attributes, 16, 1), input("led"), input("dp"))
            .addAttribute(Keys.COLOR)
            .addAttribute(Keys.SEVEN_SEG_SIZE);

    private final int[] bits;
    private final String label;
    private final String pinNumber;
    private final ValueFormatter formatter;
    private final boolean showInGraph;
    private boolean enforceSignal = false;
    private ObservableValue value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Out(ElementAttributes attributes) {
        bits = new int[]{attributes.getBits()};
        label = attributes.getLabel();
        pinNumber = attributes.get(Keys.PINNUMBER);
        formatter = attributes.getValueFormatter();
        showInGraph = attributes.get(Keys.ADD_VALUE_TO_GRAPH);
    }

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param bits       the bitcount of the different inputs
     */
    public Out(ElementAttributes attributes, int... bits) {
        this.bits = bits;
        label = attributes.getLabel();
        pinNumber = "";
        formatter = null;
        showInGraph = attributes.get(Keys.ADD_VALUE_TO_GRAPH);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        if (inputs.size() != bits.length)
            throw new NodeException("wrong input count");
        value = inputs.get(0).checkBits(bits[0], null);
        for (int i = 1; i < bits.length; i++)
            inputs.get(i).checkBits(bits[i], null);
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
        final Signal signal = new Signal(label, value)
                .setPinNumber(pinNumber)
                .setShowInGraph(showInGraph)
                .setFormat(formatter);
        if (enforceSignal || signal.isValid())
            model.addOutput(signal);
    }

    private Element enforceName() {
        enforceSignal = true;
        return this;
    }

    private final static class SevenSegTypeDescription extends ElementTypeDescription {
        private SevenSegTypeDescription() {
            super("Seven-Seg", attributes -> {
                if (attributes.get(Keys.COMMON_CONNECTION))
                    return new Out(attributes, 1, 1, 1, 1, 1, 1, 1, 1, 1);
                else
                    return new Out(attributes, 1, 1, 1, 1, 1, 1, 1, 1);
            });
            addAttribute(Keys.COLOR);
            addAttribute(Keys.COMMON_CONNECTION);
            addAttribute(Keys.COMMON_CONNECTION_TYPE);
            addAttribute(Keys.LED_PERSIST_TIME);
        }

        @Override
        public PinDescriptions getInputDescription(ElementAttributes attributes) {
            if (attributes.get(Keys.COMMON_CONNECTION)) {
                if (attributes.get(Keys.COMMON_CONNECTION_TYPE).equals(CommonConnectionType.anode))
                    return new PinDescriptions(
                            input("a"), input("b"), input("c"),
                            input("d"), input("e"), input("f"),
                            input("g"), input("dp"), input("ca")).setLangKey(getPinLangKey());
                else
                    return new PinDescriptions(
                            input("a"), input("b"), input("c"),
                            input("d"), input("e"), input("f"),
                            input("g"), input("dp"), input("cc")).setLangKey(getPinLangKey());
            } else
                return new PinDescriptions(
                        input("a"), input("b"), input("c"),
                        input("d"), input("e"), input("f"),
                        input("g"), input("dp")).setLangKey(getPinLangKey());
        }
    }
}
