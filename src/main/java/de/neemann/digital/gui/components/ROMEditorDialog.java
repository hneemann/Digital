/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.rom.ROMInterface;
import de.neemann.digital.core.memory.rom.ROMManagerFile;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The editor to edit the roms
 */
public class ROMEditorDialog extends JDialog {
    private static final Key.KeyEnum<RomHolder.Source> SOURCE = new Key.KeyEnum<>("source", RomHolder.Source.noData, RomHolder.Source.values());
    private static final Key<DataField> DATA = new Key<>("Data", DataField::new)
            .setDependsOn(SOURCE, source -> source.equals(RomHolder.Source.dataField));
    private static final Key<File> DATA_FILE = new Key.KeyFile("lastDataFile", new File(""))
            .setDependsOn(SOURCE, source -> source.equals(RomHolder.Source.file));
    private static final Key<Boolean> BIG_ENDIAN = new Key<>("bigEndian", false).setSecondary()
            .setDependsOn(SOURCE, source -> source.equals(RomHolder.Source.file));

    private static final List<Key> KEY_ARRAY_LIST = new ArrayList<>();

    private final ROMModel romModel;
    private boolean ok = false;

    static {
        KEY_ARRAY_LIST.add(SOURCE);
        KEY_ARRAY_LIST.add(DATA);
        KEY_ARRAY_LIST.add(DATA_FILE);
        KEY_ARRAY_LIST.add(BIG_ENDIAN);
    }

    /**
     * Creates a new instance
     *
     * @param parent     the dialogs parent
     * @param model      the mode touse
     * @param romManager the rom manager
     */
    public ROMEditorDialog(JDialog parent, Model model, ROMManagerFile romManager) {
        super(parent, Lang.get("win_romDialog"), true);

        romModel = new ROMModel();
        for (Node n : model.findNode(n -> n instanceof ROMInterface)) {
            final ROMInterface ri = (ROMInterface) n;
            final String label = ri.getLabel().trim();
            if (label.length() > 0) {
                ROMManagerFile.RomContainer df = romManager.getRomContainer(label);
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
    public ROMManagerFile getROMManager() {
        return romModel.createRomManager();
    }

    private static final class RomHolder {
        enum Source {noData, file, dataField}

        private final ROMInterface ri;
        private boolean bigEndian;
        private Source source;
        private File file;
        private DataField data;

        private RomHolder(ROMInterface ri, ROMManagerFile.RomContainer data) {
            this.ri = ri;
            if (data instanceof ROMManagerFile.RomContainerFile) {
                this.source = Source.file;
                this.data = new DataField(0);
                this.file = ((ROMManagerFile.RomContainerFile) data).getFile();
                this.bigEndian = ((ROMManagerFile.RomContainerFile) data).isBigEndian();
            } else if (data instanceof ROMManagerFile.RomContainerDataField) {
                this.source = Source.dataField;
                this.data = ((ROMManagerFile.RomContainerDataField) data).getDataField(0, null);
                this.file = new File("");
                this.bigEndian = false;
            } else {
                this.source = Source.noData;
                this.data = new DataField(0);
                this.file = new File("");
                this.bigEndian = false;
            }
        }

        @Override
        public String toString() {
            switch (source) {
                case dataField:
                    return ri.getLabel() + " (" + Lang.get("key_source_dataField") + ")";
                case file:
                    return ri.getLabel() + " (" + file + ")";
                default:
                    return ri.getLabel() + " (" + Lang.get("msg_noData") + ")";
            }
        }

        public boolean edit(ROMEditorDialog romEditorDialog) {
            ElementAttributes attr = new ElementAttributes()
                    .set(BIG_ENDIAN, bigEndian)
                    .set(DATA, data)
                    .set(SOURCE, source)
                    .set(Keys.ADDR_BITS, ri.getAddrBits())
                    .set(Keys.BITS, ri.getDataBits())
                    .set(DATA_FILE, file);

            AttributeDialog ad = new AttributeDialog(romEditorDialog, KEY_ARRAY_LIST, attr);
            ElementAttributes mod = ad.showDialog();
            if (mod != null) {
                data = mod.get(DATA);
                data.trim();
                file = mod.get(DATA_FILE);
                bigEndian = mod.get(BIG_ENDIAN);
                source = mod.get(SOURCE);
                return true;
            } else {
                return false;
            }
        }

        public ROMManagerFile.RomContainer getRomContainer() {
            switch (source) {
                case file:
                    return new ROMManagerFile.RomContainerFile(file, bigEndian);
                case dataField:
                    return new ROMManagerFile.RomContainerDataField(data);
                default:
                    return null;
            }
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
            romlist.get(i).source = RomHolder.Source.noData;
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

        private ROMManagerFile createRomManager() {
            ROMManagerFile rm = new ROMManagerFile();

            for (RomHolder rh : romlist) {
                final ROMManagerFile.RomContainer romContainer = rh.getRomContainer();
                if (romContainer != null)
                    rm.addContainer(rh.ri.getLabel(), romContainer);
            }

            return rm;
        }
    }

}
