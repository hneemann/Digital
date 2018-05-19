/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * The Input
 */
public class DipSwitch implements Element {

    /**
     * The inputs description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(DipSwitch.class) {
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
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DIP_DEFAULT)
            .addAttribute(Keys.DESCRIPTION);

    private final ObservableValue output;

    /**
     * Create a new instance
     *
     * @param attributes the inputs attributes
     */
    public DipSwitch(ElementAttributes attributes) {
        int value = attributes.get(Keys.DIP_DEFAULT) ? 1 : 0;
        output = new ObservableValue("out", 1)
                .setPinDescription(DESCRIPTION);
        output.setValue(value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
    }

}
