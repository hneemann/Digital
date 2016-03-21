package de.neemann.digital.gui;

import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.elements.VisualElement;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.library.PartLibrary;
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
        JMenu parts = new JMenu("Elements");

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
            super(name, new VisualElement(name).createIcon(60));
            this.name = name;
            this.insertHistory = insertHistory;
            this.circuitComponent = circuitComponent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            VisualElement visualElement = new VisualElement(name).setPos(new Vector(10, 10));
            circuitComponent.setPartToDrag(visualElement);
            insertHistory.add(this);
        }
    }

}
