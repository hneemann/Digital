package de.neemann.digital.gui;

import de.neemann.digital.core.element.*;
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
 * The LibrarySelector is responsible for building the menu used to select items for adding them to the circuit.
 * This class also handles the import of nested circuits
 *
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

    /**
     * Creates a new library selector.
     * the elementState is used to seht the window to the elemetEdit mode if a new element is added to the circuit.
     *
     * @param library      the library to select elements from
     * @param shapeFactory The shape factory
     * @param elementState the elements state
     */
    public LibrarySelector(ElementLibrary library, ShapeFactory shapeFactory, State elementState) {
        this.library = library;
        this.shapeFactory = shapeFactory;
        this.elementState = elementState;
        library.setElementNotFoundNotification(this);
        importedElements = new ArrayList<>();
    }

    /**
     * Builds the menu which is added to the menu bar.
     * If an item is selected the state is set to the edit element state and the new element is added
     * to the circuitComponent.
     *
     * @param insertHistory    the insert history is used to add selected parts to the tool bar
     * @param circuitComponent the used circuit component
     * @return the menu to ad to the menu bar
     */
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
                fc.setFileFilter(new FileNameExtensionFilter("Circuit", "dig"));
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    Imported imp = importElement(fc.getSelectedFile());
                    if (imp != null) {
                        VisualElement visualElement = new VisualElement(imp.description.getName()).setPos(new Vector(10, 10)).setShapeFactory(shapeFactory);
                        elementState.activate();
                        circuitComponent.setPartToInsert(visualElement);
                        insertHistory.add(imp.insertAction);
                    }
                }
            }
        }.setToolTip(Lang.get("menu_import_tt")).createJMenuItem());

        customMenu.add(new ToolTipAction(Lang.get("menu_refresh")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ImportedItem item : importedElements) {
                    library.removeElement(item.name);
                    customMenu.remove(item.menuEntry);
                }
            }
        }.setToolTip(Lang.get("menu_refresh_tt")).createJMenuItem());


        JMenu subMenu = null;
        String lastPath = null;
        for (ElementLibrary.ElementContainer elementContainer : library) {
            String path = elementContainer.getTreePath();
            if (!path.equals(lastPath)) {
                subMenu = new JMenu(path);
                parts.add(subMenu);
                lastPath = path;
            }
            subMenu.add(new InsertAction(elementContainer.getDescription(), insertHistory, circuitComponent)
                    .setToolTip(createToolTipText(elementContainer.getDescription().getTranslatedName()))
                    .createJMenuItem());
        }

        return parts;
    }

    private String createToolTipText(String elementName) {
        String toolTipText = Lang.getNull("elem_" + elementName + "_tt");
        if (toolTipText == null)
            return null;

        if (toolTipText.indexOf('\n') >= 0)
            toolTipText = "<html>" + toolTipText.replace("\n", "<br>") + "</html>";
        return toolTipText;
    }

    /**
     * sets the file path which is used to load missing nested elements
     *
     * @param filePath the file path
     */
    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

    @Override
    public ElementTypeDescription elementNotFound(String elementName) {
        Imported imported = importElement(new File(filePath, elementName));
        if (imported == null)
            return null;
        else
            return imported.description;
    }

    private final class InsertAction extends ToolTipAction {

        private final String name;
        private final InsertHistory insertHistory;
        private final CircuitComponent circuitComponent;

        private InsertAction(ElementTypeDescription typeDescription, InsertHistory insertHistory, CircuitComponent circuitComponent) {
            super(typeDescription.getTranslatedName(), new VisualElement(typeDescription.getName()).setShapeFactory(shapeFactory).createIcon(75));
            this.name = typeDescription.getName();
            this.insertHistory = insertHistory;
            this.circuitComponent = circuitComponent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            VisualElement visualElement = new VisualElement(name).setPos(new Vector(10, 10)).setShapeFactory(shapeFactory);
            elementState.activate();
            circuitComponent.setPartToInsert(visualElement);
            insertHistory.add(this);
        }
    }

    private Imported importElement(File file) {
        try {
            System.out.println("load element " + file);
            Circuit circuit = Circuit.loadCircuit(file, shapeFactory);
            ElementTypeDescription description =
                    new ElementTypeDescriptionCustom(file,
                            attributes -> new CustomElement(circuit, library, file.getName()),
                            circuit.getAttributes(), circuit.getInputNames(library))
                            .setShortName(createShortName(file));
            library.addDescription(description);

            InsertAction insertAction = new InsertAction(description, insertHistory, circuitComponent);
            String descriptionText = circuit.getAttributes().get(Keys.Description);
            if (descriptionText != null && descriptionText.length() > 0)
                insertAction.setToolTip(descriptionText);

            JMenuItem menuEntry = insertAction.createJMenuItem();
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
            return new Imported(description, insertAction);
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
        if (name.endsWith(".dig")) return name.substring(0, name.length() - 4);

        String transName = Lang.getNull("elem_" + name);
        if (transName == null)
            return name;
        else
            return transName;
    }

    private final static class ImportedItem {
        private final String name;
        private final JMenuItem menuEntry;

        private ImportedItem(String name, JMenuItem menuEntry) {
            this.name = name;
            this.menuEntry = menuEntry;
        }
    }

    /**
     * The description of a nested element.
     * This is a complete circuit which is used as a element.
     */
    public static class ElementTypeDescriptionCustom extends ElementTypeDescription {
        private final File file;
        private final ElementAttributes attributes;

        /**
         * Creates a new element
         *
         * @param file           the file which is loaded
         * @param elementFactory a element factory which is used to create concrete elements if needed
         * @param attributes     the attributes of the element
         * @param inputNames     the names of the input signals
         */
        public ElementTypeDescriptionCustom(File file, ElementFactory elementFactory, ElementAttributes attributes, PinDescription... inputNames) {
            super(file.getName(), elementFactory, inputNames);
            this.file = file;
            this.attributes = attributes;
            if (attributes.contains(Keys.Description))
                setDescription(attributes.get(Keys.Description));
        }

        /**
         * Returns the filename
         * the retuned file is opened if the user wants to modify the element
         *
         * @return the filename
         */
        public File getFile() {
            return file;
        }

        /**
         * @return the elements attributes
         */
        public ElementAttributes getAttributes() {
            return attributes;
        }
    }

    private static class Imported {
        private final ElementTypeDescription description;
        private final InsertAction insertAction;

        private Imported(ElementTypeDescription description, InsertAction insertAction) {
            this.description = description;
            this.insertAction = insertAction;
        }
    }
}
