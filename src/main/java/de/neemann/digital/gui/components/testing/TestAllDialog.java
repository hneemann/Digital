/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestExecutor;
import de.neemann.digital.testing.TestingDataException;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Tests all the files in a given folder
 */
public class TestAllDialog extends JDialog {

    /**
     * Creates a new dialog
     *
     * @param frame        the parent frame
     * @param folder       the folder to scan
     * @param shapeFactory the shape factory to use
     * @param library      the element library
     */
    public TestAllDialog(Frame frame, File folder, ShapeFactory shapeFactory, ElementLibrary library) {
        super(frame, Lang.get("msg_testResult"), false);
        ArrayList<FileToTest> files = new ArrayList<>();
        scan(folder.getPath().length() + 1, folder, files);

        final FileModel tableModel = new FileModel(files);
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(1).setCellRenderer(new StateRenderer(files));
        getContentPane().add(new JScrollPane(table));
        pack();
        setLocationRelativeTo(frame);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        File f = files.get(row).file;
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

        Thread t = new Thread(new TestRunner(files, tableModel, shapeFactory, library));
        t.setDaemon(true);
        t.start();
    }

    private void scan(int rootLength, File folder, ArrayList<FileToTest> files) {
        File[] fileList = folder.listFiles();
        if (fileList != null) {
            Arrays.sort(fileList, Comparator.comparing(f -> f.getPath().toLowerCase()));
            for (File f : fileList)
                if (f.isDirectory())
                    scan(rootLength, f, files);
                else if (f.isFile() && f.getName().endsWith(".dig"))
                    files.add(new FileToTest(rootLength, f));
        }
    }

    private static final class FileToTest {
        private enum State {unknown, passed, error, failed}

        private final File file;
        private final String name;
        private String message = "-";
        private State state = State.unknown;

        private FileToTest(int rootLength, File file) {
            this.file = file;
            name = file.getPath().substring(rootLength);
        }

        public String getName() {
            return name;
        }

        private void setMessage(String message, State state) {
            this.message = message;
            this.state = state;
        }
    }

    private final static class FileModel implements TableModel {
        private final ArrayList<FileToTest> files;
        private ArrayList<TableModelListener> listener;

        private FileModel(ArrayList<FileToTest> files) {
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
            FileToTest file = files.get(row);
            switch (col) {
                case 0:
                    return file.getName();
                default:
                    return file.message;
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


    private static final class TestRunner implements Runnable {
        private final ArrayList<FileToTest> files;
        private final FileModel tableModel;
        private final ShapeFactory shapeFactory;
        private final ElementLibrary library;

        private TestRunner(ArrayList<FileToTest> files, FileModel tableModel, ShapeFactory shapeFactory, ElementLibrary library) {
            this.files = files;
            this.tableModel = tableModel;
            this.shapeFactory = shapeFactory;
            this.library = library;
        }

        @Override
        public void run() {
            for (int i = 0; i < files.size(); i++) {
                FileToTest f = files.get(i);
                try {
                    Circuit circuit = Circuit.loadCircuit(f.file, shapeFactory);
                    ArrayList<TestCase> testCases = new ArrayList<>();
                    for (VisualElement el : circuit.getElements()) {
                        if (el.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION)) {
                            String label = el.getElementAttributes().getCleanLabel();
                            TestCaseDescription testData = el.getElementAttributes().get(TestCaseElement.TESTDATA);
                            testCases.add(new TestCase(label, testData));
                        }
                    }
                    if (testCases.isEmpty())
                        setMessage(f, i, Lang.get("err_noTestData"), FileToTest.State.unknown);
                    else {
                        Model model = new ModelCreator(circuit, library).createModel(false);
                        StringBuilder sb = new StringBuilder();
                        int rowCount = 0;
                        for (TestCase tc : testCases) {
                            try {
                                TestExecutor te = new TestExecutor(tc.testData).create(model);
                                if (te.allPassed()) {
                                    rowCount += te.getResult().getRows();
                                } else {
                                    if (sb.length() > 0)
                                        sb.append("; ");
                                    sb.append(Lang.get("msg_test_N_Failed", tc.label));
                                }
                            } catch (TestingDataException | NodeException e) {
                                if (sb.length() > 0)
                                    sb.append("; ");
                                sb.append(tc.label).append(": ").append(e.getMessage());
                            }
                        }
                        if (sb.length() == 0)
                            setMessage(f, i, Lang.get("msg_testPassed_N", rowCount), FileToTest.State.passed);
                        else
                            setMessage(f, i, sb.toString(), FileToTest.State.failed);
                    }

                } catch (IOException | NodeException | ElementNotFoundException | PinException e) {
                    setMessage(f, i, e.getMessage(), FileToTest.State.error);
                }
            }
        }

        private void setMessage(FileToTest f, int i, String message, FileToTest.State state) {
            SwingUtilities.invokeLater(() -> {
                f.setMessage(message, state);
                tableModel.messageChanged(i);
            });
        }

        private final class TestCase {
            private final String label;
            private final TestCaseDescription testData;

            private TestCase(String label, TestCaseDescription testData) {
                this.label = label;
                this.testData = testData;
            }
        }
    }


    private static final class StateRenderer extends DefaultTableCellRenderer {
        private final ArrayList<FileToTest> files;

        private StateRenderer(ArrayList<FileToTest> files) {
            this.files = files;
        }

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int row, int i1) {
            final Component tc = super.getTableCellRendererComponent(jTable, o, b, b1, row, i1);

            FileToTest f = files.get(row);
            switch (f.state) {
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

            return tc;
        }
    }
}
