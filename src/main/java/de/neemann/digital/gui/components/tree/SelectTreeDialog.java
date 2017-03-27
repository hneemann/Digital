package de.neemann.digital.gui.components.tree;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.InsertHistory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Tree Selector for components.
 * Created by hneemann on 25.03.17.
 */
public class SelectTreeDialog extends JDialog {

    /**
     * Creates a new instance
     *
     * @param main          the main window
     * @param library       the library to use
     * @param component     the component to insert the components to
     * @param shapeFactory  the shape factory
     * @param insertHistory the insert history
     */
    public SelectTreeDialog(Main main, ElementLibrary library, CircuitComponent component, ShapeFactory shapeFactory, InsertHistory insertHistory) {
        super(main, Lang.get("menu_elements"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        LibraryTreeModel model = new LibraryTreeModel(library);
        JTree tree = new SelectTree(model, component, shapeFactory, insertHistory);
        getContentPane().add(new JScrollPane(tree));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                library.removeListener(model);
            }
        });

        pack();
        setSize(getWidth(), main.getHeight());
        setLocation(main.getLocation().x - getWidth(), main.getLocation().y);
    }
}
