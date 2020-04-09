/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.graphics.Transform;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.*;

/**
 * The {@link de.neemann.digital.draw.elements.VisualElement}s InteractorInterface instance is called
 * if the element is clicked during execution. So the User can interact with the element.
 * Example usage at the {@link InputShape} to let the user toggle the inputs state.
 *
 * @see InputShape
 */
public interface InteractorInterface {
    /**
     * Called if clicked on running model
     *
     * @param cc        the CircuitComponent
     * @param pos       the popuplocation on screen
     * @param ioState   the state of the element
     * @param element   the element which is clicked
     * @param modelSync used to sync model access
     */
    void clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync);

    /**
     * Called if mouse is pressed on running model
     *
     * @param cc        the CircuitComponent
     * @param pos       the popuplocation on screen
     * @param ioState   the state of the element
     * @param element   the element on which the mouse is pressed
     * @param modelSync used to sync model access
     */
    void pressed(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync);

    /**
     * Called if mouse is released on running model
     *
     * @param cc        the CircuitComponent
     * @param pos       the popuplocation on screen
     * @param ioState   the state of the element
     * @param element   the element on which the mouse is released
     * @param modelSync used to sync model access
     */
    void released(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync);

    /**
     * Called if mouse is dragged on running model
     *
     * @param cc          the CircuitComponent
     * @param posOnScreen the position in screen coordinates
     * @param pos         the position in the model coordinates
     * @param transform   transformation to transform shape coordinates to the model coordinates
     * @param ioState     the state of the element
     * @param element     the element on which the mouse is dragged
     * @param modelSync   used to sync model access
     */
    void dragged(CircuitComponent cc, Point posOnScreen, Vector pos, Transform transform, IOState ioState, Element element, SyncAccess modelSync);

}
