package de.neemann.digital.draw.shapes;

import de.neemann.digital.draw.elements.Pin;

/**
 * @author hneemann
 */
public class OutputPinInfo {
    private final String name;
    private final Pin.Direction direction;

    public OutputPinInfo(String name, boolean bidirectional) {
        this.name = name;
        this.direction = bidirectional ? Pin.Direction.both : Pin.Direction.output;
    }

    public String getName() {
        return name;
    }

    public Pin.Direction getDirection() {
        return direction;
    }
}
