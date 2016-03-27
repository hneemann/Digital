package de.neemann.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by hneemann on 09.02.14.
 */
public class ErrorMessage implements Runnable {

    private final StringBuilder message;
    private Component component;

    public ErrorMessage() {
        this("");
    }

    public ErrorMessage(String message) {
        this.message = new StringBuilder(message);
    }

    public ErrorMessage addCause(Throwable e) {
        e.printStackTrace();

        if (message.length() > 0)
            message.append('\n');
        message.append(StringUtils.getExceptionMessage(e));
        return this;
    }

    public ErrorMessage show() {
        return show(null);
    }

    public ErrorMessage show(Component parent) {
        JOptionPane.showMessageDialog(parent, message.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        return this;
    }

    public ErrorMessage setComponent(Component component) {
        this.component = component;
        return this;
    }

    @Override
    public void run() {
        show(component);
    }
}
