/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.IconCreator;
import de.neemann.gui.LineBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A node in the components library
 */
public class LibraryNode implements Iterable<LibraryNode> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNode.class);
    private static final Icon ICON_NOT_UNIQUE = IconCreator.create("testFailed.png");

    private final ArrayList<LibraryNode> children;
    private final String translatedName;
    private final String name;
    private final File file;
    private final boolean isHidden;
    private ElementTypeDescription description;
    private String toolTipText;
    private ImageIcon icon;
    private ElementLibrary library;
    private LibraryNode parent;
    private boolean unique;
    private boolean descriptionImportError = false;

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
        this.toolTipText = null;
        this.file = null;
        this.isHidden = false;
    }

    /**
     * Creates a new leaf
     *
     * @param description the description
     */
    private LibraryNode(ElementTypeDescription description) {
        this.children = null;
        this.description = description;
        this.toolTipText = null;
        this.name = description.getName();
        this.translatedName = description.getTranslatedName();
        this.file = null;
        this.isHidden = false;
    }

    /**
     * Creates a new leaf
     *
     * @param file the file containing the leaf
     */
    LibraryNode(File file, boolean isLibrary) {
        children = null;
        name = file.getName();
        if (name.toLowerCase().endsWith(".dig"))
            translatedName = name.substring(0, name.length() - 4);
        else
            translatedName = name;

        isHidden = isLibrary && name.endsWith("-inc.dig");

        this.file = file;
    }

    /**
     * Adds a node.
     * Throws an exception if this node is a leaf
     *
     * @param node the node to add
     * @return this for chained calls
     */
    LibraryNode add(LibraryNode node) {
        children.add(node);
        node.parent = this;
        node.setLibrary(library);
        return this;
    }

    LibraryNode add(ElementTypeDescription node) {
        add(new LibraryNode(node));
        return this;
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
     * @return true if this is a leaf
     */
    public boolean isLeaf() {
        return description != null || file != null;
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
     * @return the description, null if not available
     **/
    public ElementTypeDescription getDescriptionOrNull() {
        return description;
    }

    /**
     * Returns the description of the element
     *
     * @return the description
     * @throws IOException IOException
     */
    public ElementTypeDescription getDescription() throws IOException {
        if (description == null) {
            if (!unique)
                throw new IOException(Lang.get("err_file_N0_ExistsTwiceBelow_N1", file.getName(), library.getRootFilePath()));
            try {
                description = library.importElement(file);
            } catch (IOException e) {
                descriptionImportError = true;
                throw e;
            }
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
    public boolean isCustom() {
        return file != null;
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
     * get the child with the given name
     *
     * @param name the name
     * @return the child
     */
    public LibraryNode getChild(String name) {
        for (LibraryNode n : children)
            if (n.getName().equals(name))
                return n;
        return null;
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
    public Icon getIcon(ShapeFactory shapeFactory) throws IOException {
        if (descriptionImportError)
            return ICON_NOT_UNIQUE;

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
    public Icon getIconOrNull(ShapeFactory shapeFactory) {
        if (unique) {
            if (icon == null && description != null)
                icon = setWideShapeFlagTo(
                        new VisualElement(description.getName())
                                .setShapeFactory(shapeFactory)
                ).createIcon(75);
            return icon;
        } else
            return ICON_NOT_UNIQUE;
    }

    /**
     * Sets the wide shape flag to this element if necessary
     *
     * @param visualElement the visual element
     * @return the given visual element
     */
    public VisualElement setWideShapeFlagTo(VisualElement visualElement) {
        // set the wide shape option to the element
        try {
            if (Settings.getInstance().get(Keys.SETTINGS_IEEE_SHAPES)
                    && getDescription().hasAttribute(Keys.WIDE_SHAPE)
                    && !visualElement.equalsDescription(Not.DESCRIPTION))
                visualElement.setAttribute(Keys.WIDE_SHAPE, true);
        } catch (IOException e1) {
            // do nothing on error
        }
        return visualElement;
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
     * @return this for chained calls
     */
    public LibraryNode setLibrary(ElementLibrary library) {
        if (this.library != library) {
            this.library = library;
            if (children != null)
                for (LibraryNode c : children)
                    c.setLibrary(library);
        }
        return this;
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
        return path.toArray(new Object[0]);
    }

    /**
     * Invalidate this node
     */
    public void invalidate() {
        description = null;
        toolTipText = null;
        icon = null;
        library.fireLibraryChanged(this);
    }

    /**
     * @return the tool tip text
     */
    public String getToolTipText() {
        if (isCustom()) {
            if (isUnique()) {
                if (description == null) {
                    if (toolTipText == null) {
                        try {
                            LOGGER.debug("load tooltip from " + file);
                            Circuit c = Circuit.loadCircuit(file, null);
                            toolTipText = new LineBreaker().toHTML().breakLines(Lang.evalMultilingualContent(c.getAttributes().get(Keys.DESCRIPTION)));
                        } catch (Exception e) {
                            toolTipText = Lang.get("msg_fileNotImportedYet");
                        }
                    }
                    return toolTipText;
                } else
                    return new LineBreaker().toHTML().breakLines(description.getDescription(new ElementAttributes()));
            } else
                return new LineBreaker().toHTML().breakLines(Lang.get("msg_fileIsNotUnique"));
        } else
            return new LineBreaker().toHTML().breakLines(Lang.getNull("elem_" + getName() + "_tt"));
    }

    /**
     * sets the unique state of this node
     *
     * @param unique true if this node is unique
     */
    void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * @return true if element is unique
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * @return the file containing this circuit
     */
    public File getFile() {
        return file;
    }

    /**
     * If the hidden flag is set, this circuit should not appear in the select menus
     *
     * @return the hidden flag
     */
    public boolean isHidden() {
        return isHidden;
    }

    /**
     * Checks if both files are equal.
     * If one of the files is null, false is returned.
     *
     * @param other the other file
     * @return true if both files are equal.
     */
    public boolean equalsFile(LibraryNode other) {
        if (file == null || other.file == null)
            return false;

        return file.equals(other.file);
    }
}
