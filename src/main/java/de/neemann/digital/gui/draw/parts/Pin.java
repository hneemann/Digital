package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Vector;

/**
 * @author hneemann
 */
public class Pin {
    private Vector pos;
    private String name;

    public Pin(Vector pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    public Vector getPos() {
        return pos;
    }
}
