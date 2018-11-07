/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.NodeInterface;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

/**
 * A simple fuse.
 */
public class Fuse implements Element, NodeInterface {

    /**
     * The fuse description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Fuse.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BLOWN);

    private final PlainSwitch s;

    /**
     * Create a new fuse
     *
     * @param attr the attributes
     */
    public Fuse(ElementAttributes attr) {
        s = new PlainSwitch(attr.getBits(), !attr.get(Keys.BLOWN), "out1", "out2");
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        s.setInputs(inputs.get(0), inputs.get(1));
    }

    @Override
    public ObservableValues getOutputs() {
        return s.getOutputs();
    }

    @Override
    public void registerNodes(Model model) {
    }

    @Override
    public void init(Model model) {
        s.init(model);
    }

    @Override
    public void hasChanged() {
        s.hasChanged();
    }
}
