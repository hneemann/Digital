/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;

import java.util.HashMap;

public class HDLContext {
    private ElementLibrary elementLibrary;
    private HashMap<Circuit, HDLCircuit> circuitMap;

    public HDLContext(ElementLibrary elementLibrary) {
        this.elementLibrary = elementLibrary;
        circuitMap = new HashMap<>();
    }

    public HDLNode createNode(VisualElement v) throws HDLException {
        try {
            ElementTypeDescription td = elementLibrary.getElementType(v.getElementName());
            if (td instanceof ElementLibrary.ElementTypeDescriptionCustom) {
                ElementLibrary.ElementTypeDescriptionCustom tdc = (ElementLibrary.ElementTypeDescriptionCustom) td;

                HDLCircuit c = circuitMap.get(tdc.getCircuit());
                if (c == null) {
                    c = new HDLCircuit(tdc.getCircuit(), v.getElementName(), this);
                    circuitMap.put(tdc.getCircuit(), c);
                }

                return new HDLNodeCustom(v.getElementName(), v.getElementAttributes(), c);
            } else
                return new HDLNode(v.getElementName(),
                        v.getElementAttributes(),
                        new ObserVableValuesBits(
                                td.createElement(v.getElementAttributes()).getOutputs()));

        } catch (ElementNotFoundException | PinException | NodeException e) {
            throw new HDLException("error creating node", e);
        }
    }

    public interface BitProvider {
        int getBits(String name);
    }

    private class ObserVableValuesBits implements BitProvider {
        private final ObservableValues values;

        private ObserVableValuesBits(ObservableValues values) {
            this.values = values;
        }

        @Override
        public int getBits(String name) {
            return values.get(name).getBits();
        }
    }
}
