package de.neemann.digital.gui;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementFactory;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.CustomElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundNotification;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.state.State;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class LibrarySelector implements ElementNotFoundNotification {
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final State elementState;
    private File filePath;
    private JMenu customMenu;
    private InsertHistory insertHistory;
    private CircuitComponent circuitComponent;
    private ArrayList<ImportedItem> importedElements;

    public LibrarySelector(ElementLibrary library, ShapeFactory shapeFactory, State elementState) {
        this.library = library;
        this.shapeFactory = shapeFactory;
        this.elementState = elementState;
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
                JFileChooser fc = new JFileChooser(filePath);
                fc.addChoosableFileFilter(new FileNameExtensionFilter("Circuit", "dig"));
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    ElementTypeDescription des = importElement(fc.getSelectedFile());
                    if (des != null) {
                        VisualElement visualElement = new VisualElement(des.getName()).setPos(new Vector(10, 10)).setShapeFactory(shapeFactory);
                        circuitComponent.setPartToDrag(visualElement);
                    }
                }
            }
        }.setToolTip(Lang.get("menu_import_tt")));

        customMenu.add(new ToolTipAction(Lang.get("menu_refresh")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ImportedItem item : importedElements) {
                    library.removeElement(item.name);
                    customMenu.remove(item.menuEntry);
                }
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
            subMenu.add(new InsertAction(pc.getName(), insertHistory, circuitComponent).createJMenuItem());
        }

        return parts;
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
            super(createShortName(name), new VisualElement(name).setShapeFactory(shapeFactory).createIcon(75));
            this.name = name;
            this.insertHistory = insertHistory;
            this.circuitComponent = circuitComponent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            VisualElement visualElement = new VisualElement(name).setPos(new Vector(10, 10)).setShapeFactory(shapeFactory);
            elementState.activate();
            circuitComponent.setPartToDrag(visualElement);
            insertHistory.add(this);
        }
    }

    private ElementTypeDescription importElement(File file) {
        try {
            System.out.println("load element " + file);
            Circuit circuit = Circuit.loadCircuit(file, shapeFactory);
            ElementTypeDescription description =
                    new ElementTypeDescriptionCustom(file,
                            attributes -> new CustomElement(circuit, library, file.getName()),
                            circuit.getAttributes(), circuit.getInputNames(library))
                            .setShortName(createShortName(file));
            library.addDescription(description);

            JMenuItem menuEntry = new InsertAction(description.getName(), insertHistory, circuitComponent).createJMenuItem();
            ImportedItem item = findImportedItem(description.getName());
            if (item != null) {
                if (customMenu != null) {
                    customMenu.remove(item.menuEntry);
                }
                importedElements.remove(item);
            }
            importedElements.add(new ImportedItem(description.getName(), menuEntry));
            if (customMenu != null)
                customMenu.add(menuEntry);
            return description;
        } catch (Exception e) {
            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorImportingModel")).addCause(e));
        }
        return null;
    }

    private ImportedItem findImportedItem(String name) {
        for (ImportedItem i : importedElements) {
            if (i.name.equals(name))
                return i;
        }
        return null;
    }

    private String createShortName(File file) {
        return createShortName(file.getName());
    }

    private String createShortName(String name) {
        if (name.endsWith(".dig")) name = name.substring(0, name.length() - 4);
        return name;
    }

    private static class ImportedItem {
        private final String name;
        private final JMenuItem menuEntry;

        public ImportedItem(String name, JMenuItem menuEntry) {
            this.name = name;
            this.menuEntry = menuEntry;
        }
    }

    public static class ElementTypeDescriptionCustom extends ElementTypeDescription {
        private final File file;
        private final ElementAttributes attributes;

        public ElementTypeDescriptionCustom(File file, ElementFactory elementFactory, ElementAttributes attributes, String... inputNames) {
            super(file.getName(), elementFactory, inputNames);
            this.file = file;
            this.attributes = attributes;
        }

        public File getFile() {
            return file;
        }

        public ElementAttributes getAttributes() {
            return attributes;
        }
    }
}
