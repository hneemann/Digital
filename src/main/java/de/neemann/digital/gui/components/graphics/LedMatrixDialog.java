package de.neemann.digital.gui.components.graphics;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;

/**
 * The LED matrix dialog
 * Created by hneemann on 08.04.17.
 */
public class LedMatrixDialog extends JDialog {

    private final LedMatrixComponent ledMatrixComponent;

    /**
     * Create a new instance
     *
     * @param dy         height of matrix
     * @param data       data
     * @param color      the LEDs color
     * @param ledPersist if true the LEDs light up indefinite
     */
    public LedMatrixDialog(int dy, long[] data, Color color, boolean ledPersist) {
        super((JFrame) null, Lang.get("elem_LedMatrix"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        ledMatrixComponent = new LedMatrixComponent(dy, data, color, ledPersist);
        getContentPane().add(ledMatrixComponent);
        pack();

        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setVisible(true);
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
