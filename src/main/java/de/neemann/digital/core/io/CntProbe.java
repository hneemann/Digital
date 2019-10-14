/*
 * Copyright (c) 2016 Helmut Neemann, Mats Engstrom
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The counting probe CntProbe
 */
public class CntProbe extends Node implements Element {

    /**
     * The possible edges
     */
    public enum Edges {
    /**
     * Trigger on the rising edge only
     */
    rising,

    /**
     * Trigger on the falling edge only
     */
    falling,

    /**
     * Trigger on both edges
     */
    both
 }


    /**
     * The CntProbe description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("CntProbe", CntProbe.class,
            input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.CNTPROBE_EDGE);

    private final String label;
    private final Edges edge;
    private ObservableValue value;
    private boolean lastValue;
    private int cnt;


    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public CntProbe(ElementAttributes attributes) {
        label = attributes.get(Keys.LABEL);
        edge = attributes.get(Keys.CNTPROBE_EDGE);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        value = inputs.get(0).checkBits(1, null, 0).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean nowValue=value.getBool();
        if (nowValue != lastValue) {
            if (
                (edge==Edges.rising && nowValue)
                || (edge==Edges.falling && !nowValue)
                || (edge==Edges.both)
               ) cnt++;
        }
        lastValue=nowValue;
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    /**
     * Returns the counts
     *
     * @return cnt
     */
    public int getCnt() {
        return cnt;
    }

}
