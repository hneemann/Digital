package de.neemann.digital.gui.components.tree;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryListener;
import de.neemann.digital.draw.library.LibraryNode;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;

/**
 * TreeModel based on a {@link ElementLibrary}
 * Created by hneemann on 25.03.17.
 */
public class MyTreeModel implements TreeModel, LibraryListener {
    private final LibraryNode root;
    private final ElementLibrary library;
    private final ArrayList<TreeModelListener> listeners = new ArrayList<>();

    MyTreeModel(ElementLibrary library) {
        root = library.getRoot();
        this.library = library;
        library.addListener(this);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object o, int i) {
        return ((LibraryNode) o).getChild(i);
    }

    @Override
    public int getChildCount(Object o) {
        return ((LibraryNode) o).size();
    }

    @Override
    public boolean isLeaf(Object o) {
        return ((LibraryNode) o).isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath treePath, Object o) {

    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {
        return ((LibraryNode) o).indexOf((LibraryNode) o1);
    }

    @Override
    public void addTreeModelListener(TreeModelListener treeModelListener) {
        listeners.add(treeModelListener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener treeModelListener) {
        listeners.remove(treeModelListener);
    }

    @Override
    public void libraryChanged() {
        LibraryNode custom = library.getCustomNode();
        if (custom != null) {
            final TreeModelEvent treeModelEvent = new TreeModelEvent(this, new TreePath(new Object[]{root, custom}));
            for (TreeModelListener l : listeners)
                l.treeStructureChanged(treeModelEvent);
        }
    }
}
