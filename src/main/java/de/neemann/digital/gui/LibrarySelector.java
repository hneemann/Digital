package de.neemann.digital.gui;

import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.library.PartLibrary;
import de.neemann.digital.gui.draw.parts.VisualPart;
import de.process.utils.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author hneemann
 */
public class LibrarySelector {
    private final PartLibrary library;

    public LibrarySelector(PartLibrary library) {
        this.library = library;
    }

    public JMenu buildMenu(InsertHistory insertHistory, CircuitComponent circuitComponent) {
        JMenu parts = new JMenu("Parts");

        JMenu subMenu = null;
        String lastPath = null;
        for (PartLibrary.PartContainer pc : library) {
            String path = pc.getTreePath();
            if (!path.equals(lastPath)) {
                subMenu = new JMenu(path);
                parts.add(subMenu);
                lastPath = path;
            }
            subMenu.add(new InsertAction(pc.getName(), insertHistory, circuitComponent));
        }

        return parts;
    }

    private class InsertAction extends ToolTipAction {

        private final String name;
        private final InsertHistory insertHistory;
        private final CircuitComponent circuitComponent;

        public InsertAction(String name, InsertHistory insertHistory, CircuitComponent circuitComponent) {
            super(name, new VisualPart(name).createIcon(60));
            this.name = name;
            this.insertHistory = insertHistory;
            this.circuitComponent = circuitComponent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            VisualPart visualPart = new VisualPart(name).setPos(new Vector(10, 10));
            circuitComponent.setPartToDrag(visualPart);
            insertHistory.add(this);
        }
    }

}
