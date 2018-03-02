/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.tree;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryListener;
import de.neemann.digital.draw.library.LibraryNode;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * TreeModel based on a {@link ElementLibrary}
 */
public class LibraryTreeModel implements TreeModel, LibraryListener {
    private final LibraryNode root;
    private final ArrayList<TreeModelListener> listeners = new ArrayList<>();
    private HashMap<LibraryNode, Container> map;

    /**
     * Creates a new library tree model
     *
     * @param library the library
     */
    public LibraryTreeModel(ElementLibrary library) {
        root = library.getRoot();
        map = new HashMap<>();
        library.addListener(this);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object o, int i) {
        return getContainer((LibraryNode) o).getChild(i);
    }

    @Override
    public int getChildCount(Object o) {
        return getContainer((LibraryNode) o).size();
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
        return getContainer((LibraryNode) o).indexOf((LibraryNode) o1);
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
    public void libraryChanged(LibraryNode node) {
        final TreeModelEvent treeModelEvent = new TreeModelEvent(this, new TreePath(node.getPath()));
        for (TreeModelListener l : listeners)
            l.treeStructureChanged(treeModelEvent);
    }

    /**
     * Same as getRoot() but returns the typed root element
     *
     * @return the root LibraryNode
     */
    public LibraryNode getTypedRoot() {
        return root;
    }

    private Container getContainer(LibraryNode libraryNode) {
        return map.computeIfAbsent(libraryNode, Container::new);
    }

    private static final class Container {
        private final ArrayList<LibraryNode> list;

        private Container(LibraryNode libraryNode) {
            list = new ArrayList<>(libraryNode.size());
            for (LibraryNode ln : libraryNode)
                if (!ln.isHidden())
                    list.add(ln);
        }

        private LibraryNode getChild(int i) {
            return list.get(i);
        }

        private int size() {
            return list.size();
        }

        private int indexOf(LibraryNode o1) {
            return list.indexOf(o1);
        }
    }


}
