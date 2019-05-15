/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Modification;

import java.util.ArrayList;

/**
 * Modifies the measurement ordering
 */
public class ModifyMeasurementOrdering implements Modification<Circuit> {
    private final ArrayList<String> names;

    /**
     * Creates a new instance
     *
     * @param names the new ordering
     */
    public ModifyMeasurementOrdering(ArrayList<String> names) {
        this.names = names;
    }

    @Override
    public void modify(Circuit circuit) {
        circuit.setMeasurementOrdering(names);
    }

    @Override
    public String toString() {
        return Lang.get("mod_modifiedMeasurementOrdering");
    }
}
