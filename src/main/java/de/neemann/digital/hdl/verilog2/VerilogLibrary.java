/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLNode;
import de.neemann.digital.hdl.verilog2.lib.VerilogElement;
import de.neemann.digital.hdl.verilog2.lib.VerilogTemplate;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ideras
 */
public class VerilogLibrary {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerilogLibrary.class);

    private final HashMap<String, VerilogElement> map;
    private final ArrayList<HDLNode> nodeList = new ArrayList<>();
    private final ElementLibrary elementLibrary;

    /**
     * Creates a new instance
     *
     * @param elementLibrary the element library used
     */
    public VerilogLibrary(ElementLibrary elementLibrary) {
        this.elementLibrary = elementLibrary;
        map = new HashMap<>();
    }

    private void put(ElementTypeDescription description, VerilogElement velem) {
        map.put(description.getName(), velem);
    }

    /**
     * Returns the associated verilog element for a given node
     *
     * @param node the HDL node
     * @return the associated verilog element.
     * @throws HDLException HDLException
     */
    public VerilogElement getVerilogElement(HDLNode node) throws HDLException {
        String elementName = node.getElementName();
        VerilogElement e = map.get(elementName);
        if (e == null) {
            try {
                ClassLoader cl = null;
                try {
                    cl = elementLibrary.getElementType(elementName).getClassLoader();
                } catch (ElementNotFoundException ex) {
                    // try default
                }

                e = new VerilogTemplate(elementName, cl);
                map.put(elementName, e);
            } catch (IOException ex) {
                ex.printStackTrace();
                LOGGER.info("could not load '" + VerilogTemplate.neededFileName(elementName) + "'");
            }
        }

        if (e == null)
            throw new HDLException(Lang.get("err_verilogNoElement_N", elementName));

        return e;
    }

    /**
     * Returns the verilog name of the given node
     *
     * @param node the node
     * @return the name
     */
    public String getName(HDLNode node) {
        if (!nodeList.contains(node)) {
            nodeList.add(node);
            node.setHdlEntityName(node.getElementName());

        }
        return node.getHdlEntityName();
    }
}
