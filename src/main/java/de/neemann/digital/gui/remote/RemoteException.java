package de.neemann.digital.gui.remote;

/**
 * Exception thrown by the RemoteInterface
 * Created by hneemann on 20.08.16.
 */
public class RemoteException extends Exception {
    /**
     * Create a new Exception
     *
     * @param message the message
     */
    public RemoteException(String message) {
        super(message);
    }
}
