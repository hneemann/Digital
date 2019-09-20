/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.rom.ROMInterface;
import de.neemann.digital.core.memory.rom.ROMManger;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * The editor to edit the roms
 */
public class ROMEditorDialog extends JDialog {
    private final ROMModel romModel;
    private boolean ok = false;

    /**
     * Creates a new instance
     *
     * @param parent     the dialogs parent
     * @param model      the mode touse
     * @param romManager the rom manager
     */
    public ROMEditorDialog(JDialog parent, Model model, ROMManger romManager) {
        super(parent, Lang.get("win_romDialog"), true);

        romModel = new ROMModel();
        for (Node n : model.findNode(n -> n instanceof ROMInterface)) {
            final ROMInterface ri = (ROMInterface) n;
            final String label = ri.getLabel().trim();
            if (label.length() > 0) {
                DataField df = romManager.getRom(label);
                if (df == null) df = new DataField(0);
                romModel.add(new RomHolder(ri, df));
            }
        }

        romModel.sort();

        final JList<RomHolder> list = new JList<>(romModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getContentPane().add(new JScrollPane(list));

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int i = list.getSelectedIndex();
                if (mouseEvent.getClickCount() == 2 && i >= 0 && i < romModel.getSize())
                    romModel.edit(i);
            }
        });

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(new ToolTipAction(Lang.get("btn_edit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = list.getSelectedIndex();
                if (i >= 0 && i < romModel.getSize())
                    romModel.edit(i);
            }
        }.setToolTip(Lang.get("btn_editRom_tt")).createJButton());
        buttons.add(new ToolTipAction(Lang.get("btn_clearData")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = list.getSelectedIndex();
                if (i >= 0 && i < romModel.getSize())
                    romModel.delete(i);
            }
        }.setToolTip(Lang.get("btn_clearRom_tt")).createJButton());
        getContentPane().add(buttons, BorderLayout.EAST);

        buttons.add(new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok = true;
                dispose();
            }
        }));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * opens the dialog
     *
     * @return true if content was modified
     */
    public boolean showDialog() {
        setVisible(true);
        return ok;
    }

    /**
     * @return returns the modified content
     */
    public ROMManger getROMManager() {
        return romModel.createRomManager();
    }

    private static final class RomHolder {
        private final ROMInterface ri;
        private DataField data;

        private RomHolder(ROMInterface ri, DataField data) {
            this.ri = ri;
            this.data = data;
        }

        @Override
        public String toString() {
            if (!hasData())
                return ri.getLabel() + " (" + Lang.get("msg_noData") + ")";
            else
                return ri.getLabel();
        }

        boolean hasData() {
            return data.getData().length > 0;
        }

        public boolean edit(ROMEditorDialog romEditorDialog) {
            DataEditor de = new DataEditor(romEditorDialog, data, ri.getDataBits(), ri.getAddrBits(), false, SyncAccess.NOSYNC, ri.getIntFormat());
            if (de.showDialog()) {
                data = de.getModifiedDataField();
                return true;
            } else
                return false;
        }
    }

    private final class ROMModel implements ListModel<RomHolder> {
        private final ArrayList<RomHolder> romlist;
        private ArrayList<ListDataListener> listeners = new ArrayList<>();

        private ROMModel() {
            romlist = new ArrayList<>();
        }

        public void add(RomHolder ri) {
            romlist.add(ri);
        }

        @Override
        public int getSize() {
            return romlist.size();
        }

        @Override
        public RomHolder getElementAt(int i) {
            return romlist.get(i);
        }

        @Override
        public void addListDataListener(ListDataListener listDataListener) {
            listeners.add(listDataListener);
        }

        @Override
        public void removeListDataListener(ListDataListener listDataListener) {
            listeners.remove(listDataListener);
        }

        private void sort() {
            romlist.sort(Comparator.comparing(r -> r.ri.getLabel()));
        }

        public void delete(int i) {
            romlist.get(i).data = new DataField(0);
            fireChanged(i);
        }

        public void edit(int i) {
            RomHolder rh = romlist.get(i);
            if (rh.edit(ROMEditorDialog.this))
                fireChanged(i);
        }

        private void fireChanged(int i) {
            ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i, i);
            for (ListDataListener l : listeners)
                l.contentsChanged(ev);
        }

        private ROMManger createRomManager() {
            ROMManger rm = new ROMManger();

            for (RomHolder rh : romlist)
                rm.addRom(rh.ri.getLabel(), rh.data);

            return rm;
        }
    }

}
