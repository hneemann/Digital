package de.neemann.digital.builder;

import de.neemann.digital.core.element.PinDescription;

public class Pin {
    private final int num;
    private final PinDescription.Direction direction;

    public Pin(int num, PinDescription.Direction direction) {
        this.num = num;
        this.direction = direction;
    }

    public int getNum() {
        return num;
    }

    public PinDescription.Direction getDirection() {
        return direction;
    }
}
