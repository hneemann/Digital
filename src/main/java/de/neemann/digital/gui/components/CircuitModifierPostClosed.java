/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelEventType;
import de.neemann.digital.core.ModelStateObserverTyped;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.Modification;
import de.neemann.digital.undo.Modifications;

import javax.swing.*;

/**
 * Allows the model to modify the circuit
 */
public class CircuitModifierPostClosed implements CircuitModifier, ModelStateObserverTyped {

    private final Modifications.Builder<Circuit> builder;
    private final CircuitModifier circuitModifier;

    /**
     * Creates a new instance
     *
     * @param circuitModifier the parent modifier used to modify the circuit
     */
    public CircuitModifierPostClosed(CircuitModifier circuitModifier) {
        this.circuitModifier = circuitModifier;
        builder = new Modifications.Builder<>(Lang.get("mod_modifiedByRunningModel"));
    }

    @Override
    public void modify(Modification<Circuit> modification) {
        builder.add(modification);
    }

    @Override
    public ModelEventType[] getEvents() {
        return new ModelEventType[]{ModelEventType.POSTCLOSED};
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event.getType().equals(ModelEventType.POSTCLOSED)) {
            Modification<Circuit> m = builder.build();
            if (m != null)
                circuitModifier.modify(m);
        }
    }
}
