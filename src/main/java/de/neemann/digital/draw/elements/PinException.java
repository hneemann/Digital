package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ExceptionWithOrigin;
import de.neemann.digital.draw.model.Net;

/**
 * Exception thrown dealing with pins
 *
 * @author hneemann
 */
public class PinException extends ExceptionWithOrigin {
    private VisualElement element;
    private Net net;

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param element the visual element affected
     */
    public PinException(String message, VisualElement element) {
        super(message);
        this.element = element;
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param net     the net affected
     */
    public PinException(String message, Net net) {
        super(message);
        this.net = net;
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public PinException(String message) {
        super(message);
    }

    /**
     * @return the effected element
     */
    public VisualElement getVisualElement() {
        return element;
    }

    /**
     * @return the effected net
     */
    public Net getNet() {
        return net;
    }
}
