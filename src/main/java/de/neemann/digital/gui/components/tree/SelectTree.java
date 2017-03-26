package de.neemann.digital.gui.components.tree;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.InsertAction;
import de.neemann.digital.gui.InsertHistory;
import de.neemann.digital.gui.components.CircuitComponent;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * Tree to select items
 * Created by hneemann on 25.03.17.
 */
public class SelectTree extends JTree {

    /**
     * Create a new instance
     *
     * @param library       the library to use
     * @param component     the component to insert the components to
     * @param shapeFactory  the shape factory
     * @param insertHistory the insert history
     */
    public SelectTree(ElementLibrary library, CircuitComponent component, ShapeFactory shapeFactory, InsertHistory insertHistory) {
        super(new MyTreeModel(library));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                TreePath path = getSelectionPath();
                if (path != null && path.getPathCount() > 0) {
                    LibraryNode node = (LibraryNode) path.getLastPathComponent();
                    if (node.isLeaf()) {
                        try {
                            ElementTypeDescription d = node.getDescription();
                            component.setPartToInsert(new VisualElement(d.getName()).setShapeFactory(shapeFactory));
                            insertHistory.add(new InsertAction(node, insertHistory, component, shapeFactory));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

    }
}
