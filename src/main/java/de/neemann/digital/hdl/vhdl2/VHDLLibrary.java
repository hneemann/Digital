/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.HDLNode;
import de.neemann.digital.hdl.vhdl2.entities.VHDLEntity;
import de.neemann.digital.hdl.vhdl2.entities.VHDLTemplate;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class VHDLLibrary {
    private static final Logger LOGGER = LoggerFactory.getLogger(VHDLLibrary.class);
    private final HDLModel model;

    private HashMap<String, VHDLEntity> map;

    public VHDLLibrary(HDLModel model) {
        this.model = model;
        map = new HashMap<>();
    }

    public VHDLEntity getEntity(HDLNode node) throws HDLException {
        String elementName = node.getElementName();
        VHDLEntity e = map.get(elementName);
        if (e == null) {

            HDLCircuit c = model.getCustomCircuit(elementName);
            try {
                e = new VHDLTemplate(elementName);
                map.put(elementName, e);
            } catch (IOException ex) {
                ex.printStackTrace();
                LOGGER.info("could not load '" + VHDLTemplate.neededFileName(elementName) + "'");
            }
        }

        if (e == null)
            throw new HDLException(Lang.get("err_vhdlNoEntity_N", elementName));
        return e;
    }
}
