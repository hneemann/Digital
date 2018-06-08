/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

/**
 * Only a placeholder.
 * Has no connections to the model!
 */
public class DummyElement implements Element {

    /**
     * The DataElement description
     */
    public static final ElementTypeDescription DATADESCRIPTION = new ElementTypeDescription("Data", DummyElement.class)
            .addAttribute(Keys.MICRO_STEP)
            .addAttribute(Keys.MAX_STEP_COUNT);

    /**
     * The TextElement description
     */
    public static final ElementTypeDescription TEXTDESCRIPTION = new ElementTypeDescription("Text", DummyElement.class)
            .addAttribute(Keys.DESCRIPTION)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.TEXT_ORIENTATION)
            .addAttribute(Keys.FONT_SIZE);

    /**
     * Creates a new dummy element
     *
     * @param attr the attributes
     */
    public DummyElement(ElementAttributes attr) {
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
    }

}
