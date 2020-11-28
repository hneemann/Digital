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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Used to edit ROM data fields.
 * Looks like a HEX editor.
 */
public class DataEditor extends JDialog {
    private static final Color MYGRAY = new Color(230, 230, 230);
    private final IntFormat dataFormat;
    private final IntFormat addrFormat;
    private final int dataBits;
    private final int addrBits;
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
     * @param intFormat      the integer format to be used
     */
    public DataEditor(Component parent, DataField dataField, int dataBits, int addrBits, boolean modelIsRunning, SyncAccess modelSync, IntFormat intFormat) {
        super(SwingUtilities.windowForComponent(parent), Lang.get("key_Data"), modelIsRunning ? ModalityType.MODELESS : ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.dataBits = dataBits;
        this.addrBits = addrBits;
        dataFormat = intFormat;
        if (intFormat.equals(IntFormat.ascii) || intFormat.equals(IntFormat.bin))
            addrFormat = IntFormat.def;
        else
            addrFormat = intFormat;

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
        int widthOfZero = table.getFontMetrics(table.getFont()).stringWidth("00000000") / 8;
        int widthOfData = widthOfZero * (dataFormat.strLen(dataBits) + 1);
        for (int c = 1; c < table.getColumnModel().getColumnCount(); c++) {
            tableWidth += widthOfData;
            TableColumn col = table.getColumnModel().getColumn(c);
            col.setPreferredWidth(widthOfData);
        }

        DefaultTableCellRenderer dataRenderer = new DefaultTableCellRenderer();
        dataRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.setDefaultRenderer(NumberString.class, dataRenderer);

        DefaultTableCellRenderer addrRenderer = new DefaultTableCellRenderer();
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
                    if (fileName != null)
                        fc.setSelectedFile(fileName);
                    fc.setFileFilter(new FileNameExtensionFilter("hex", "hex"));
                    if (fc.showOpenDialog(DataEditor.this) == JFileChooser.APPROVE_OPTION) {
                        fileName = fc.getSelectedFile();
                        try {
                            DataField dataRead = Importer.read(fc.getSelectedFile(), dataBits)
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
                    if (fileName != null)
                        fc.setSelectedFile(fileName);
                    fc.setFileFilter(new FileNameExtensionFilter("hex", "hex"));
                    new SaveAsHelper(DataEditor.this, fc, "hex").checkOverwrite(
                            file -> {
                                fileName = fc.getSelectedFile();
                                localDataField.saveTo(file);
                            }
                    );
                }
            }.createJMenuItem());


            menuBar.add(data);


            setJMenuBar(menuBar);
        }

        pack();
        if (getWidth() < 150)
            setSize(new Dimension(150, getHeight()));
        setLocationRelativeTo(parent);
    }

    private int calcCols(int size, int dataBits, IntFormat dataFormat) {
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
        this.fileName = fileName;
    }

    /**
     * @return the file name last used
     */
    public File getFileName() {
        return fileName;
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
                return addrFormat.formatToEdit(new Value(columnIndex - 1, addrBits));
            else
                return Lang.get("key_Value");
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return NumberString.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex > 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                return new NumberString(rowIndex * cols, addrBits, addrFormat);
            else
                return new NumberString(dataField.getDataWord(rowIndex * cols + (columnIndex - 1)), dataBits, dataFormat);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            long decode = ((NumberString) aValue).getVal();
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
    }

    /**
     * Used to represent a number in the table
     */
    public static class NumberString {
        private final String str;
        private final long val;

        private NumberString(long val, int bits, IntFormat format) {
            this.val = val;
            this.str = format.formatToEdit(new Value(val, bits));
        }

        /**
         * Called by JTable to create a new value from an edited string!
         * In an exception is thrown, the cell is marked with a small red border.
         *
         * @param str the string after editing
         * @throws Bits.NumberFormatException Bits.NumberFormatException
         */
        public NumberString(String str) throws Bits.NumberFormatException {
            this.val = Bits.decode(str);
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }

        private long getVal() {
            return val;
        }
    }
}
