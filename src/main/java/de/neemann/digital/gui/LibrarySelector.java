package de.neemann.digital.gui;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.elements.Circuit;
import de.neemann.digital.gui.draw.elements.VisualElement;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.library.CustomElement;
import de.neemann.digital.gui.draw.library.ElementLibrary;
import de.neemann.digital.gui.draw.library.ElementNotFoundNotification;
import de.neemann.digital.lang.Lang;
import de.process.utils.gui.ErrorMessage;
import de.process.utils.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

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
    private ArrayList<String> importedElements;

    public LibrarySelector(ElementLibrary library) {
        this.library = library;
        library.setElementNotFoundNotification(this);
        importedElements = new ArrayList<>();
    }

    public JMenu buildMenu(InsertHistory insertHistory, CircuitComponent circuitComponent) {
        this.insertHistory = insertHistory;
        this.circuitComponent = circuitComponent;
        JMenu parts = new JMenu(Lang.get("menu_elements"));

        customMenu = new JMenu(Lang.get("menu_custom"));
        parts.add(customMenu);

        customMenu.add(new ToolTipAction(Lang.get("menu_import")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = Main.getjFileChooser(lastFile);
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    importElement(fc.getSelectedFile());
                }
            }
        }.setToolTip(Lang.get("menu_import_tt")));

        customMenu.add(new ToolTipAction(Lang.get("menu_refresh")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (String name : importedElements)
                    library.removeElement(name);
            }
        }.setToolTip(Lang.get("menu_refresh_tt")));


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
            System.out.println("load element " + file);
            Circuit circuit = Circuit.loadCircuit(file);
            ElementTypeDescription description =
                    new ElementTypeDescription(file.getName(),
                            attributes -> new CustomElement(circuit, library),
                            circuit.getInputNames(library))
                            .setShortName(createShortName(file));
            library.addDescription(description);
            if (customMenu != null)
                customMenu.add(new InsertAction(description.getName(), insertHistory, circuitComponent));
            importedElements.add(description.getName());
            return description;
        } catch (Exception e) {
            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorImportingModel")).addCause(e));
        }
        return null;
    }

    private String createShortName(File file) {
        String name = file.getName();
        if (name.endsWith(".dig")) name = name.substring(0, name.length() - 4);
        return name;
    }

}
