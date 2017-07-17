package de.neemann.gui;

import de.neemann.digital.core.ExceptionWithOrigin;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;

/**
 * Used to show error messages.
 * Implements runnable so you can apply this class directly to {@link SwingUtilities#invokeLater(Runnable)}
 *
 * @author hneemann on 09.02.14.
 */
public class ErrorMessage implements Runnable {

    private final StringBuilder message;
    private Component component;

    /**
     * Creates a new instance
     */
    public ErrorMessage() {
        this("");
    }

    /**
     * Creates anew instance
     *
     * @param message the message to show
     */
    public ErrorMessage(String message) {
        this.message = new StringBuilder(message);
    }

    /**
     * Adds a cause to the message
     *
     * @param e the cause
     * @return this for call chaining
     */
    public ErrorMessage addCause(Throwable e) {
        e.printStackTrace();

        if (message.length() > 0)
            message.append('\n');
        addExceptionMessage(e);

        String orig = ExceptionWithOrigin.getOriginOf(e);
        if (orig != null) {
            if (message.length() > 0) message.append('\n');
            message.append(Lang.get("msg_errInFile_N", orig));
        }

        return this;
    }

    /**
     * Creates a exception message
     *
     * @param e the {@link Throwable} instance
     */
    private void addExceptionMessage(Throwable e) {
        while (e != null) {
            final String m = e.getMessage();
            if (m != null && m.length() > 0)
                message.append(m);
            else
                message.append(e.getClass().getSimpleName());
            e = e.getCause();
            if (e != null)
                message.append("\n").append(Lang.get("msg_errCausedBy")).append(": ");
        }
    }

    /**
     * Shows the error message
     */
    public void show() {
        show(null);
    }

    /**
     * Shows the error message
     *
     * @param parent the parent
     */
    public void show(Component parent) {
        JOptionPane optionPane = new JOptionPane(
                new LineBreaker(120)
                        .toHTML()
                        .preserveContainedLineBreaks()
                        .breakLines(message.toString()),
                JOptionPane.ERROR_MESSAGE);
        JDialog dialog = optionPane.createDialog(parent, Lang.get("error"));
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Sets a parents component.
     * Used if supplied to {@link SwingUtilities#invokeLater(Runnable)}
     *
     * @param component the parent
     * @return this for call chaining
     */
    public ErrorMessage setComponent(Component component) {
        this.component = component;
        return this;
    }

    @Override
    public void run() {
        show(component);
    }
}
