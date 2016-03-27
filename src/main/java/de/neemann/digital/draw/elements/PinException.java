package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.model.Net;

/**
 * @author hneemann
 */
public class PinException extends Exception {
    private VisualElement element;
    private Net net;

    public PinException(String message, VisualElement element) {
        super(message);
        this.element = element;
    }

    public PinException(String message, Net net) {
        super(message);
        this.net = net;
    }

    public PinException(String message) {
        super(message);
    }

    public VisualElement getVisualElement() {
        return element;
    }

    public Net getNet() {
        return net;
    }
}
