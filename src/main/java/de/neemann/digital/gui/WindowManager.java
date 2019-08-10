/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * Handles the Windows menu entries
 */
public final class WindowManager {

    private static final class InstanceHolderClass {
        private static final WindowManager INSTANCE = new WindowManager();
    }

    /**
     * @return the WindowManager
     */
    public static WindowManager getInstance() {
        return InstanceHolderClass.INSTANCE;
    }

    private final ArrayList<FrameHolder> list;

    private WindowManager() {
        list = new ArrayList<>();
    }

    /**
     * Registers a frame
     *
     * @param frame the frame to register
     * @return the main menu entry to add
     */
    public JMenu registerAndCreateMenu(JFrame frame) {
        FrameHolder frameHolder = new FrameHolder(frame);
        list.add(frameHolder);
        update();
        return frameHolder.getMenu(list);
    }

    private void remove(FrameHolder holder) {
        list.remove(holder);
        update();
    }

    private void update() {
        for (FrameHolder wh : list)
            wh.update(list);
    }

    private static final class FrameHolder {

        private final JFrame frame;
        private JMenu menu;

        private FrameHolder(JFrame frame) {
            this.frame = frame;

            frame.addPropertyChangeListener(propertyChangeEvent -> {
                if (propertyChangeEvent.getPropertyName().equals("title")) {
                    getInstance().update();
                }
            });

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent windowEvent) {
                    getInstance().remove(FrameHolder.this);
                }
            });
        }

        public String getTitle() {
            return frame.getTitle();
        }

        public void update(ArrayList<FrameHolder> list) {
            if (menu != null) {
                menu.removeAll();
                for (FrameHolder wh : list)
                    menu.add(wh.createItem());
            }
        }

        private JMenuItem createItem() {
            return new JMenuItem(new AbstractAction(frame.getTitle()) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    frame.setVisible(true);
                    frame.toFront();
                }
            });
        }

        private JMenu getMenu(ArrayList<FrameHolder> list) {
            if (menu == null) {
                menu = new JMenu(Lang.get("menu_window"));
                update(list);
            }
            return menu;
        }
    }

}
