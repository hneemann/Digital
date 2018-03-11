/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.Vector;

/**
 * A simple moveable instance.
 */
public interface Movable {

    /**
     * Moves the instance by the given delta
     *
     * @param delta the movement
     */
    void move(Vector delta);

    /**
     * @return the actual position
     */
    Vector getPos();
}
