/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * @param <T> the type of the editor
 */
public interface Editor<T> {

    /**
     * @return the value of the editor
     * @throws EditorParseException Value in editor field is not valid
     */
    T getValue() throws EditorParseException;

    /**
     * Sets the value to the gui
     *
     * @param value the value to set
     */
    void setValue(T value);

    /**
     * Adds the components of the editor to the panel
     *
     * @param panel             the panel to add the components to
     * @param key               the key which is to edit
     * @param elementAttributes the attributes
     * @param dialog            the containing dialog
     */
    void addToPanel(EditorPanel panel, Key<T> key, ElementAttributes elementAttributes, AttributeDialog dialog);

    /**
     * Used to enable/disable the component.
     *
     * @param enabled true enables the component
     */
    void setEnabled(boolean enabled);

    /**
     * Adds an actionListener to the component
     *
     * @param actionListener the actionListener to add
     */
    default void addActionListener(ActionListener actionListener) {
    }

    /**
     * @return true if a major invisible change has been made that is unlikely to be lost.
     */
    default boolean invisibleModification() {
        return false;
    }

    /**
     * Indicates a invalid value in a input field
     */
    class EditorParseException extends Exception {
        protected EditorParseException(Exception cause) {
            super(cause);
        }

        @Override
        public String getMessage() {
            return getCause().getMessage();
        }
    }

}
