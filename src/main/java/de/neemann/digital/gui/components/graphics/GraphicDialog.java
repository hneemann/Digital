package de.neemann.digital.gui.components.graphics;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

import javax.swing.*;

/**
 * @author hneemann
 */
public class GraphicDialog extends JDialog {
    private final GraphicComponent graphicComponent;

    public GraphicDialog(int width, int height) {
        super((JFrame) null, Lang.get("elem_GraphicCard"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        graphicComponent = new GraphicComponent(width, height);
        getContentPane().add(graphicComponent);
        pack();

        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateGraphic(DataField memory, boolean bank) {
        graphicComponent.updateGraphic(memory.getData(), bank);
    }
}
