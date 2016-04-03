package de.neemann.digital.gui.components;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ProbeDialog extends JDialog implements ModelStateObserver {

    private final ModelEvent.Event type;
    private final SignalTableModel tableModel;

    public ProbeDialog(Frame owner, Model model, ModelEvent.Event type) {
        super(owner, Lang.get("win_measures"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.type = type;

        ArrayList<Model.Signal> signals = model.getSignals();
        tableModel = new SignalTableModel(signals);
        JTable list = new JTable(tableModel);
        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
        setAlwaysOnTop(true);

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

        setPreferredSize(new Dimension(150, getPreferredSize().height));

        pack();
        setLocationRelativeTo(owner);
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event.getType() == type || event.getType() == ModelEvent.Event.MANUALCHANGE) {
            tableModel.fireChanged();
        }
    }

    private class SignalTableModel implements TableModel {
        private final ArrayList<Model.Signal> signals;
        private ArrayList<TableModelListener> listeners = new ArrayList<>();

        public SignalTableModel(ArrayList<Model.Signal> signals) {
            this.signals = signals;
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
            if (columnIndex == 0) return Lang.get("key_label");
            else return Lang.get("key_value");
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) return signals.get(rowIndex).getName();
            else return signals.get(rowIndex).getValue().getValueString();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
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
