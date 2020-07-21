/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.testing;

import de.neemann.digital.core.ErrorDetector;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.data.Value;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.data.ValueTableModel;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.components.data.GraphDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestExecutor;
import de.neemann.digital.testing.TestingDataException;
import de.neemann.gui.IconCreator;
import de.neemann.gui.LineBreaker;
import de.neemann.gui.MyFileChooser;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Dialog to show the testing results.
 * ToDo: Sync of data access is missing!
 */
public class ValueTableDialog extends JDialog {
    /**
     * Background color for failed tests
     */
    static final Color FAILED_COLOR = new Color(255, 200, 200);
    /**
     * Background color for passed tests
     */
    static final Color PASSED_COLOR = new Color(200, 255, 200);
    private static final Icon ICON_FAILED = IconCreator.create("testFailed.png");
    private static final Icon ICON_PASSED = IconCreator.create("testPassed.png");
    private static final Icon ICON_GRAPH = IconCreator.create("measurement-graph.png");


    private final ArrayList<ValueTable> resultTableData;
    private final JTabbedPane tp;
    private final Window owner;
    private final ToolTipAction asGraph;

    /**
     * Creates a new result dialog.
     *
     * @param owner the parent frame
     * @param title the frame title
     */
    public ValueTableDialog(Window owner, String title) {
        super(owner, title, ModalityType.MODELESS);
        this.owner = owner;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        resultTableData = new ArrayList<>();
        tp = new JTabbedPane();

        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu(Lang.get("menu_file"));
        bar.add(file);
        file.add(new ToolTipAction(Lang.get("menu_saveData")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                int tab = tp.getSelectedIndex();
                if (tab < 0) tab = 0;
                JFileChooser fileChooser = new MyFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Comma Separated Values", "csv"));
                new SaveAsHelper(ValueTableDialog.this, fileChooser, "csv")
                        .checkOverwrite(resultTableData.get(tab)::saveCSV);
            }
        }.setToolTip(Lang.get("menu_saveData_tt")).createJMenuItem());

        JMenu view = new JMenu(Lang.get("menu_view"));
        asGraph = new ToolTipAction(Lang.get("menu_showDataAsGraph"), ICON_GRAPH) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int tab = tp.getSelectedIndex();
                if (tab < 0) tab = 0;
                new GraphDialog(ValueTableDialog.this, Lang.get("win_testdata_N", tp.getTitleAt(tab)), resultTableData.get(tab))
                        .disableTable()
                        .setVisible(true);
            }
        }.setToolTip(Lang.get("menu_showDataAsGraph_tt"));
        view.add(asGraph.createJMenuItem());
        bar.add(view);
        setJMenuBar(bar);

        JToolBar toolBar = new JToolBar();
        toolBar.add(asGraph.createJButtonNoText());
        getContentPane().add(toolBar, BorderLayout.NORTH);

        getContentPane().add(tp);
    }

    /**
     * Add test results
     *
     * @param tsl     list of test sets
     * @param circuit the circuit
     * @param library the library to use
     * @return this for chained calls
     * @throws NodeException            NodeException
     * @throws TestingDataException     DataException
     * @throws PinException             PinException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ValueTableDialog addTestResult(ArrayList<TestSet> tsl, Circuit circuit, ElementLibrary library) throws TestingDataException, ElementNotFoundException, PinException, NodeException {
        Collections.sort(tsl);
        int i = 0;
        int errorTabIndex = -1;
        for (TestSet ts : tsl) {
            Model model = new ModelCreator(circuit, library).createModel(false);
            ErrorDetector errorDetector = new ErrorDetector();
            model.addObserver(errorDetector);
            try {
                TestExecutor testExecutor = new TestExecutor(ts.data).create(model);

                String tabName;
                Icon tabIcon;
                if (testExecutor.allPassed()) {
                    tabName = Lang.get("msg_test_N_Passed", ts.name);
                    tabIcon = ICON_PASSED;
                } else {
                    tabName = Lang.get("msg_test_N_Failed", ts.name);
                    tabIcon = ICON_FAILED;
                    errorTabIndex = i;
                }
                if (testExecutor.toManyResults())
                    tabName += " " + Lang.get("msg_test_missingLines");

                tp.addTab(tabName, tabIcon, new JScrollPane(createTable(testExecutor.getResult())));
                if (testExecutor.toManyResults())
                    tp.setToolTipTextAt(i, new LineBreaker().toHTML().breakLines(Lang.get("msg_test_missingLines_tt")));
                resultTableData.add(testExecutor.getResult());
                i++;
                errorDetector.check();
            } catch (Exception e) {
                throw new TestingDataException(Lang.get("err_whileExecutingTests_N0", ts.name), e);
            } finally {
                model.close();
            }
        }
        if (errorTabIndex >= 0)
            tp.setSelectedIndex(errorTabIndex);

        pack();
        setLocationRelativeTo(owner);
        return this;
    }

    /**
     * Add a table to this dialog
     *
     * @param name       the name of the tab
     * @param valueTable the values
     * @return this for chained calls
     */
    public ValueTableDialog addValueTable(String name, ValueTable valueTable) {
        tp.addTab(name, new JScrollPane(createTable(valueTable)));
        resultTableData.add(valueTable);

        pack();
        setLocationRelativeTo(owner);
        return this;
    }

    private JTable createTable(ValueTable valueTable) {
        JTable table = new JTable(new ValueTableModel(valueTable));
        table.setDefaultRenderer(Value.class, new ValueRenderer());
        table.setDefaultRenderer(Integer.class, new NumberRenderer());
        final Font font = table.getFont();
        table.setRowHeight(font.getSize() * 6 / 5);
        return table;
    }

    /**
     * Disable the show as graph function
     *
     * @return this for chained calls
     */
    public ValueTableDialog disableGraph() {
        asGraph.setEnabled(false);
        return this;
    }

    /**
     * A TestSet contains the {@link TestCaseDescription} and the name of the TestData.
     * Is only a value bean
     */
    public static class TestSet implements Comparable<TestSet> {

        private final TestCaseDescription data;
        private final String name;

        /**
         * Creates a new instance
         *
         * @param data the TestData
         * @param name the name of the data, eg. the used label
         */
        public TestSet(TestCaseDescription data, String name) {
            this.data = data;
            this.name = name;
        }

        @Override
        public int compareTo(TestSet o) {
            return name.compareTo(o.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestSet testSet = (TestSet) o;

            return name.equals(testSet.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    private static class ValueRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Value v = (Value) value;
            if (v != null) {
                comp.setText(v.toString());
                comp.setHorizontalAlignment(JLabel.CENTER);

                switch (((Value) value).getState()) {
                    case NORMAL:
                        comp.setBackground(Color.WHITE);
                        break;
                    case FAIL:
                        comp.setBackground(FAILED_COLOR);
                        break;
                    case PASS:
                        comp.setBackground(PASSED_COLOR);
                        break;
                }
            }
            return comp;
        }
    }

    private static class NumberRenderer extends DefaultTableCellRenderer {
        private static final Color NUM_BACKGROUND_COLOR = new Color(238, 238, 238);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            comp.setBackground(NUM_BACKGROUND_COLOR);
            comp.setHorizontalAlignment(JLabel.RIGHT);
            return comp;
        }
    }

}
