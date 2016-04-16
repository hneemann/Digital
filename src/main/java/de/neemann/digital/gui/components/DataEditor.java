package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class DataEditor extends JDialog {
    private final DataField dataField;
    private boolean ok = false;

    public DataEditor(JComponent parent, DataField dataField) {
        this(parent, dataField, null);
    }

    public DataEditor(JComponent parent, DataField dataField, ElementAttributes attr) {
        super(SwingUtilities.windowForComponent(parent), Lang.get("key_data"), attr == null ? ModalityType.MODELESS : ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        int size;
        int bits;
        boolean register;
        if (attr != null) {
            bits = attr.getBits();
            if (attr.contains(Keys.ADDR_BITS))
                size = 1 << attr.get(Keys.ADDR_BITS);
            else
                size = 1 << attr.get(Keys.INPUT_COUNT);

            this.dataField = new DataField(dataField, size, bits);
            register = false;
        } else {
            this.dataField = dataField;
            size = this.dataField.size();
            bits = this.dataField.getBits();
            register = true;
        }

        int cols = 16;
        if (size <= 16) cols = 1;
        else if (size <= 128) cols = 8;

        MyTableModel dm = new MyTableModel(this.dataField, cols);
        JTable table = new JTable(dm);
        table.setDefaultRenderer(MyLong.class, new MyLongRenderer(bits));
        getContentPane().add(new JScrollPane(table));

        if (register) {
            this.dataField.addListener(dm);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    DataEditor.this.dataField.removeListener(dm);
                }
            });
        }


        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok = true;
                dispose();
            }
        }));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        setPreferredSize(new Dimension((cols + 1) * 50, getPreferredSize().height));

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

    private static class MyTableModel implements TableModel, DataField.DataListener {
        private final DataField dataField;
        private final int cols;
        private final int rows;
        private ArrayList<TableModelListener> listener = new ArrayList<>();

        private MyTableModel(DataField dataField, int cols) {
            this.dataField = dataField;
            this.cols = cols;
            rows = (dataField.size() - 1) / cols + 1;
        }

        @Override
        public int getRowCount() {
            return rows;
        }

        @Override
        public int getColumnCount() {
            return cols + 1;
        }

        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0)
                return Lang.get("addr");
            else if (cols > 1)
                return Integer.toHexString(columnIndex - 1).toUpperCase();
            else
                return Lang.get("key_value");
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
                return new MyLong((long) rowIndex * cols);
            }
            return new MyLong(dataField.getData(rowIndex * cols + (columnIndex - 1)));
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            dataField.setData(rowIndex * cols + (columnIndex - 1), ((MyLong) aValue).getValue());
        }

        private void fireEvent(TableModelEvent e) {
            for (TableModelListener l : listener)
                l.tableChanged(e);
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listener.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listener.remove(l);
        }

        @Override
        public void valueChanged(int addr) {
            fireEvent(new TableModelEvent(this, addr / cols));
        }
    }


    private static class MyLongRenderer extends DefaultTableCellRenderer {

        private final int chars;

        private MyLongRenderer(int bits) {
            this.chars = (bits - 1) / 4 + 1;
            setHorizontalAlignment(JLabel.RIGHT);
        }

        @Override
        protected void setValue(Object value) {
            String str = Long.toHexString(((MyLong) value).getValue()).toUpperCase();
            while (str.length() < chars) str = "0" + str;
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
