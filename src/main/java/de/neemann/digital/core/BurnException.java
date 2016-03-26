package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class BurnException extends RuntimeException {
    public BurnException() {
        super(Lang.get("err_burnError"));
    }
}
