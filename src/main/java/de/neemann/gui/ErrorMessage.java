/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import de.neemann.digital.core.ExceptionWithOrigin;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Used to show error messages.
 * Implements runnable so you can apply this class directly to {@link SwingUtilities#invokeLater(Runnable)}
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
        ErrorDialog dialog = new ErrorDialog(parent, Lang.get("error"), message.toString());
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

    /**
     * the error dialog
     */
    public static final class ErrorDialog extends JDialog {
        private static final Icon ICON = IconCreator.create("dialog-error.png");
        private String errorMessage;

        private ErrorDialog(Component parent, String title, String message) {
            super(getParentWindow(parent), title, ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            errorMessage = message;
            message = new LineBreaker(80)
                    .toHTML()
                    .preserveContainedLineBreaks()
                    .breakLines(message);


            JLabel ta = new JLabel(message);
            int border = ta.getFont().getSize();
            ta.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            getContentPane().add(ta);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton button = new JButton(new AbstractAction(Lang.get("ok")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    dispose();
                }
            });
            buttons.add(button);
            getContentPane().add(buttons, BorderLayout.SOUTH);

            JLabel l = new JLabel(ICON);
            l.setVerticalAlignment(JLabel.TOP);
            l.setBorder(BorderFactory.createEmptyBorder(border, border, border, 0));
            getContentPane().add(l, BorderLayout.WEST);

            pack();
            setLocationRelativeTo(parent);
            setAlwaysOnTop(true);
        }

        /**
         * @return the error message
         */
        public String getErrorMessage() {
            return errorMessage;
        }

    }

    /**
     * Get the parent window of the given component.
     * If the component is a window this window is returned
     *
     * @param parent the parent component
     * @return the window instance
     */
    public static Window getParentWindow(Component parent) {
        if (parent == null)
            return null;
        else if (parent instanceof Window)
            return (Window) parent;
        else
            return SwingUtilities.getWindowAncestor(parent);
    }
}
