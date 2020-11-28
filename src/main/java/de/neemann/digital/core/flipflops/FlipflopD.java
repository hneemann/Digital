/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.stats.Countable;

import static de.neemann.digital.core.ObservableValues.ovs;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The D Flipflop
 */
public class FlipflopD extends Node implements Element, Countable {

    /**
     * The D-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("D_FF", FlipflopD.class, input("D"), input("C").setClock())
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE)
            .supportsHDL();

    private final int bits;
    private final boolean isProbe;
    private final String label;
    private ObservableValue dVal;
    private ObservableValue clockVal;
    private ObservableValue q;
    private ObservableValue qn;
    private boolean lastClock;
    private long value;
    private long defaultValue;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public FlipflopD(ElementAttributes attributes) {
        this(attributes,
                new ObservableValue("Q", attributes.getBits()).setPinDescription(DESCRIPTION),
                new ObservableValue("~Q", attributes.getBits()).setPinDescription(DESCRIPTION));
    }

    /**
     * Creates a new D-FF with the given outputs!
     *
     * @param label the label
     * @param q     output
     * @param qn    inverted output
     * @param def   the default value
     */
    public FlipflopD(String label, ObservableValue q, ObservableValue qn, long def) {
        this(new ElementAttributes()
                .set(Keys.LABEL, label)
                .setBits(q.getBits())
                .set(Keys.DEFAULT, def), q, qn);
        if (qn.getBits() != q.getBits())
            throw new RuntimeException("wrong bit count given!");
    }

    FlipflopD(ElementAttributes attributes, ObservableValue q, ObservableValue qn) {
        super(true);
        bits = attributes.getBits();
        this.q = q;
        this.qn = qn;
        isProbe = attributes.get(Keys.VALUE_IS_PROBE);
        label = attributes.getLabel();

        defaultValue = attributes.get(Keys.DEFAULT);
        value = defaultValue;
        q.setValue(value);
        qn.setValue(~value);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock)
            value = dVal.getValue();
        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setValue(value);
        qn.setValue(~value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        dVal = inputs.get(0).checkBits(bits, this, 0);
        clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
    }

    @Override
    public ObservableValues getOutputs() {
        return ovs(q, qn);
    }

    @Override
    public void registerNodes(Model model) {
        super.registerNodes(model);
        if (isProbe)
            model.addSignal(new Signal(label, q, (v, z) -> {
                value = v;
                q.setValue(value);
                qn.setValue(~value);
            }).setTestOutput());
    }

    /**
     * @return the D input
     */
    public ObservableValue getDInput() {
        return dVal;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the clock value
     */
    public ObservableValue getClock() {
        return clockVal;
    }

    @Override
    public int getDataBits() {
        return bits;
    }

    void setValue(long value) {
        this.value = value;
    }

    /**
     * @return the default value
     */
    public long getDefault() {
        return defaultValue;
    }

}
