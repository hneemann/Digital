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
import java.util.List;

/**
 * A Modifier that loads a given rom file to the program memory of the model.
 */
public class ProgramMemoryLoader implements ModelModifier {
    private final File romHex;

    /**
     * Creates a new rom modifier
     *
     * @param romHex the file to load
     */
    ProgramMemoryLoader(File romHex) {
        this.romHex = romHex;
    }

    @Override
    public void preInit(Model model) throws NodeException {
        List<Node> progMem = model.findNode(n -> n instanceof ProgramMemory && ((ProgramMemory) n).isProgramMemory());
        if (progMem.isEmpty())
            throw new NodeException(Lang.get("err_noRomFound"));
        if (progMem.size() > 1)
            throw new NodeException(Lang.get("err_moreThenOneRomFound"));

        try {
            ((ProgramMemory) progMem.get(0)).setProgramMemory(new DataField(romHex));
        } catch (IOException e) {
            throw new NodeException(Lang.get("err_errorLoadingRomData"), e);
        }
    }
}
