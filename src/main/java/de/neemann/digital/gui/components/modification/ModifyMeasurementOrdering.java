package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Modifies the measurement ordering
 * Created by hneemann on 30.05.17.
 */
public class ModifyMeasurementOrdering implements Modification {
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
    public void modify(Circuit circuit, ElementLibrary library) {
        circuit.setMeasurementOrdering(names);
    }

    @Override
    public String toString() {
        return Lang.get("mod_modifiedMeasurementOrdering");
    }
}
