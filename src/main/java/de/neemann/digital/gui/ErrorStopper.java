package de.neemann.digital.gui;

/**
 * @author hneemann
 */
public interface ErrorStopper {
    void showErrorAndStopModel(String message, Exception cause);
}
