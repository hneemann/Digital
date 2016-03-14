package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Vector;

/**
 * @author hneemann
 */
public class Pin {

    private final Vector pos;
    private final String name;
    private final Direction direction;

    public Pin(Vector pos, String name, Direction direction) {
        this.pos = pos;
        this.name = name;
        this.direction = direction;
    }

    public Vector getPos() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {input, output, both}
}
