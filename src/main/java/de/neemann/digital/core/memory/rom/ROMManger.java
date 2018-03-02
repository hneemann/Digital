/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.rom;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.memory.DataField;

import java.util.HashMap;

/**
 * The Manager to manage all necessary rom images
 */
public class ROMManger {
    /**
     * The empty instance
     */
    public static final ROMManger EMPTY = new ROMManger();

    private final HashMap<String, DataField> roms;

    /**
     * Creates a new instance
     */
    public ROMManger() {
        roms = new HashMap<>();
    }

    /**
     * Applies the available roms to the model
     *
     * @param model the mode to use
     */
    public void applyTo(Model model) {
        for (Node n : model.findNode(n -> n instanceof ROMInterface)) {
            ROMInterface rom = (ROMInterface) n;
            DataField data = roms.get(rom.getLabel());
            if (data != null)
                rom.setData(data);
        }
    }

    /**
     * Returns the rom content of the given name
     *
     * @param label the roms label
     * @return the stored data
     */
    public DataField getRom(String label) {
        return roms.get(label);
    }

    /**
     * Adds a rom's contet to this ROMManager
     *
     * @param label the label
     * @param data  the data
     */
    public void addRom(String label, DataField data) {
        data = data.getMinimized();
        if (data.getData().length > 0)
            roms.put(label, data);
    }

    /**
     * @return returns EMPTY it this ROMManager is empty, this otherwise
     */
    public ROMManger getMinimized() {
        if (roms.isEmpty())
            return EMPTY;
        return this;
    }

    /**
     * @return true if no ROM's are stored
     */
    public boolean isEmpty() {
        return roms.isEmpty();
    }
}
