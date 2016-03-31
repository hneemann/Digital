package de.neemann.digital.gui;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.components.CircuitComponent;

/**
 * @author hneemann
 */
public class GuiModelObserver implements Observer, ModelStateObserver {
    private final CircuitComponent component;
    private final ModelEvent.Event type;
    private boolean changed = false;

    public GuiModelObserver(CircuitComponent component, ModelEvent.Event type) {
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
