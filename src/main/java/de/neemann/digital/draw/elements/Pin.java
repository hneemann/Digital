package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinInfo;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Puts the pins name and the pins x-y-position together!
 * @author hneemann
 */
public class Pin extends PinInfo {

    private final Vector pos;
    private ObservableValue value;
    private ObservableValue readerValue;  // reader for bidirectional pins


    public Pin(Vector pos, PinDescription pin) {
        super(pin);
        this.pos = pos;
    }

    public Vector getPos() {
        return pos;
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

}
