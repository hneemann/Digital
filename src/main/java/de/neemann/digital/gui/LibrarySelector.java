/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryListener;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * The LibrarySelector is responsible for building the menu used to select items for adding them to the circuit.
 */
public class LibrarySelector implements LibraryListener {
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private JMenu componentsMenu;
    private InsertHistory insertHistory;
    private CircuitComponent circuitComponent;

    /**
     * Creates a new library selector.
     * the elementState is used to set the window to the elementEdit mode if a new element is added to the circuit.
     *
     * @param library      the library to select elements from
     * @param shapeFactory The shape factory
     */
    public LibrarySelector(ElementLibrary library, ShapeFactory shapeFactory) {
        this.library = library;
        this.shapeFactory = shapeFactory;
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
        libraryChanged(null);

        return componentsMenu;
    }

    @Override
    public void libraryChanged(LibraryNode node) {
        componentsMenu.removeAll();

        for (LibraryNode n : library.getRoot())
            addComponents(componentsMenu, n);

        if (library.getCustomNode() != null) {
            JMenuItem m = componentsMenu.getItem(componentsMenu.getItemCount() - 1);
            if (m instanceof JMenu) {
                JMenu menu = (JMenu) m;
                menu.addSeparator();
                menu.add(new ToolTipAction(Lang.get("menu_update")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            library.updateEntries();
                        } catch (IOException ex) {
                            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorUpdatingLibrary")).addCause(ex));
                        }
                    }
                }.setToolTip(Lang.get("menu_update_tt")).createJMenuItem());
            }
        }
    }

    private void addComponents(JMenu parts, LibraryNode node) {
        if (node.isLeaf()) {
            if (!node.isHidden())
                parts.add(new InsertAction(node, insertHistory, circuitComponent, shapeFactory).createJMenuItem());
        } else {
            JMenu subMenu = new JMenu(node.getName());
            for (LibraryNode child : node)
                addComponents(subMenu, child);
            parts.add(subMenu);
        }
    }
}
