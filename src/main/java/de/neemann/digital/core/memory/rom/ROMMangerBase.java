/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.rom;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;

/**
 * The Manager to manage all necessary rom images
 */
public abstract class ROMMangerBase {

    /**
     * Applies the available roms to the model
     *
     * @param model  the mode to use
     * @param origin the file origin
     * @throws NodeException NodeException
     */
    public void applyTo(Model model, File origin) throws NodeException {
        if (isEmpty())
            return;
        for (Node n : model.findNode(n -> n instanceof ROMInterface)) {
            ROMInterface rom = (ROMInterface) n;
            DataField data;
            try {
                data = getRom(rom.getLabel(), rom.getDataBits(), origin);
            } catch (IOException e) {
                throw new NodeException(Lang.get("err_could_not_load_rom"), e);
            }
            if (data != null)
                rom.setData(data);
        }
    }

    /**
     * @return true if no ROM's are stored
     */
    public abstract boolean isEmpty();

    /**
     * Returns the rom content of the given name
     *
     * @param label    the roms label
     * @param dataBits the data bit needed
     * @param origin   the file origin
     * @return the stored data
     * @throws IOException IOException
     */
    public abstract DataField getRom(String label, int dataBits, File origin) throws IOException;

}
