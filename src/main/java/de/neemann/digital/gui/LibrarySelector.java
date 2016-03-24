package de.neemann.digital.gui;

import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementFactory;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.elements.Circuit;
import de.neemann.digital.gui.draw.elements.VisualElement;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.library.CustomElement;
import de.neemann.digital.gui.draw.library.ElementLibrary;
import de.neemann.digital.gui.draw.library.ElementNotFoundNotification;
import de.process.utils.gui.ErrorMessage;
import de.process.utils.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author hneemann
 */
public class LibrarySelector implements ElementNotFoundNotification {
    private final ElementLibrary library;
    private File lastFile;
    private File filePath;
    private JMenu customMenu;
    private InsertHistory insertHistory;
    private CircuitComponent circuitComponent;

    public LibrarySelector(ElementLibrary library) {
        this.library = library;
        library.setElementNotFoundNotification(this);
    }

    public JMenu buildMenu(InsertHistory insertHistory, CircuitComponent circuitComponent) {
        this.insertHistory = insertHistory;
        this.circuitComponent = circuitComponent;
        JMenu parts = new JMenu("Elements");

        customMenu = new JMenu("Custom");
        parts.add(customMenu);

        ToolTipAction importAction = new ToolTipAction("Import") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = Main.getjFileChooser(lastFile);
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    importElement(fc.getSelectedFile());
                }
            }
        }.setToolTip("Imports a model as a useable Element!");
        customMenu.add(importAction.createJMenuItem());


        JMenu subMenu = null;
        String lastPath = null;
        for (ElementLibrary.ElementContainer pc : library) {
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

    public void setLastFile(File lastFile) {
        this.lastFile = lastFile;
    }

    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

    @Override
    public ElementTypeDescription notFound(String elementName) {
        return importElement(new File(filePath, elementName));
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

    private ElementTypeDescription importElement(File file) {
        try {
            Circuit circuit = Circuit.loadCircuit(file);
            ElementTypeDescription description = new ElementTypeDescription(file.getName(), new ElementFactory() {
                @Override
                public Element create(ElementAttributes attributes) {
                    return new CustomElement(circuit, library);
                }
            }, circuit.getInputNames(library));
            library.addDescription(description);
            customMenu.add(new InsertAction(description.getName(), insertHistory, circuitComponent));
            return description;
        } catch (Exception e) {
            new ErrorMessage("error importing model").addCause(e).show();
        }
        return null;
    }

}
