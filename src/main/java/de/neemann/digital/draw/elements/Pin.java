package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Puts the pins name and the pins x-y-position together!
 * @author hneemann
 */
public class Pin {

    private final Vector pos;
    private final String name;
    private final Direction direction;
    private ObservableValue value;
    private ObservableValue readerValue;  // reader for bidirectional pins

    public Pin(Vector pos, Pin pin) {
        this(pos, pin.name, pin.direction);
    }

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

    public ObservableValue getValue() {
        return value;
    }

    public void setValue(ObservableValue value) {
        this.value = value;
    }

    public ObservableValue getReaderValue() {
        return readerValue;
    }

    public void setReaderValue(ObservableValue readerValue) {
        this.readerValue = readerValue;
    }

    public enum Direction {input, output, both}
}
