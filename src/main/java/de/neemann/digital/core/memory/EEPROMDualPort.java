/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.rom.ROMInterface;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A EEPROM module.
 */
public class EEPROMDualPort extends RAMDualPort implements ROMInterface {

    /**
     * The EEPROMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(EEPROMDualPort.class,
            input("A"),
            input("Din"),
            input("str"),
            input("C").setClock(),
            input("ld"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DATA);

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public EEPROMDualPort(ElementAttributes attr) {
        super(attr);
    }

    @Override
    protected DataField createDataField(ElementAttributes attr, int size) {
        DataField memory = attr.get(Keys.DATA);
        if (memory.size() != size) {
            memory = new DataField(memory, size);
            attr.set(Keys.DATA, memory);
        }
        return memory;
    }

}
