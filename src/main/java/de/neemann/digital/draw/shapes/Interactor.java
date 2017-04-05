package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.Element;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.graphics.Transform;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.sync.Sync;

import java.awt.*;

/**
 * The VisualParts Interactor instance is called if the element is clicked
 * during execution. So the User can interact with the element during execution.
 * Used at the InputShape to let the user toggle the inputs state.
 *
 * @author hneemann
 * @see InputShape
 */
public abstract class Interactor implements InteractorInterface {

    @Override
    public boolean pressed(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
        return false;
    }

    @Override
    public boolean released(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync) {
        return false;
    }

    @Override
    public boolean dragged(CircuitComponent cc, Vector pos, Transform transform, IOState ioState, Element element, Sync modelSync) {
        return false;
    }
}
