package de.neemann.digital.gui;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class InsertHistory {

    private final JToolBar bar;
    private final ArrayList<AbstractAction> actions;
    private final int removePos;

    public InsertHistory(JToolBar bar) {
        this.bar = bar;
        actions = new ArrayList<>();
        removePos = bar.getComponentCount();
    }

    public void add(AbstractAction action) {
        if (!actions.contains(action)) {
            actions.add(action);
            bar.add(action);
            if (actions.size() > 3) {
                actions.remove(0);
                bar.remove(removePos);
            }
        }
    }
}
