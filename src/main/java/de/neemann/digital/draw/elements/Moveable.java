package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.Vector;

/**
 * A simple moveable instance.
 *
 * @author hneemann
 */
public interface Moveable {

    /**
     * Moves the instance by the given delta
     *
     * @param delta the movement
     */
    void move(Vector delta);
}
