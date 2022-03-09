/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.rom;

import de.neemann.digital.core.memory.DataField;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

/**
 * The Manager to manage all necessary rom images
 */
public class ROMManager extends ROMMangerBase {

    private final HashMap<String, DataField> roms;

    /**
     * Creates a new instance
     */
    public ROMManager() {
        roms = new HashMap<>();
    }

    @Override
    public DataField getRom(String label, int dataBits, File origin) throws IOException {
        return roms.get(label);
    }

    @Override
    public boolean isEmpty() {
        return roms.isEmpty();
    }

    /**
     * @return a list of stored roms
     */
    protected HashMap<String, DataField> getRoms() {
        return roms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ROMManager romManger = (ROMManager) o;
        return Objects.equals(roms, romManger.roms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roms);
    }
}
