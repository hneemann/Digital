/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ProgramMemory;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Modifier that loads a given rom file to the program memory of the model.
 */
public class RomLoader implements ModelModifier {
    private final File romHex;

    /**
     * Creates a new rom modifier
     *
     * @param romHex the file to load
     */
    RomLoader(File romHex) {
        this.romHex = romHex;
    }

    @Override
    public void preInit(Model model) throws NodeException {
        List<ProgramMemory> roms = new ArrayList<>();
        for (Node n : model)
            if (n instanceof ProgramMemory) {
                ProgramMemory pr = (ProgramMemory) n;
                if (pr.isProgramMemory())
                    roms.add(pr);
            }
        if (roms.isEmpty())
            throw new NodeException(Lang.get("msg_noRomFound"));
        if (roms.size() > 1)
            throw new NodeException(Lang.get("msg_moreThenOneRomFound"));

        try {
            roms.get(0).setProgramMemory(new DataField(romHex));
        } catch (IOException e) {
            throw new NodeException(e.getMessage());
        }
    }
}
