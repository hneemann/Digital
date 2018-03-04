/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import javax.swing.*;
import java.awt.*;

/**
 * A simple confirmation dialog builder
 */
public class ConfirmDialogBuilder {

    private final String message;
    private String title = "Question";
    private String yes = "Yes";
    private String no = "No";
    private int def = 0;
    private String cancle;

    /**
     * Creates anew dialog builder
     *
     * @param message the dialog message
     */
    public ConfirmDialogBuilder(String message) {
        this.message = message;
    }

    /**
     * Sets the dialogs title
     *
     * @param title the dialogs title
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the text for the yes-option
     *
     * @param yes the yes option text
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setYesOption(String yes) {
        this.yes = yes;
        return this;
    }

    /**
     * Sets the text for the no-option
     *
     * @param no the no option text
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setNoOption(String no) {
        this.no = no;
        return this;
    }

    /**
     * Sets the text for the cancle-option
     *
     * @param cancle the cancle option text
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setCancleOption(String cancle) {
        this.cancle = cancle;
        return this;
    }

    /**
     * Sets yes as the default option
     *
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setYesAsDefault() {
        def = 0;
        return this;
    }

    /**
     * Sets no as the default option
     *
     * @return this for chaining calls
     */
    public ConfirmDialogBuilder setNoAsDefault() {
        def = 1;
        return this;
    }

    /**
     * Shows the dialog
     *
     * @param parent the parent component
     * @return the result of showOptionDialog
     */
    public int show(Component parent) {
        Object[] options;
        int optionType;
        if (cancle == null) {
            options = new Object[]{yes, no};
            optionType = JOptionPane.YES_NO_OPTION;
        } else {
            options = new Object[]{yes, no, cancle};
            optionType = JOptionPane.YES_NO_CANCEL_OPTION;
        }

        return JOptionPane.showOptionDialog(parent,
                message,
                title,
                optionType,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[def]);
    }

}
