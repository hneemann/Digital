/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.*;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.importer.Importer;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.MyFileChooser;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Used to edit ROM data fields.
 * Looks like a HEX editor.
 */
public class DataEditor extends JDialog {
    private static final Color MYGRAY = new Color(230, 230, 230);
    private static File lastUsedFileName;
    private final ValueFormatter addrFormat;
    private final ValueFormatter dataFormat;
    private final int addrBits;
    private final int dataBits;
    private final DataField localDataField;
    private final JTable table;
    private boolean ok = false;
    private File fileName;
    private Node node;

    /**
     * Creates a new instance
     *
     * @param parent         the parent
     * @param dataField      the data to edit
     * @param dataBits       the bit count of the values to edit
     * @param addrBits       the bit count of the adresses
     * @param modelIsRunning true if model is running
     * @param modelSync      used to access the running model
     * @param dataFormat     the integer format to be used
     */
    public DataEditor(Component parent, DataField dataField, int dataBits, int addrBits, boolean modelIsRunning, SyncAccess modelSync, ValueFormatter dataFormat) {
        super(SwingUtilities.windowForComponent(parent), Lang.get("key_Data"), modelIsRunning ? ModalityType.MODELESS : ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addrBits = addrBits;
        this.dataBits = dataBits;
        this.dataFormat = dataFormat;
        if (dataFormat.isSuitedForAddresses())
            addrFormat = dataFormat;
        else
            addrFormat = IntFormat.HEX_FORMATTER;

        if (modelIsRunning)
            localDataField = dataField;
        else
            localDataField = new DataField(dataField);

        final int size = 1 << addrBits;
        final int cols = calcCols(size, dataBits, dataFormat);
        final int rows = (size - 1) / cols + 1;

        int tableWidth = 0;
        MyTableModel dm = new MyTableModel(this.localDataField, cols, rows, modelSync);
        table = new JTable(dm);
        final FontMetrics fontMetrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(fontMetrics.getHeight() * 9 / 8);
        int widthOfZero = fontMetrics.stringWidth("00000000") / 8;
        int widthOfData = widthOfZero * (dataFormat.strLen(dataBits) + 1);
        for (int c = 1; c < table.getColumnModel().getColumnCount(); c++) {
            tableWidth += widthOfData;
            TableColumn col = table.getColumnModel().getColumn(c);
            col.setPreferredWidth(widthOfData);
        }

        DefaultTableCellRenderer dataRenderer = new MyRenderer(dataFormat, dataBits);
        dataRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.setDefaultRenderer(Long.class, dataRenderer);
        table.setDefaultEditor(Long.class, new MyEditor(dataFormat, dataBits));

        DefaultTableCellRenderer addrRenderer = new MyRenderer(addrFormat, addrBits);
        addrRenderer.setBackground(MYGRAY);
        addrRenderer.setHorizontalAlignment(JLabel.RIGHT);
        TableColumn addrColumn = table.getColumnModel().getColumn(0);
        addrColumn.setCellRenderer(addrRenderer);
        int widthOfAddr = widthOfZero * (addrFormat.strLen(addrBits) + 1);
        addrColumn.setPreferredWidth(widthOfAddr);
        tableWidth += widthOfAddr;

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane);
        Dimension dim = table.getPreferredScrollableViewportSize();
        scrollPane.setPreferredSize(new Dimension(tableWidth, dim.height));

        if (modelIsRunning) {
            dataField.addListener(dm);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    dataField.removeListener(dm);
                }
            });
        } else {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(new JButton(new AbstractAction(Lang.get("ok")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (table.isEditing()) {
                        table.getCellEditor().stopCellEditing();
                    } else {
                        ok = true;
                        dispose();
                    }
                }
            }));
            getContentPane().add(buttons, BorderLayout.SOUTH);

            JMenuBar menuBar = new JMenuBar();
            JMenu data = new JMenu(Lang.get("menu_file"));

            data.add(new ToolTipAction(Lang.get("btn_clearData")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    localDataField.clearAll();
                    dm.fireEvent(new TableModelEvent(dm));
                }
            }.setToolTip(Lang.get("btn_clearData_tt")).createJMenuItem());
            data.add(new ToolTipAction(Lang.get("btn_load")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new MyFileChooser();
                    JCheckBox bigEndian = new JCheckBox(Lang.get("msg_bigEndian"));
                    if (dataBits > 8) {
                        bigEndian.setToolTipText(Lang.get("key_bigEndian_tt"));
                        fc.setAccessory(bigEndian);
                    }
                    setFileNameTo(fc);
                    fc.setFileFilter(new FileNameExtensionFilter("hex", "hex"));
                    if (fc.showOpenDialog(DataEditor.this) == JFileChooser.APPROVE_OPTION) {
                        setFileName(fc.getSelectedFile());
                        try {
                            DataField dataRead = Importer.read(fc.getSelectedFile(), dataBits, bigEndian.isSelected())
                                    .trimValues(addrBits, dataBits);
                            localDataField.setDataFrom(dataRead);
                            dm.fireEvent(new TableModelEvent(dm));
                        } catch (IOException e1) {
                            new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e1).show(DataEditor.this);
                        }
                    }
                }
            }.createJMenuItem());
            data.add(new ToolTipAction(Lang.get("btn_save")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new MyFileChooser();
                    setFileNameTo(fc);
                    fc.setFileFilter(new FileNameExtensionFilter("hex", "hex"));
                    new SaveAsHelper(DataEditor.this, fc, "hex").checkOverwrite(
                            file -> {
                                setFileName(file);
                                localDataField.saveTo(file);
                            }
                    );
                }
            }.createJMenuItem());


            menuBar.add(data);

            setJMenuBar(menuBar);
        }

        new ToolTipAction(Lang.get("menu_paste")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                try {
                    Object data = clpbrd.getData(DataFlavor.stringFlavor);
                    new PasteHandler(data.toString(), table).paste();
                } catch (UnsupportedFlavorException | IOException e1) {
                    new ErrorMessage(Lang.get("msg_errorPastingData")).addCause(e1).show();
                }
            }
        }.setAcceleratorCTRLplus('V').enableAcceleratorIn(table);

        new ToolTipAction(Lang.get("menu_copy")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = table.getSelectedRows();
                if (rows.length > 0) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(((MyTableModel) table.getModel()).toString(rows)), null);
                }
            }
        }.setAcceleratorCTRLplus('C').enableAcceleratorIn(table);

        pack();
        if (getWidth() < 150)
            setSize(new Dimension(150, getHeight()));
        setLocationRelativeTo(parent);
    }

    private int calcCols(int size, int dataBits, ValueFormatter dataFormat) {
        if (size <= 16) return 1;

        int colWidth = dataFormat.strLen(dataBits);
        int cols = 2;
        while (true) {
            int newCols = cols * 2;
            if (colWidth * newCols > 100 || size / newCols < newCols)
                break;
            cols = newCols;
        }
        return cols;
    }

    /**
     * @return the data field
     */
    public DataField getModifiedDataField() {
        localDataField.trim();
        return localDataField;
    }

    /**
     * @return true if data is modified
     */
    public boolean showDialog() {
        setVisible(true);
        return ok;
    }

    /**
     * Shows this dialog and attaches it to the given running model
     *
     * @param label the label of the RAM component
     * @param model the model to use
     */
    public void showDialog(String label, Model model) {
        if (label.length() > 0)
            setTitle(label);
        showDialog();

        if (model != null) {
            model.getWindowPosManager().register("RAM_DATA_" + label, this);
            model.addObserver(event -> {
                if (event.getType().equals(ModelEventType.CLOSED))
                    detachFromRunningModel();
            }, ModelEventType.CLOSED);
        }
    }

    /**
     * Called if dialog shows data from running model and model had stopped.
     */
    public void detachFromRunningModel() {
        table.setForeground(Color.BLUE);
        table.setToolTipText(Lang.get("msg_dataNotUpdatedAnymore"));
        table.setEnabled(false);
    }

    /**
     * Sets the filename to use
     *
     * @param fileName the filename
     */
    public void setFileName(File fileName) {
        if (fileName.exists()) {
            this.fileName = fileName;
            lastUsedFileName = fileName;
        }
    }

    private void setFileNameTo(JFileChooser fc) {
        if (fileName != null)
            fc.setSelectedFile(fileName);
        else if (lastUsedFileName != null)
            fc.setSelectedFile(lastUsedFileName);
    }

    /**
     * Sets the node if this DataEditor edits a DataField used in a running model.
     *
     * @param node the node
     * @return this for chained calls
     */
    public DataEditor setNode(Node node) {
        this.node = node;
        return this;
    }

    private final class MyTableModel implements TableModel, DataField.DataListener {
        private final DataField dataField;
        private final int cols;
        private final SyncAccess modelSync;
        private final int rows;
        private final ArrayList<TableModelListener> listener = new ArrayList<>();

        private MyTableModel(DataField dataField, int cols, int rows, SyncAccess modelSync) {
            this.dataField = dataField;
            this.cols = cols;
            this.rows = rows;
            this.modelSync = modelSync;
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
                return addrFormat.formatToView(new Value(columnIndex - 1, addrBits));
            else
                return Lang.get("key_Value");
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Long.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                return rowIndex * cols;
            else
                return dataField.getDataWord(rowIndex * cols + (columnIndex - 1));
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            long decode = (long) aValue;
            modelSync.modify(() -> {
                int addr = rowIndex * cols + (columnIndex - 1);
                boolean modified = dataField.setData(addr, decode);
                if (modified && node != null)
                    node.hasChanged();
            });
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
            if (addr < 0) {
                // all values have changed!
                fireEvent(new TableModelEvent(this));
            } else {
                // only one value has changed
                fireEvent(new TableModelEvent(this, addr / cols));
            }
        }

        public String toString(int[] rows) {
            StringBuilder sb = new StringBuilder();
            for (int r : rows) {
                int offs = r * cols;
                sb.append(addrFormat.formatToEdit(new Value(offs, addrBits)));
                for (int c = 0; c < cols; c++) {
                    long val = dataField.getDataWord(offs + c);
                    sb.append("\t").append(dataFormat.formatToEdit(new Value(val, dataBits)));
                }
                sb.append(System.lineSeparator());
            }
            return sb.toString();
        }
    }

    private static final class MyRenderer extends DefaultTableCellRenderer {
        private final ValueFormatter intFormat;
        private final int bits;

        private MyRenderer(ValueFormatter intFormat, int bits) {
            this.intFormat = intFormat;
            this.bits = bits;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            long val = 0;
            if (value != null)
                val = ((Number) value).longValue();
            setText(intFormat.formatToView(new Value(val, bits)));
            return this;
        }
    }

    private static final class MyEditor extends DefaultCellEditor {
        private final ValueFormatter intFormat;
        private final int bits;
        private long value;

        private static JTextField createTextField() {
            JTextField tf = new JTextField();
            tf.setHorizontalAlignment(JTextField.RIGHT);
            return tf;
        }

        private MyEditor(ValueFormatter intFormat, int bits) {
            super(createTextField());
            this.intFormat = intFormat;
            this.bits = bits;
        }


        @Override
        public Component getTableCellEditorComponent(JTable jTable, Object o, boolean isSelected, int row, int col) {
            JTextField editor = (JTextField) super.getTableCellEditorComponent(jTable, o, isSelected, row, col);
            editor.setText(intFormat.formatToEdit(new Value((Long) o, bits)));
            return editor;
        }

        @Override
        public boolean stopCellEditing() {
            String s = (String) super.getCellEditorValue();

            try {
                this.value = Bits.decode(s);
            } catch (Exception e) {
                ((JComponent) this.getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }

            return super.stopCellEditing();
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }
    }

    private static final class PasteHandler {
        private final String data;
        private final int yOrigin;
        private final int xOrigin;
        private final MyTableModel model;

        /**
         * Creates a new Paste handler
         *
         * @param data  the datastrin give by the systems clipboard
         * @param table the tabel to insert the data to
         */
        private PasteHandler(String data, JTable table) {
            this.data = data;
            xOrigin = table.getSelectedColumn();
            yOrigin = table.getSelectedRow();
            model = (MyTableModel) table.getModel();
        }

        /**
         * called to handle the paste action
         */
        private void paste() {
            if (xOrigin >= 0 && yOrigin >= 0) {
                StringTokenizer rows = new StringTokenizer(data, "\n\r");
                int y = 0;
                while (rows.hasMoreTokens()) {
                    String line = rows.nextToken();
                    StringTokenizer cols = new StringTokenizer(line, "\t");
                    int x = 0;
                    while (cols.hasMoreTokens()) {
                        String cell = cols.nextToken();
                        setData(xOrigin + x, yOrigin + y, cell.trim());
                        x++;
                    }
                    y++;
                }
                model.fireEvent(new TableModelEvent(model));
            }
        }

        private void setData(int col, int row, String value) {
            if (col < model.getColumnCount() && row < model.getRowCount()) {
                if (model.isCellEditable(row, col)) {
                    Class<?> type = model.getColumnClass(col);
                    if (type == Long.class) {
                        try {
                            model.setValueAt(Bits.decode(value), row, col);
                        } catch (Bits.NumberFormatException e) {
                            // do nothing on error
                        }
                    }
                }
            }
        }
    }

}
