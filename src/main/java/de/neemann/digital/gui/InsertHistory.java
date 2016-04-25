package de.neemann.digital.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * The InsertHistory puts the most frequently used elements to the toolbar of the main window.
 * So its easier to build circuits.
 *
 * @author hneemann
 */
public class InsertHistory {

    private static final int MAX_ICONS = 6;
    private final JToolBar bar;
    private final ArrayList<WrapperAction> wrappers;
    private int mainTime;

    /**
     * Creates a new instance
     *
     * @param bar the toolbar to put the elements to
     */
    public InsertHistory(JToolBar bar) {
        this.bar = bar;
        wrappers = new ArrayList<>();
    }

    /**
     * Add an action to the toolbar.
     * The given action is wrapped by an action which counts the usage.
     * So its possible to remove the item which is not used the longest time when the toolbar becomes to large.
     *
     * @param action the action
     */
    public void add(AbstractAction action) {
        if (!contains(action)) {
            WrapperAction wrapper = new WrapperAction(action, bar.getComponentCount());
            wrappers.add(wrapper);
            bar.add(wrapper);
            if (wrappers.size() > MAX_ICONS) {
                int oldest = findOldestIndex();
                wrapper = wrappers.get(oldest);
                bar.remove(wrapper.componentPosition);
                for (int i = oldest; i < wrappers.size(); i++)
                    wrappers.get(i).componentPosition--;
                wrappers.remove(oldest);
            }
        }
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

    private boolean contains(AbstractAction action) {
        for (WrapperAction wrapper : wrappers)
            if (wrapper.action == action)
                return true;
        return false;
    }

    private final class WrapperAction extends AbstractAction {

        private final AbstractAction action;
        private int componentPosition;
        private int time;

        private WrapperAction(AbstractAction action, int componentPosition) {
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
    }
}
