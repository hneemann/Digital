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
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;

/**
 * Tree to select items
 */
public class SelectTree extends JTree {

    private final ShapeFactory shapeFactory;

    private final HashSet<TreePath> expandedPaths;
    private boolean saveExpandedPaths;

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
        expandedPaths = new HashSet<>();
        saveExpandedPaths = true;
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
        addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                if (saveExpandedPaths) {
                    expandedPaths.add(event.getPath());
                    System.out.println("Added: " + event.getPath());
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                expandedPaths.remove(event.getPath());
                System.out.println("Removed: " + event.getPath());
            }
        });
        setCellRenderer(new MyCellRenderer());
        setToolTipText("");

        // open first child
        expandPath(new TreePath(model.getFirstLeafParent().getPath()));
    }

    /**
     * Expand a path, but don't restore its expanded state when
     * {@link SelectTree#setModelAndRestoreExpansion(TreeModel)} is used.
     *
     * @param path path to expand.
     */
    public void expandPathTemporarily(TreePath path) {
        saveExpandedPaths = false;
        expandPath(path);
        saveExpandedPaths = true;
    }

    /**
     * Set a new model and restore previously expanded nodes from previous model.
     *
     * @param newModel new model to use.
     */
    public void setModelAndRestoreExpansion(TreeModel newModel) {
        setModel(newModel);
        for (TreePath path : expandedPaths) {
            expandPath(path);
        }
    }

    /**
     * @return a set of expanded library nodes, excluding temporarily expanded nodes.
     */
    public HashSet<LibraryNode> getExpandedNodes() {
        HashSet<LibraryNode> expandedNodes = new HashSet<>();
        for (TreePath path : expandedPaths) {
            expandedNodes.add((LibraryNode) path.getLastPathComponent());
        }
        return expandedNodes;
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

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (getRowCount() == 0) {
            g.setColor(Color.GRAY);
            String text = Lang.get("key_search_noResults");
            g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2,
                    Math.min((getHeight() + getFont().getSize()) / 2, 100));
        }
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
