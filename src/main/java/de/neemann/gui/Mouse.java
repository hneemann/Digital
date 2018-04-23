/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.Settings;

import java.awt.event.MouseEvent;

/**
 * Used to interpret mouse clicks
 */
public interface Mouse {

    /**
     * Returns a mouse event interpreter
     *
     * @return a Mouse interface implementation
     */
    static Mouse getMouse() {
        if (Settings.getInstance().get(Keys.SETTINGS_MAC_MOUSE))
            return new Mouse() {
                @Override
                public boolean isPrimaryClick(MouseEvent e) {
                    return e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown();
                }

                @Override
                public boolean isSecondaryClick(MouseEvent e) {
                    return e.getButton() == MouseEvent.BUTTON1 && e.isControlDown();
                }

                @Override
                public boolean isClickModifier(MouseEvent e) {
                    return e.isShiftDown();
                }
            };
        else
            return new Mouse() {
                @Override
                public boolean isPrimaryClick(MouseEvent e) {
                    return e.getButton() == MouseEvent.BUTTON1;
                }

                @Override
                public boolean isSecondaryClick(MouseEvent e) {
                    return e.getButton() == MouseEvent.BUTTON3;
                }

                @Override
                public boolean isClickModifier(MouseEvent e) {
                    return e.isControlDown();
                }
            };
    }


    /**
     * Returns true is MouseEvent e is a primary click
     *
     * @param e the mouse event
     * @return true if MouseEvent e is a primary click
     */
    boolean isPrimaryClick(MouseEvent e);

    /**
     * Returns true is MouseEvent e is a secondary click
     *
     * @param e the mouse event
     * @return true if MouseEvent e is a secondary click
     */
    boolean isSecondaryClick(MouseEvent e);

    /**
     * Returns true if modifier is pressed
     *
     * @param e the mouse event
     * @return true if modifier is pressed
     */
    boolean isClickModifier(MouseEvent e);

}
