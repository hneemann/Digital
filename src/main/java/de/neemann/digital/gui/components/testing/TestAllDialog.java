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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * Tests all the files in a given folder
 */
public class TestAllDialog extends JDialog {

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
        FolderTestRunner folderTestRunner = new FolderTestRunner(folder);

        final FileModel tableModel = new FileModel(folderTestRunner.getFiles());
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setCellRenderer(new StateRenderer());
        getContentPane().add(new JScrollPane(table));
        pack();
        setLocationRelativeTo(frame);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
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
            }
        });

        folderTestRunner.startTests(
                (f, row) -> SwingUtilities.invokeLater(() -> tableModel.messageChanged(row)),
                shapeFactory,
                library);
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
        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int row, int i1) {
            final JLabel tc = (JLabel) super.getTableCellRendererComponent(jTable, o, b, b1, row, i1);

            FolderTestRunner.FileToTest f = (FolderTestRunner.FileToTest) o;
            switch (f.getStatus()) {
                case error:
                    tc.setBackground(Color.LIGHT_GRAY);
                    break;
                case unknown:
                    tc.setBackground(Color.WHITE);
                    break;
                case passed:
                    tc.setBackground(ValueTableDialog.PASSED_COLOR);
                    break;
                case failed:
                    tc.setBackground(ValueTableDialog.FAILED_COLOR);
                    break;
            }
            tc.setText(f.getMessage());

            return tc;
        }
    }
}
