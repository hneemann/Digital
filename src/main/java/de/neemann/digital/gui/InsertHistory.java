package de.neemann.digital.gui;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryListener;
import de.neemann.digital.draw.library.LibraryNode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The InsertHistory puts the most frequently used elements to the toolbar of the main window.
 * So its easier to build circuits.
 *
 * @author hneemann
 */
public class InsertHistory implements LibraryListener {

    private static final int MAX_ICONS = 6;
    private final JToolBar bar;
    private final ElementLibrary library;
    private final ArrayList<WrapperAction> wrappers;
    private int mainTime;

    /**
     * Creates a new instance
     *
     * @param bar the toolbar to put the elements to
     */
    public InsertHistory(JToolBar bar, ElementLibrary library) {
        this.bar = bar;
        this.library = library;
        wrappers = new ArrayList<>();
    }

    /**
     * Add an action to the toolbar.
     * The given action is wrapped by an action which counts the usage.
     * So its possible to remove the item which is not used the longest time when the toolbar becomes to large.
     *
     * @param action the action
     */
    public void add(InsertAction action) {
        if (!contains(action)) {
            WrapperAction wrapper = new WrapperAction(action, bar.getComponentCount());
            wrappers.add(wrapper);
            bar.add(wrapper);
            if (wrappers.size() > MAX_ICONS) {
                int oldest = findOldestIndex();
                removeWrapperFromBar(wrappers.get(oldest));
                wrappers.remove(oldest);
            }
        }
    }

    private void removeWrapperFromBar(WrapperAction wrapper) {
        final int position = wrapper.componentPosition;
        bar.remove(position);
        for (WrapperAction w : wrappers)
            if (w.componentPosition > position)
                w.componentPosition--;
    }

    private int findOldestIndex() {
        int found = -1;
        int oldestTime = mainTime;
        for (int i = 0; i < wrappers.size(); i++) {
            WrapperAction wrapper = wrappers.get(i);
            if (wrapper.time < oldestTime) {
                found = i;
                oldestTime = wrapper.time;
            }
        }
        return found;
    }

    private boolean contains(InsertAction action) {
        for (WrapperAction wrapper : wrappers)
            if (wrapper.action.getName().equals(action.getName()))
                return true;
        return false;
    }


    @Override
    public void libraryChanged(LibraryNode node) {
        removeAllCustomComponents();
    }

    /**
     * remove all custom components
     */
    private void removeAllCustomComponents() {
        Iterator<WrapperAction> it = wrappers.iterator();
        while (it.hasNext()) {
            WrapperAction w = it.next();
            if (w.action.isCustom()) {
                LibraryNode n = library.getElementNodeOrNull(w.action.getName());
                if (n == null) {  // is'nt there, so delete
                    removeWrapperFromBar(w);
                    it.remove();
                } else
                    w.update(n);
            }
        }
        bar.revalidate();
    }

    private final class WrapperAction extends AbstractAction {
        private final InsertAction action;
        private int componentPosition;
        private int time;

        private WrapperAction(InsertAction action, int componentPosition) {
            super(action.getValue(Action.NAME).toString(), (Icon) action.getValue(Action.SMALL_ICON));
            this.action = action;
            this.componentPosition = componentPosition;
            time = mainTime++;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            action.actionPerformed(e);
            time = mainTime++;
        }

        public void update(LibraryNode n) {
            action.update(n);
            putValue(Action.SMALL_ICON, action.getValue(Action.SMALL_ICON));
        }
    }
}
