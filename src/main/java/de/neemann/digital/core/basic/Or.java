/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import java.util.ArrayList;

/**
 * The Or
 */
public class Or extends Function {

    /**
     * The And description
     */
    public static final ElementTypeDescription DESCRIPTION = new FanInDescription(Or.class).addAttribute(Keys.WIDE_SHAPE);

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Or(ElementAttributes attributes) {
        super(attributes.get(Keys.BITS));
    }

    @Override
    protected long calculate(ArrayList<ObservableValue> inputs) throws NodeException {
        long f = 0;
        for (ObservableValue i : inputs) {
            f |= i.getValue();
        }
        return f;
    }
}
