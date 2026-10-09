/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryListener;
import de.neemann.digital.draw.library.LibraryNode;

import javax.swing.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * The InsertHistory puts the most frequently used elements to the toolbar of the main window.
 * So it's easier to build circuits.
 */
public class InsertHistory implements LibraryListener {
    private static final int MAX_ICONS = 6;
    private final JToolBar bar;
    private final ArrayList<InsertAction> actionsLRU;
    private final IdentityHashMap<InsertAction, JComponent> components;
    private final ElementLibrary library;

    /**
     * Creates a new instance
     *
     * @param bar     the toolbar to put the elements to
     * @param library the library to use
     */
    public InsertHistory(JToolBar bar, ElementLibrary library) {
        this.bar = bar;
        this.library = library;
        actionsLRU = new ArrayList<>();
        components = new IdentityHashMap<>();
    }

    /**
     * Add an action to the toolbar.
     * The history of actions is kept in the least-recently-used (LRU) order.
     * When the toolbar becomes too large, the oldest (LRU) action is removed.
     *
     * @param action the action
     */
    public void add(InsertAction action) {
        if (!actionsLRU.contains(action)) {
            if (actionsLRU.size() >= MAX_ICONS) {
                InsertAction oldest = actionsLRU.get(0);
                bar.remove(components.get(oldest));
                components.remove(oldest);
                actionsLRU.remove(0);
            }
            components.put(action, bar.add(action));
        } else {
            actionsLRU.remove(action);
        }
        actionsLRU.add(action);
    }

    /**
     * @return the last insert action
     */
    public InsertAction getLastInsertAction() {
        if (!actionsLRU.isEmpty()) return actionsLRU.get(actionsLRU.size() - 1);
        return null;
    }

    @Override
    public void libraryChanged(LibraryNode node) {
        updateCustomComponents();
    }

    /**
     * Updates all custom components.
     * If the component no longer exists, it is deleted from the history toolbar.
     */
    private void updateCustomComponents() {
        Iterator<InsertAction> it = actionsLRU.iterator();
        while (it.hasNext()) {
            InsertAction a = it.next();
            if (a.isCustom()) {
                LibraryNode n = library.getElementNodeOrNull(a.getName());
                if (n == null) {  // isn't there, so delete
                    bar.remove(components.get(a));  // removes from the toolbar
                    components.remove(a);           // removes from components
                    it.remove();                    // removes from actionsLRU
                } else a.update(n);
            }
        }
        bar.revalidate();
    }
}
