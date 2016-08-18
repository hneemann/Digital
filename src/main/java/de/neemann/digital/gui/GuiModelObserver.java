package de.neemann.digital.gui;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.components.CircuitComponent;

import javax.swing.*;

/**
 * This observer is added to the model if real time timers are started.
 * This observer paints the CircuitComponent after a step is calculated.
 * It is registered to all elements which visual representation depends on a model value.
 * This listener method only sets a flag if there was a change.
 * For repainting it is also registered to the model to repaint the circuit if the step is complete.
 *
 * @author hneemann
 */
public class GuiModelObserver implements Observer, ModelStateObserver {
    private final CircuitComponent component;
    private final ModelEvent type;
    private boolean changed = false;
    private volatile boolean paintPending;

    /**
     * Creates a new instance.
     *
     * @param component the component to update
     * @param type      the event type which triggers a repainting
     */
    public GuiModelObserver(CircuitComponent component, ModelEvent type) {
        this.component = component;
        this.type = type;
    }

    @Override
    public void hasChanged() {
        changed = true;
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (changed && event == type) {
            if (!paintPending) {
                paintPending = true;
                SwingUtilities.invokeLater(() -> {
                    paintPending = false;
                    component.paintImmediately();
                });
            }
            changed = false;
        }
    }
}
