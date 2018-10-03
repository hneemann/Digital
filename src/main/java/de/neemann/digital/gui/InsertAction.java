/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.MissingShape;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Action to insert the given node to the given circuit
 */
public final class InsertAction extends ToolTipAction {
    private final InsertHistory insertHistory;
    private final CircuitComponent circuitComponent;
    private final ShapeFactory shapeFactory;
    private LibraryNode node;

    /**
     * Creates a new instance
     *
     * @param node             the node which holds the element to add
     * @param insertHistory    the history to add the element to
     * @param circuitComponent the component to add the element to
     * @param shapeFactory     the shapeFactory to create the icon
     */
    public InsertAction(LibraryNode node, InsertHistory insertHistory, CircuitComponent circuitComponent, ShapeFactory shapeFactory) {
        super(node.getTranslatedName(), node.getIconOrNull(shapeFactory));
        this.shapeFactory = shapeFactory;
        this.node = node;
        this.insertHistory = insertHistory;
        this.circuitComponent = circuitComponent;
        setEnabled(node.isUnique());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (node.isUnique()) {
            VisualElement visualElement = node.setWideShapeFlagTo(new VisualElement(node.getName()).setPos(new Vector(10, 10)).setShapeFactory(shapeFactory));
            if (getIcon() == null) {
                try {
                    node.getDescription();
                    setIcon(node.getIcon(shapeFactory));
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorImportingModel_N0", node.getName())).addCause(ex));
                }
            }

            if (visualElement.getShape() instanceof MissingShape)
                return;

            circuitComponent.setPartToInsert(visualElement);
            insertHistory.add(this);
        }
    }

    /**
     * @return true if element to insert is a custom element
     */
    public boolean isCustom() {
        return node.isCustom();
    }

    /**
     * @return the name of the node to insert
     */
    public String getName() {
        return node.getName();
    }

    /**
     * Updates this action to a new node
     *
     * @param node the node
     */
    public void update(LibraryNode node) {
        this.node = node;
        try {
            final Icon icon = node.getIcon(shapeFactory);
            setIcon(icon);
        } catch (IOException ex) {
            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorImportingModel_N0", node.getName())).addCause(ex));
        }
    }

    /**
     * @return the library node
     */
    public LibraryNode getNode() {
        return node;
    }


    /**
     * Implements a lazy loading of the tooltips.
     * Avoids the reading of all tooltips from the lib files if menu is created.
     * This code ensures that the tooltips are only loaded from the file if the text is shown to the user.
     *
     * @return the JMenuItem created
     */
    @Override
    public JMenuItem createJMenuItem() {
        JMenuItem i = new JMenuItem(node.getTranslatedName(), getIcon()) {
            @Override
            public String getToolTipText() {
                return node.getToolTipText();
            }
        };
        i.addActionListener(InsertAction.this);
        i.setEnabled(node.isUnique());
        ToolTipManager.sharedInstance().registerComponent(i);
        return i;
    }
}
