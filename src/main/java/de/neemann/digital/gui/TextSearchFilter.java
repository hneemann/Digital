/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.gui.components.tree.LibraryTreeModel;

/**
 * Used to filter nodes in the tree view
 */
public class TextSearchFilter implements LibraryTreeModel.Filter {
    private final String filterStr;

    /**
     * Creates a new filter
     *
     * @param filterStr the search string
     */
    public TextSearchFilter(String filterStr) {
        this.filterStr = filterStr.toLowerCase();
    }

    @Override
    public boolean accept(LibraryNode node) {
        return node.getName().toLowerCase().contains(filterStr)
                || node.getTranslatedName().toLowerCase().contains(filterStr);
    }
}
