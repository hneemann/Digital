package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A Modifier that loads a given rom file to the program memory of the model.
 * Created by hneemann on 17.12.16.
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
        List<ROM> roms = model.findNode(ROM.class, ROM::isProgramMemory);
        if (roms.isEmpty())
            throw new NodeException(Lang.get("msg_noRomFound"));
        if (roms.size() > 1)
            throw new NodeException(Lang.get("msg_moreThenOneRomFound"));

        try {
            roms.get(0).setData(new DataField(romHex));
            roms.get(0).provideRomAdress(model);
        } catch (IOException e) {
            throw new NodeException(e.getMessage());
        }
    }
}
