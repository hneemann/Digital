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
 * The VisualParts Interactor instance is called if the element is clicked
 * during execution. So the user can interact with the element during execution.
 * Used at the InputShape to let the user toggle the inputs state.
 * @see InputShape
 */
public abstract class Interactor implements InteractorInterface {

    @Override
    public void pressed(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
    }

    @Override
    public void released(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
    }

    @Override
    public void dragged(CircuitComponent cc, Point posOnScreen, Vector pos, Transform transform, IOState ioState, Element element, SyncAccess modelSync) {
    }
}
