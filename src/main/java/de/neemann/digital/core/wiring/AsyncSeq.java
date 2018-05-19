/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 */
public class AsyncSeq implements Element {

    /**
     * the clocks description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(AsyncSeq.class)
            .addAttribute(Keys.RUN_AT_REAL_TIME)
            .addAttribute(Keys.FREQUENCY);

    private final int frequency;

    /**
     * Creates a new instance
     *
     * @param attributes the clocks attributes
     */
    public AsyncSeq(ElementAttributes attributes) {
        if (attributes.get(Keys.RUN_AT_REAL_TIME)) {
            int f = attributes.get(Keys.FREQUENCY);
            if (f < 1) f = 1;
            frequency = f;
        } else
            frequency = 0;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"));
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
        model.setAsyncInfos(this);
    }

    /**
     * @return the clocks frequency
     */
    public int getFrequency() {
        return frequency;
    }

}
