/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMInterface;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.ModelEntry;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.DataEditor;

import java.awt.*;

/**
 * The RAM shape
 */
public class RAMShape extends GenericShape {
    private final int dataBits;
    private final int addrBits;
    private final String dialogTitle;
    private Model model;

    /**
     * Creates a new instance
     *
     * @param attr        the attributes of the element
     * @param description element type description
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public RAMShape(ElementAttributes attr, ElementTypeDescription description) throws NodeException, PinException {
        this(attr, description, 3);
    }

    /**
     * Creates a new instance
     *
     * @param attr        the attributes of the element
     * @param description element type description
     * @param width       the used width
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public RAMShape(ElementAttributes attr, ElementTypeDescription description, int width) throws NodeException, PinException {
        super(description.getShortName(),
                description.getInputDescription(attr),
                description.getOutputDescriptions(attr),
                attr.getLabel(), true, width);
        if (attr.getLabel().length() > 0)
            dialogTitle = attr.getLabel();
        else
            dialogTitle = description.getShortName();
        dataBits = attr.get(Keys.BITS);
        addrBits = attr.get(Keys.ADDR_BITS);
        setInverterConfig(attr.get(Keys.INVERTER_CONFIG));
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return new Interactor() {
            @Override
            public void clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
                if (element instanceof RAMInterface) {
                    RAMInterface ram = (RAMInterface) element;
                    DataField dataField = ram.getMemory();
                    DataEditor dataEditor = new DataEditor(cc, dataField, dataBits, addrBits, true, modelSync, ram.getValueFormatter());
                    if (element instanceof Node)
                        dataEditor.setNode((Node) element);
                    dataEditor.showDialog(dialogTitle, model);
                }
            }
        };
    }

    @Override
    public void registerModel(ModelCreator modelCreator, Model model, ModelEntry element) {
        this.model = model;
    }
}
