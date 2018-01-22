package de.neemann.digital.gui.components.graphics;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;

/**
 * The dialog used to show the graphics
 *
 * @author hneemann
 */
public class GraphicDialog extends JDialog {
    private final GraphicComponent graphicComponent;

    /**
     * Creates a new instance of the given size
     *
     * @param parent the parent window
     * @param width  width in pixel
     * @param height height in pixel
     */
    public GraphicDialog(Window parent, int width, int height) {
        super(parent, Lang.get("elem_GraphicCard"), ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        graphicComponent = new GraphicComponent(width, height);
        getContentPane().add(graphicComponent);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Updates the graphics data
     *
     * @param memory the raw data to use
     * @param bank   the bank to show
     */
    public void updateGraphic(DataField memory, boolean bank) {
        graphicComponent.updateGraphic(memory.getData(), bank);
    }
}
