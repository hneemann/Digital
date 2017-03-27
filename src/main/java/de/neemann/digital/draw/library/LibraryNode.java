package de.neemann.digital.draw.library;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.shapes.ShapeFactory;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A node in the components library
 * Created by hneemann on 25.03.17.
 */
public class LibraryNode implements Iterable<LibraryNode> {

    private final ArrayList<LibraryNode> children;
    private final String translatedName;
    private final String name;
    private final DescriptionCreator creator;
    private ElementTypeDescription description;
    private ImageIcon icon;
    private ElementLibrary library;
    private LibraryNode parent;

    /**
     * Creates a new node with the given name.
     * The node can have children
     *
     * @param name name of the node
     */
    LibraryNode(String name) {
        this.name = name;
        this.translatedName = name;
        this.children = new ArrayList<>();
        this.description = null;
        this.creator = null;
    }

    /**
     * Creates a new leaf
     *
     * @param description the description
     */
    private LibraryNode(ElementTypeDescription description) {
        this.children = null;
        this.description = description;
        this.name = description.getName();
        this.translatedName = description.getTranslatedName();
        this.creator = null;
    }

    /**
     * Creates a new leaf
     *
     * @param name    the name of the leaf
     * @param creator used to create the {@link ElementTypeDescription} if necessary
     */
    LibraryNode(String name, DescriptionCreator creator) {
        this.children = null;
        this.name = name;
        this.translatedName = name;
        this.creator = creator;
    }

    /**
     * Adds a node.
     * Throws an exception if this node is a leaf
     *
     * @param node the node to add
     */
    void add(LibraryNode node) {
        children.add(node);
        node.parent = this;
        node.setLibrary(library);
    }

    void add(ElementTypeDescription node) {
        add(new LibraryNode(node));
    }

    /**
     * Traverse the tree
     *
     * @param v   a visitor
     * @param <V> the type of the visitor
     * @return the visitor
     */
    public <V extends Visitor> V traverse(V v) {
        v.visit(this);
        if (children != null) {
            for (LibraryNode tn : children)
                tn.traverse(v);
        }
        return v;
    }

    /**
     * @return trie if this is a leaf
     */
    public boolean isLeaf() {
        return description != null || creator != null;
    }

    /**
     * @return true if the description is already loaded
     */
    public boolean isDescriptionLoaded() {
        return description != null;
    }

    /**
     * Returns the description of the element
     *
     * @return the description
     * @throws IOException IOException
     */
    public ElementTypeDescription getDescription() throws IOException {
        if (description == null) {
            description = creator.createDescription();
            library.fireLibraryChanged(this);
        }
        return description;
    }

    /**
     * @return the translated name of the element
     */
    public String getTranslatedName() {
        return translatedName;
    }

    /**
     * @return the name od id of this element
     */
    public String getName() {
        return name;
    }

    @Override
    public Iterator<LibraryNode> iterator() {
        return children.iterator();
    }

    /**
     * all children are removed
     */
    public void removeAll() {
        children.clear();
    }

    /**
     * @return true if this node is empty
     */
    public boolean isEmpty() {
        if (isLeaf())
            return false;

        return children.isEmpty();
    }

    /**
     * @return returns the description if present, null otherwise
     */
    public ElementTypeDescription getDescriptionOrNull() {
        return description;
    }

    /**
     * get the child with index i
     *
     * @param i the index
     * @return the child
     */
    public LibraryNode getChild(int i) {
        return children.get(i);
    }

    /**
     * @return the number of children
     */
    public int size() {
        return children == null ? 0 : children.size();
    }

    /**
     * Returns the index of the gicen child
     *
     * @param node the node
     * @return the nodes index
     */
    public int indexOf(LibraryNode node) {
        return children.indexOf(node);
    }

    @Override
    public String toString() {
        return translatedName;
    }

    /**
     * Returns the icon.
     * If icon not available the icon is created
     *
     * @param shapeFactory the shape factory to create the icon
     * @return the icon
     * @throws IOException IOException
     */
    public ImageIcon getIcon(ShapeFactory shapeFactory) throws IOException {
        getDescription();
        return getIconOrNull(shapeFactory);
    }

    /**
     * Returns the icon.
     * If icon not available null is returned
     *
     * @param shapeFactory the shape factory to create the icon
     * @return the icon or null
     */
    public ImageIcon getIconOrNull(ShapeFactory shapeFactory) {
        if (icon == null && description != null)
            icon = new VisualElement(description.getName()).setShapeFactory(shapeFactory).createIcon(75);
        return icon;
    }

    /**
     * Removes the given child.
     *
     * @param child the element to remove
     */
    public void remove(LibraryNode child) {
        children.remove(child);
    }

    /**
     * Sets the library this node belongs to
     *
     * @param library the library
     */
    public void setLibrary(ElementLibrary library) {
        if (this.library != library) {
            this.library = library;
            if (children != null)
                for (LibraryNode c : children)
                    c.setLibrary(library);
        }
    }

    /**
     * returns the tree path
     *
     * @return the path
     */
    public Object[] getPath() {
        ArrayList<Object> path = new ArrayList<>();
        LibraryNode n = this;
        while (n != null) {
            path.add(0, n);
            n = n.parent;
        }
        return path.toArray(new Object[path.size()]);
    }

}
