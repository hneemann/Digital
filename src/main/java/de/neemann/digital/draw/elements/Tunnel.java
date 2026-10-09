/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ValueFormatter;
import de.neemann.digital.core.element.*;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * Allows a tunneling of wires to make the schematic more readable then drawing
 * long wires.
 */
public class Tunnel implements Element {

    /**
     * The position of the value text relative to the tunnel
     */
    public enum Position {
        /**
         * Left of the tunnel
         */
        LEFT,
        /**
         * Right of the tunnel
         */
        RIGHT,
        /**
         * Above the tunnel
         */
        TOP,
        /**
         * Below the tunnel
         */
        BOTTOM
    }

    /**
     * key to enable the value display
     */
    public static final Key<Boolean> SHOW_VALUE = new Key<>("showValue", false);

    /**
     * key for the position of the value text
     */
    public static final Key<Position> VALUE_POS = new Key.KeyEnum<>("valPos", Position.BOTTOM, Position.values());

    /**
     * The TunnelElement description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Tunnel.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.NETNAME)
            .addAttribute(Keys.INT_FORMAT)
            .addAttribute(SHOW_VALUE)
            .addAttribute(VALUE_POS)
            .supportsHDL();

    private final String label;
    private ObservableValue value;
    private final ValueFormatter formatter;
    private final boolean showValue;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Tunnel(ElementAttributes attributes) {
        this.label = attributes.getLabel();
        this.formatter = attributes.getValueFormatter();
        this.showValue = attributes.get(SHOW_VALUE);
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the formatter
     */
    public ValueFormatter getFormatter() {
        return formatter;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        value = inputs.get(0);
    }

    /**
     * @return the tunneled value
     */
    public ObservableValue getValue() {
        return value;
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
    }
}
