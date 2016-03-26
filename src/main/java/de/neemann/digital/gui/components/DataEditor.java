package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class DataEditor extends JDialog {
    private final DataField dataField;
    private boolean ok = false;

    public DataEditor(JComponent parent, DataField dataField, ElementAttributes attr) {
        super(SwingUtilities.windowForComponent(parent), Lang.get("key_data"), ModalityType.APPLICATION_MODAL);

        int bits = attr.getBits();
        int size;
        if (attr.contains(AttributeKey.InputCount)) {
            size = 1 << attr.get(AttributeKey.InputCount);
        } else if (attr.contains(AttributeKey.AddrBits)) {
            size = 1 << attr.get(AttributeKey.AddrBits);
        } else
            size = dataField.size();

        this.dataField = new DataField(dataField, size);

        JTable table = new JTable(new MyTableModel(this.dataField));
        table.setDefaultRenderer(MyLong.class, new MyLongRenderer(bits));
        getContentPane().add(new JScrollPane(table));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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

    public DataField getDataField() {
        return dataField;
    }

    public boolean showDialog() {
        setVisible(true);
        return ok;
    }

    private class MyTableModel implements TableModel {
        private final DataField dataField;
        private final int rows;
        private ArrayList<TableModelListener> listener = new ArrayList<>();

        public MyTableModel(DataField dataField) {
            this.dataField = dataField;
            rows = (dataField.size() - 1) / 16 + 1;
        }

        @Override
        public int getRowCount() {
            return rows;
        }

        @Override
        public int getColumnCount() {
            return 17;
        }

        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0)
                return Lang.get("addr");
            else
                return Integer.toHexString(columnIndex - 1).toUpperCase();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return MyLong.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return new MyLong(rowIndex * 16);
            }
            return new MyLong(dataField.getData(rowIndex * 16 + (columnIndex - 1)));
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            dataField.setData(rowIndex * 16 + (columnIndex - 1), ((MyLong) aValue).getValue());
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listener.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listener.remove(l);
        }
    }

    private class MyLongRenderer extends DefaultTableCellRenderer {
        private final int bits;

        public MyLongRenderer(int bits) {
            this.bits = bits;
            setHorizontalAlignment(JLabel.RIGHT);
        }

        @Override
        protected void setValue(Object value) {
            String str = Long.toHexString(((MyLong) value).getValue()).toUpperCase();
            super.setValue(str);
        }
    }

    public static class MyLong {
        private final long data;

        public MyLong(String value) {
            data = Long.decode(value);
        }

        public MyLong(long data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "0x" + Long.toHexString(data).toUpperCase();
        }

        public long getValue() {
            return data;
        }
    }

}
