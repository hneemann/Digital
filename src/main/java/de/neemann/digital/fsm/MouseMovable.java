/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.draw.graphics.VectorInterface;

/**
 * Element which can be moved by the mouse
 */
public interface MouseMovable {
    /**
     * @return the position
     */
    VectorInterface getPos();

    /**
     * Sets the position by the mouse.
     * Is called while dragging.
     *
     * @param pos the intermediate position
     */
    void setPosDragging(VectorFloat pos);

    /**
     * Sets the position by the mouse.
     * Is called if mouse button is released.
     *
     * @param pos the final position
     */
    void setPos(VectorFloat pos);
}
