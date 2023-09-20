/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.testing;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.FolderTestRunner;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Tests all the files in a given folder
 */
public class TestAllDialog extends JDialog {

    private final FolderTestRunner folderTestRunner;

    /**
     * Creates a new dialog and starts the test execution.
     *
     * @param frame        the parent frame
     * @param folder       the folder to scan
     * @param shapeFactory the shape factory to use
     * @param library      the element library
     */
    public TestAllDialog(Frame frame, File folder, ShapeFactory shapeFactory, ElementLibrary library) {
        super(frame, Lang.get("msg_testResult"), false);
        folderTestRunner = new FolderTestRunner(folder);

        final FileModel tableModel = new FileModel(folderTestRunner.getFiles());
        JTable table = new JTable(tableModel);
        table.setRowSelectionAllowed(true);
        TableRowSorter<FileModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(1, (Comparator<FolderTestRunner.FileToTest>) (f1, f2) -> -Integer.compare(f1.getStatus().ordinal(), f2.getStatus().ordinal()));
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(1).setCellRenderer(new StateRenderer());
        getContentPane().add(new JScrollPane(table));
        pack();
        setLocationRelativeTo(frame);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2)
                    openCircuit(table, frame, library);
            }
        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                if (keyEvent.getKeyChar() == ' ')
                    openCircuit(table, frame, library);
            }
        });

        folderTestRunner.startTests(
                (f, row) -> SwingUtilities.invokeLater(() -> tableModel.messageChanged(row)),
                shapeFactory,
                library);
    }

    private void openCircuit(JTable table, Frame frame, ElementLibrary library) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            File f = folderTestRunner.getFiles().get(row).getFile();
            new Main.MainBuilder()
                    .setParent(frame)
                    .setFileToOpen(f)
                    .setLibrary(library)
                    .keepPrefMainFile()
                    .openLater();
        }
    }

    /**
     * @return the used folder test runner.
     */
    public FolderTestRunner getFolderTestRunner() {
        return folderTestRunner;
    }

    private final static class FileModel implements TableModel {
        private final ArrayList<FolderTestRunner.FileToTest> files;
        private ArrayList<TableModelListener> listener;

        private FileModel(ArrayList<FolderTestRunner.FileToTest> files) {
            this.files = files;
            listener = new ArrayList<>();
        }

        @Override
        public int getRowCount() {
            return files.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return Lang.get("msg_testFile");
                default:
                    return Lang.get("msg_testResult");
            }
        }

        @Override
        public Class<?> getColumnClass(int i) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int i, int i1) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            FolderTestRunner.FileToTest file = files.get(row);
            switch (col) {
                case 0:
                    return file.getName();
                default:
                    return file;
            }
        }

        @Override
        public void setValueAt(Object o, int i, int i1) {
        }

        @Override
        public void addTableModelListener(TableModelListener tableModelListener) {
            listener.add(tableModelListener);
        }

        @Override
        public void removeTableModelListener(TableModelListener tableModelListener) {
            listener.remove(tableModelListener);
        }

        private void messageChanged(int row) {
            TableModelEvent te = new TableModelEvent(this, row, row, 1);
            for (TableModelListener l : listener)
                l.tableChanged(te);
        }
    }

    private static final class StateRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean sel, boolean b1, int row, int i1) {
            final JLabel tc = (JLabel) super.getTableCellRendererComponent(jTable, o, sel, b1, row, i1);

            FolderTestRunner.FileToTest f = (FolderTestRunner.FileToTest) o;
            if (f != null) {
                Color color;
                switch (f.getStatus()) {
                    case error:
                        color = Color.LIGHT_GRAY;
                        break;
                    case passed:
                        color = ValueTableDialog.PASSED_COLOR;
                        break;
                    case failed:
                        color = ValueTableDialog.FAILED_COLOR;
                        break;
                    default:
                        color = Color.WHITE;
                        break;
                }
                if (sel)
                    color = color.darker();
                tc.setBackground(color);
                tc.setText(f.getMessage());
            }

            return tc;
        }
    }
}
