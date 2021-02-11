/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.tree;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.InsertAction;
import de.neemann.digital.gui.InsertHistory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Tree to select items
 */
public class SelectTree extends JTree {

    private final ShapeFactory shapeFactory;
    private Enumeration<TreePath> storedExpanded;

    /**
     * Create a new instance
     *
     * @param model         the model to use
     * @param component     the component to insert the components to
     * @param shapeFactory  the shape factory
     * @param insertHistory the insert history
     */
    public SelectTree(LibraryTreeModel model, CircuitComponent component, ShapeFactory shapeFactory, InsertHistory insertHistory) {
        super(model);
        this.shapeFactory = shapeFactory;
        setSelectionModel(null);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                TreePath path = getClosestPathForLocation(mouseEvent.getX(), mouseEvent.getY());
                if (path != null && path.getPathCount() > 0) {
                    LibraryNode node = (LibraryNode) path.getLastPathComponent();
                    if (node.isLeaf() && node.isUnique()) {
                        try {
                            ElementTypeDescription d = node.getDescription();
                            final VisualElement element = node.setWideShapeFlagTo(new VisualElement(d.getName()).setShapeFactory(shapeFactory));
                            component.setPartToInsert(element);
                            insertHistory.add(new InsertAction(node, insertHistory, component, shapeFactory));
                        } catch (IOException e) {
                            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorImportingModel_N0", node.getName())).addCause(e));
                        }
                    }
                }
            }
        });
        setCellRenderer(new MyCellRenderer());
        setToolTipText("");

        // open first child
        expandPath(new TreePath(model.getFirstLeafParent().getPath()));
    }

    /**
     * Sets a new model to this SelectTree.
     *
     * @param newModel the new model
     */
    public void setModel(LibraryTreeModel newModel) {
        LibraryTreeModel oldModel = (LibraryTreeModel) getModel();
        if (!oldModel.isFiltered() && newModel.isFiltered())
            storedExpanded = getExpandedDescendants(new TreePath(getModel().getRoot()));

        oldModel.close();

        boolean restore = oldModel.isFiltered() && !newModel.isFiltered();
        super.setModel(newModel);
        if (restore && storedExpanded != null) {
            while (storedExpanded.hasMoreElements())
                expandPath(storedExpanded.nextElement());
            storedExpanded = null;
        } else
            expandPath(new TreePath(newModel.getFirstLeafParent().getPath()));
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        TreePath selPath = getPathForLocation(e.getX(), e.getY());
        if (selPath != null && selPath.getPathCount() > 0) {
            Object lp = selPath.getLastPathComponent();
            if (lp instanceof LibraryNode) {
                return ((LibraryNode) lp).getToolTipText();
            }
        }
        return null;
    }

    private class MyCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            JLabel comp = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (leaf)
                comp.setIcon(((LibraryNode) value).getIconOrNull(shapeFactory));
            else
                comp.setIcon(null);

            return comp;
        }
    }
}
