package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;

import javax.swing.*;

/**
 * @param <T> the type of the editor
 * @author hneemann
 */
public interface Editor<T> {

    /**
     * @return the value of the editor
     * @throws EditorParseException Value in editor field is not valid
     */
    T getValue() throws EditorParseException;

    /**
     * Adds the components of the editor to the panel
     *
     * @param panel             the panel to add the components to
     * @param key               the key which is to edit
     * @param elementAttributes the attributes
     * @param dialog            the containing dialog
     * @param constraints       the constraints used to place the components in the panel
     */
    void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes, AttributeDialog dialog, ConstraintsBuilder constraints);

    /**
     * Used to enable/disable the component.
     *
     * @param enabled true enables the component
     */
    void setEnabled(boolean enabled);

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
