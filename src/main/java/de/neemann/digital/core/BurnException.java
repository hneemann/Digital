package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class BurnException extends NodeException {
    public BurnException(Node node, ObservableValue... values) {
        super(Lang.get("err_burnError"), node, values);
    }
}
