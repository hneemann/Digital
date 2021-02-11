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
import java.util.Iterator;

/**
 * TreeModel based on a {@link ElementLibrary}
 */
public class LibraryTreeModel implements TreeModel, LibraryListener {
    private final LibraryNode root;
    private final ElementLibrary library;
    private final Filter filter;
    private final ArrayList<TreeModelListener> listeners = new ArrayList<>();
    private final HashMap<LibraryNode, Container> map;

    /**
     * Creates a new library tree model
     *
     * @param library the library
     */
    public LibraryTreeModel(ElementLibrary library) {
        this(library, null);
    }

    /**
     * Creates a new library tree model
     *
     * @param library the library
     * @param filter  used to filter library entries
     */
    public LibraryTreeModel(ElementLibrary library, Filter filter) {
        root = library.getRoot();
        this.library = library;
        this.filter = filter;
        map = new HashMap<>();
        library.addListener(this);
    }

    /**
     * Called to detach the model from the library
     */
    public void close() {
        library.removeListener(this);
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
        if (node.isLeaf()) {
            map.remove(node);
            TreeModelEvent treeModelEvent = new TreeModelEvent(this, new TreePath(node.getPath()));
            for (TreeModelListener l : listeners)
                l.treeNodesChanged(treeModelEvent);
        } else {
            map.clear();
            TreeModelEvent treeModelEvent = new TreeModelEvent(this, new TreePath(root.getPath()));
            for (TreeModelListener l : listeners)
                l.treeStructureChanged(treeModelEvent);
        }
    }

    /**
     * @return the parent of the first leave
     */
    public LibraryNode getFirstLeafParent() {
        Container c = getContainer(root);
        if (c.size() == 0)
            return root;
        while (true) {
            for (LibraryNode n : c)
                if (n.isLeaf())
                    return c.node;
            c = getContainer(c.getChild(0));
        }
    }

    /**
     * @return true if this model is filtered
     */
    public boolean isFiltered() {
        return filter != null;
    }

    private Container getContainer(LibraryNode libraryNode) {
        Container c = map.get(libraryNode);
        if (c == null) {
            c = new Container(libraryNode, filter);
            map.put(libraryNode, c);
        }
        return c;
    }

    private final class Container implements Iterable<LibraryNode> {
        private final ArrayList<LibraryNode> list;
        private final LibraryNode node;

        private Container(LibraryNode libraryNode, Filter filter) {
            list = new ArrayList<>(libraryNode.size());
            node = libraryNode;
            for (LibraryNode ln : libraryNode) {
                if (!ln.isHidden()) {
                    if (filter == null)
                        list.add(ln);
                    else {
                        if (ln.isLeaf()) {
                            if (filter.accept(ln))
                                list.add(ln);
                        } else {
                            Container c = new Container(ln, filter);
                            if (c.size() > 0) {
                                list.add(ln);
                                map.put(ln, c);
                            }
                        }
                    }
                }
            }
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

        @Override
        public Iterator<LibraryNode> iterator() {
            return list.iterator();
        }
    }

    /**
     * filter interface
     */
    public interface Filter {
        /**
         * Returns true if the node should be shown in the tree
         *
         * @param node the node
         * @return true if visible
         */
        boolean accept(LibraryNode node);
    }
}
