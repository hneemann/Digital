/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ExceptionWithOrigin;
import de.neemann.digital.draw.model.Net;

/**
 * Exception thrown dealing with pins
 */
public class PinException extends ExceptionWithOrigin {
    private Net net;

    /**
     * Creates a new instance
     *
     * @param message       the message
     * @param visualElement the visual element affected
     */
    public PinException(String message, VisualElement visualElement) {
        super(message);
        setVisualElement(visualElement);
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
        setOrigin(net.getOrigin());
        setVisualElement(net.getVisualElement());
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
     * @return the effected net
     */
    public Net getNet() {
        return net;
    }
}
