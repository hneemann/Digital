package de.neemann.digital.gui.components.test;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

/**
 * Dialog to show the test results.
 *
 * @author hneemann
 */
public class TestResultDialog extends JDialog {
    private static final Color FAILED_COLOR = new Color(255, 200, 200);

    /**
     * Creates a new result dialog.
     *
     * @param owner the parent frame
     * @param tsl   list of test sets
     * @param model the model to test
     * @throws NodeException NodeException
     * @throws DataException DataException
     */
    public TestResultDialog(JFrame owner, ArrayList<TestSet> tsl, Model model) throws NodeException, DataException {
        super(owner, Lang.get("msg_testResult"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTabbedPane tp = new JTabbedPane();
        for (TestSet ts : tsl) {
            TestResult tr = new TestResult(ts.data).create(model);

            JTable table = new JTable(tr);
            table.setDefaultRenderer(MatchedValue.class, new MyRenderer());

            String tabName;
            if (tr.isAllPassed())
                tabName = Lang.get("msg_test_N_Passed", ts.name);
            else
                tabName = Lang.get("msg_test_N_Failed", ts.name);
            tp.addTab(tabName, new JScrollPane(table));
        }

        getContentPane().add(tp);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * A TestSet contains the {@link TestData} and the name of the TestData.
     * Is only a value bean
     */
    public static class TestSet {

        private final TestData data;
        private final String name;

        /**
         * Creates a new instance
         *
         * @param data the TestData
         * @param name the name of the data, eg. the used label
         */
        public TestSet(TestData data, String name) {
            this.data = data;
            this.name = name;
        }
    }

    private class MyRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            MatchedValue v = (MatchedValue) value;

            comp.setText(v.toString());
            comp.setHorizontalAlignment(JLabel.CENTER);

            if (v.isPassed())
                comp.setBackground(Color.WHITE);
            else
                comp.setBackground(FAILED_COLOR);

            return comp;
        }
    }
}
