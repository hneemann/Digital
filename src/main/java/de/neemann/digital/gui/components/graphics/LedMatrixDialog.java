/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.graphics;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;

/**
 * The LED matrix dialog
 */
public class LedMatrixDialog extends JDialog {

    private final LedMatrixComponent ledMatrixComponent;

    /**
     * Create a new instance
     *
     * @param parent     the parent frame
     * @param dy         height of matrix
     * @param data       data
     * @param color      the LEDs color
     * @param ledPersist if true the LEDs light up indefinite
     */
    public LedMatrixDialog(JFrame parent, int dy, long[] data, Color color, boolean ledPersist) {
        super(parent, Lang.get("elem_LedMatrix"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        ledMatrixComponent = new LedMatrixComponent(dy, data, color, ledPersist);
        getContentPane().add(ledMatrixComponent);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);

        addWindowFocusListener(new MoveFocusTo(parent));
    }

    /**
     * Update the graphic
     *
     * @param colAddr col update
     * @param rowData updated data
     */
    public void updateGraphic(int colAddr, long rowData) {
        ledMatrixComponent.updateGraphic(colAddr, rowData);
    }
}
