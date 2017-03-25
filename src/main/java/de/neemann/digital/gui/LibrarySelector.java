package de.neemann.digital.gui;

import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryListener;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.state.State;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.StringUtils;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * The LibrarySelector is responsible for building the menu used to select items for adding them to the circuit.
 * This class also handles the import of nested circuits
 *
 * @author hneemann
 */
public class LibrarySelector implements LibraryListener {
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final State elementState;
    private JMenu componentsMenu;
    private InsertHistory insertHistory;
    private CircuitComponent circuitComponent;

    /**
     * Creates a new library selector.
     * the elementState is used to set the window to the elementEdit mode if a new element is added to the circuit.
     *
     * @param library      the library to select elements from
     * @param shapeFactory The shape factory
     * @param elementState the elements state
     */
    public LibrarySelector(ElementLibrary library, ShapeFactory shapeFactory, State elementState) {
        this.library = library;
        library.addListener(this);
        this.shapeFactory = shapeFactory;
        this.elementState = elementState;
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
        componentsMenu = new JMenu(Lang.get("menu_elements"));
        libraryChanged();

        return componentsMenu;
    }

    @Override
    public void libraryChanged() {
        componentsMenu.removeAll();

        for (LibraryNode n : library.getRoot())
            addComponents(componentsMenu, n);

        insertHistory.removeCustom();

        JMenuItem m = componentsMenu.getItem(componentsMenu.getItemCount() - 1);
        if (m instanceof JMenu) {
            ((JMenu) m).add(new ToolTipAction(Lang.get("menu_import")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        library.updateEntries();
                    } catch (IOException ex) {
                        SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorImportingModel")).addCause(ex));
                    }
                }
            }.setToolTip(Lang.get("menu_import_tt")).createJMenuItem());
        }

    }

    private void addComponents(JMenu parts, LibraryNode node) {
        if (node.isLeaf()) {
            parts.add(new InsertAction(node, insertHistory, circuitComponent)
                    .setToolTip(createToolTipText(node.getName()))
                    .createJMenuItem());
        } else {
            JMenu subMenu = new JMenu(node.getName());
            for (LibraryNode child : node)
                addComponents(subMenu, child);
            parts.add(subMenu);
        }
    }

    private static String createToolTipText(String elementName) {
        return StringUtils.textToHTML(Lang.getNull("elem_" + elementName + "_tt"));
    }

    final class InsertAction extends ToolTipAction {
        private final LibraryNode node;
        private final InsertHistory insertHistory;
        private final CircuitComponent circuitComponent;

        private InsertAction(LibraryNode node, InsertHistory insertHistory, CircuitComponent circuitComponent) {
            super(node.getTranslatedName(), createIcon(node, shapeFactory));
            this.node = node;
            this.insertHistory = insertHistory;
            this.circuitComponent = circuitComponent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            VisualElement visualElement = new VisualElement(node.getName()).setPos(new Vector(10, 10)).setShapeFactory(shapeFactory);
            elementState.enter();
            circuitComponent.setPartToInsert(visualElement);
            if (getIcon() == null) {
                try {
                    node.getDescription();
                    setIcon(createIcon(node, shapeFactory));
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorImportingModel")).addCause(ex));
                }
            }
            insertHistory.add(this);
        }

        public boolean isCustom() {
            return node.getDescriptionOrNull() instanceof ElementLibrary.ElementTypeDescriptionCustom;
        }
    }

    private static ImageIcon createIcon(LibraryNode node, ShapeFactory shapeFactory) {
        // don't load the description if only the icon is needed
        // create action without an icon instead
        if (node.isDescriptionLoaded()) {
            try {
                return new VisualElement(node.getDescription().getName()).setShapeFactory(shapeFactory).createIcon(75);
            } catch (IOException ex) {
                SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorImportingModel")).addCause(ex));
            }
        }
        return null;
    }

}
