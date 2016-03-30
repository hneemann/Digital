package de.neemann.digital.gui;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.Observer;

import javax.swing.*;

/**
 * @author hneemann
 */
public class GuiModelObserver implements Observer, ModelStateObserver {
    private final JComponent component;
    private final ModelEvent.Event type;
    private boolean changed = false;

    public GuiModelObserver(JComponent component, ModelEvent.Event type) {
        this.component = component;
        this.type = type;
    }

    @Override
    public void hasChanged() {
        changed = true;
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (changed && event.getType() == type) {
            component.paintImmediately(0, 0, component.getWidth(), component.getHeight());
            changed = false;
        }
    }
}
