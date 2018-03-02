/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserverTyped;
import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.components.CircuitComponent;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This observer is added to the model if real time timers are started.
 * This observer paints the CircuitComponent after a step is calculated.
 * It is registered to all elements which visual representation depends on a model value.
 * This listener method only sets a flag if there was a change.
 * For repainting it is also registered to the model to repaint the circuit if the step is complete.
 */
public class GuiModelObserver implements Observer, ModelStateObserverTyped {
    private static final long TIMEOUT = 100;
    private final CircuitComponent component;
    private final ModelEvent type;
    private final AtomicBoolean paintPending = new AtomicBoolean();
    private long lastUpdateTime;
    private boolean changed = false;

    /**
     * Creates a new instance.
     *
     * @param component the component to update
     * @param type      the event type which triggers a repainting
     */
    public GuiModelObserver(CircuitComponent component, ModelEvent type) {
        this.component = component;
        this.type = type;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void hasChanged() {
        changed = true;
    }

    @Override
    public void handleEvent(ModelEvent event) {
        long time = System.currentTimeMillis();
        boolean timeOut = time - lastUpdateTime > TIMEOUT;
        if ((changed || timeOut) && event == type) {
            if (paintPending.compareAndSet(false, true)) {
                lastUpdateTime = time;
                SwingUtilities.invokeLater(() -> {
                    component.paintImmediately();
                    paintPending.set(false);
                });
            }
            changed = false;
        }
    }

    @Override
    public ModelEvent[] getEvents() {
        return new ModelEvent[]{type};
    }
}
