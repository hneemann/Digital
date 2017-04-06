package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.Element;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.graphics.Transform;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.sync.Sync;

import java.awt.*;

/**
 * The VisualParts InteractorInterface instance is called if the element is clicked
 * during execution. So the User can interact with the element during execution.
 * Used at the InputShape to let the user toggle the inputs state.
 *
 * @author hneemann
 * @see InputShape
 */
public interface InteractorInterface {
    /**
     * Called if clicked on running model
     *
     * @param cc      the CircuitComponent
     * @param pos     the popuplocation on screen
     * @param ioState the state of the element
     * @return true if model is changed
     */
    boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync);

    /**
     * Called mouse is pressed on running model
     *
     * @param cc      the CircuitComponent
     * @param pos     the popuplocation on screen
     * @param ioState the state of the element
     * @return true if model is changed
     */
    boolean pressed(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync);

    /**
     * Called mouse is released on running model
     *
     * @param cc      the CircuitComponent
     * @param pos     the popuplocation on screen
     * @param ioState the state of the element
     * @return true if model is changed
     */
    boolean released(CircuitComponent cc, Point pos, IOState ioState, Element element, Sync modelSync);

    /**
     * Called mouse is dragged on running model
     *
     * @param cc        the CircuitComponent
     * @param pos       the position in the model coordinates
     * @param transform transformation to transform shape coordinates to the model coordinates
     * @param ioState   the state of the element
     * @return true if model is changed
     */
    boolean dragged(CircuitComponent cc, Vector pos, Transform transform, IOState ioState, Element element, Sync modelSync);

}
