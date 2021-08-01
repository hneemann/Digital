/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.*;
import de.neemann.digital.core.memory.RAMInterface;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class ProbeDialog extends JDialog implements ModelStateObserverTyped {

    private final ModelEventType type;
    private final SignalTableModel tableModel;
    private boolean tableUpdateEnable = true;

    /**
     * Creates a new instance
     *
     * @param owner    the owner
     * @param model    the model to run
     * @param type     the event type which fires a dialog repaint
     * @param ordering the names list used to order the measurement values
     */
    public ProbeDialog(Frame owner, Model model, ModelEventType type, List<String> ordering) {
        super(owner, Lang.get("win_measures"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.type = type;

        ArrayList<Signal> signals = model.getSignalsCopy();
        new OrderMerger<String, Signal>(ordering) {
            @Override
            public boolean equals(Signal a, String b) {
                return a.getName().equals(b);
            }
        }.order(signals);

        tableModel = new SignalTableModel(signals, model);
        JTable list = new JTable(tableModel);
        list.setRowHeight(list.getFont().getSize() * 6 / 5);
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                model.addObserver(ProbeDialog.this);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                model.removeObserver(ProbeDialog.this);
            }
        });

        List<Node> memoryList = model.findNode(n -> n instanceof RAMInterface);
        if (memoryList.size() > 0) {
            JMenuBar bar = new JMenuBar();
            final JMenu memory = new JMenu(Lang.get("menu_probe_memory"));
            memory.setToolTipText(Lang.get("menu_probe_memory_tt"));
            for (Node n : memoryList) {
                if (n instanceof RAMInterface) {
                    RAMInterface ram = (RAMInterface) n;
                    String name = ram.getLabel();
                    if (name == null || name.length() == 0)
                        name = ram.getClass().getSimpleName();
                    memory.add(new JMenuItem(new ToolTipAction(name) {
                        @Override
                        public void actionPerformed(ActionEvent actionEvent) {
                            new DataEditor(ProbeDialog.this,
                                    ram.getMemory(),
                                    ram.getDataBits(),
                                    ram.getAddrBits(),
                                    true,
                                    model, ram.getValueFormatter())
                                    .setNode(n)
                                    .showDialog(ram.getLabel(), model);
                        }
                    }));
                }
            }
            bar.add(memory);
            setJMenuBar(bar);
        }

        setPreferredSize(new Dimension(150, getPreferredSize().height));

        pack();
        setLocationRelativeTo(owner);
    }

    private final AtomicBoolean paintPending = new AtomicBoolean();

    @Override
    public void handleEvent(ModelEvent event) {
        if (event.getType() == type || event == ModelEvent.CHECKBURN) {
            if (tableUpdateEnable) {
                if (paintPending.compareAndSet(false, true)) {
                    SwingUtilities.invokeLater(() -> {
                        tableModel.fireChanged();
                        paintPending.set(false);
                    });
                }
            }
        }
        switch (event.getType()) {
            case RUN_TO_BREAK:
                tableUpdateEnable = false;
                break;
            case BREAK:
            case CLOSED:
                tableUpdateEnable = true;
                SwingUtilities.invokeLater(tableModel::fireChanged);
                break;
        }
    }

    @Override
    public ModelEventType[] getEvents() {
        return new ModelEventType[]{type, ModelEventType.CHECKBURN, ModelEventType.RUN_TO_BREAK, ModelEventType.BREAK, ModelEventType.CLOSED};
    }

    private static class SignalTableModel implements TableModel {
        private final ArrayList<Signal> signals;
        private final SyncAccess modelSync;
        private final ArrayList<TableModelListener> listeners = new ArrayList<>();

        SignalTableModel(ArrayList<Signal> signals, SyncAccess modelSync) {
            this.signals = signals;
            this.modelSync = modelSync;
        }

        @Override
        public int getRowCount() {
            return signals.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return Lang.get("key_Label");
            else return Lang.get("key_Value");
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1 && signals.get(rowIndex).getSetter() != null;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) return signals.get(rowIndex).getName();
            else return signals.get(rowIndex).getValueString();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                Signal.Setter s = signals.get(rowIndex).getSetter();
                if (s != null)
                    try {
                        final String str = aValue.toString();
                        if (str.equals("?") || str.equals("z") || str.equals("Z")) {
                            modelSync.modify(() -> s.set(0, -1));
                        } else {
                            long value = Bits.decode(str);
                            modelSync.modify(() -> s.set(value, 0));
                        }
                    } catch (Bits.NumberFormatException e) {
                        // do nothing in this case!
                    }
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }

        public void fireChanged() {
            TableModelEvent e = new TableModelEvent(this, 0, signals.size() - 1);
            for (TableModelListener l : listeners)
                l.tableChanged(e);
        }
    }
}
