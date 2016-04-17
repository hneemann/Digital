package de.neemann.digital.gui;

/**
 * Interface used to stop the model.
 *
 * @author hneemann
 */
public interface ErrorStopper {
    /**
     * Called to stop the mode
     *
     * @param message the message
     * @param cause   the cause
     */
    void showErrorAndStopModel(String message, Exception cause);
}
