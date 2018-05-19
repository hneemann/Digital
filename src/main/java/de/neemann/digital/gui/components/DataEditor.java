/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.memory.DataField;
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
    private DataField localDataField;
    private final JTable table;
    private boolean ok = false;
    private File fileName;

    /**
     * Creates a new instance
     *
     * @param parent         the parent
     * @param dataField      the data to edit
     * @param size           the size of the data field to edit
     * @param dataBits       the bit count of the values to edit
     * @param addrBits       the bit count of the adresses
     * @param modelIsRunning true if model is running
     * @param modelSync      used to access the running model
     */
    public DataEditor(Component parent, DataField dataField, int size, int dataBits, int addrBits, boolean modelIsRunning, SyncAccess modelSync) {
        super(SwingUtilities.windowForComponent(parent), Lang.get("key_Data"), modelIsRunning ? ModalityType.MODELESS : ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        if (modelIsRunning)
            localDataField = dataField;
        else
            localDataField = new DataField(dataField, size);

        final int cols = calcCols(size, dataBits);

        int tableWidth = 0;
        MyTableModel dm = new MyTableModel(this.localDataField, cols, modelSync);
        table = new JTable(dm);
        int widthOfZero = table.getFontMetrics(table.getFont()).stringWidth("00000000") / 8;
        table.setDefaultRenderer(MyLong.class, new MyLongRenderer(dataBits));
        for (int c = 1; c < table.getColumnModel().getColumnCount(); c++) {
            int width = widthOfZero * ((dataBits - 1) / 4 + 2);
            tableWidth += width;
            TableColumn col = table.getColumnModel().getColumn(c);
            col.setPreferredWidth(width);
        }

        MyLongRenderer addrRenderer = new MyLongRenderer(addrBits);
        addrRenderer.setBackground(MYGRAY);
        TableColumn addrColumn = table.getColumnModel().getColumn(0);
        addrColumn.setCellRenderer(addrRenderer);
        int width = widthOfZero * ((addrBits - 1) / 4 + 2);
        addrColumn.setPreferredWidth(width);
        tableWidth += width;


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
                            localDataField.setDataFrom(new DataField(fc.getSelectedFile()));
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

    private int calcCols(int size, int dataBits) {
        int cols = 16;
        if (size <= 16) cols = 1;
        else if (size <= 128) cols = 8;

        if (dataBits > 20 && cols == 16) cols = 8;
        return cols;
    }

    /**
     * @return the data field
     */
    public DataField getModifiedDataField() {
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
                if (event.equals(ModelEvent.STOPPED))
                    detachFromRunningModel();
            }, ModelEvent.STOPPED);
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

    private final static class MyTableModel implements TableModel, DataField.DataListener {
        private final DataField dataField;
        private final int cols;
        private final SyncAccess modelSync;
        private final int rows;
        private ArrayList<TableModelListener> listener = new ArrayList<>();

        private MyTableModel(DataField dataField, int cols, SyncAccess modelSync) {
            this.dataField = dataField;
            this.cols = cols;
            this.modelSync = modelSync;
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
                return Lang.get("key_Value");
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
            return new MyLong(dataField.getDataWord(rowIndex * cols + (columnIndex - 1)));
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            modelSync.access(() -> {
                dataField.setData(rowIndex * cols + (columnIndex - 1), ((MyLong) aValue).getValue());
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


    private final static class MyLongRenderer extends DefaultTableCellRenderer {

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

    /**
     * Used to store a long is used by the table
     */
    public static class MyLong {
        private final long data;

        /**
         * Is called by the JTable to create a new instance if field was edited
         *
         * @param value the edited value
         * @throws Bits.NumberFormatException Bits.NumberFormatException
         */
        public MyLong(String value) throws Bits.NumberFormatException {
            data = Bits.decode(value);
        }

        /**
         * Creates a new instance
         *
         * @param data the value to store
         */
        public MyLong(long data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "0x" + Long.toHexString(data).toUpperCase();
        }

        /**
         * @return the stored value
         */
        public long getValue() {
            return data;
        }
    }

}
