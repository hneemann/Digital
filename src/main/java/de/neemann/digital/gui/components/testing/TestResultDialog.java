package de.neemann.digital.gui.components.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.*;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.IconCreator;
import de.neemann.gui.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Dialog to show the testing results.
 *
 * @author hneemann
 */
public class TestResultDialog extends JDialog {
    private static final Color FAILED_COLOR = new Color(255, 200, 200);
    private static final Color PASSED_COLOR = new Color(200, 255, 200);
    private static final Icon ICON_FAILED = IconCreator.create("testFailed.png");
    private static final Icon ICON_PASSED = IconCreator.create("testPassed.png");

    /**
     * Creates a new result dialog.
     *
     * @param owner   the parent frame
     * @param tsl     list of test sets
     * @param circuit the circuit
     * @param library the library to use
     * @throws NodeException            NodeException
     * @throws TestingDataException     DataException
     * @throws PinException             PinException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public TestResultDialog(JFrame owner, ArrayList<TestSet> tsl, Circuit circuit, ElementLibrary library) throws NodeException, TestingDataException, PinException, ElementNotFoundException {
        super(owner, Lang.get("msg_testResult"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Collections.sort(tsl);

        JTabbedPane tp = new JTabbedPane();
        int i = 0;
        int errorTabIndex = -1;
        for (TestSet ts : tsl) {
            Model model = new ModelCreator(circuit, library).createModel(false);

            TestResult tr = new TestResult(ts.data).create(model);

            if (tr.getException() != null)
                SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorWhileExecutingTests_N0", ts.name)).addCause(tr.getException()).setComponent(this));

            JTable table = new JTable(new TestResultModel(tr));
            table.setDefaultRenderer(MatchedValue.class, new MatchedValueRenderer());
            table.setDefaultRenderer(Integer.class, new NumberRenderer());
            table.getColumnModel().getColumn(0).setMaxWidth(40);

            String tabName;
            Icon tabIcon;
            if (tr.allPassed()) {
                tabName = Lang.get("msg_test_N_Passed", ts.name);
                tabIcon = ICON_PASSED;
            } else {
                tabName = Lang.get("msg_test_N_Failed", ts.name);
                tabIcon = ICON_FAILED;
                errorTabIndex = i;
            }
            if (tr.toManyResults())
                tabName += " " + Lang.get("msg_test_missingLines");

            tp.addTab(tabName, tabIcon, new JScrollPane(table));
            if (tr.toManyResults())
                tp.setToolTipTextAt(i, StringUtils.textToHTML(Lang.get("msg_test_missingLines_tt")));
            i++;
        }
        if (errorTabIndex >= 0)
            tp.setSelectedIndex(errorTabIndex);

        getContentPane().add(tp);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * A TestSet contains the {@link TestData} and the name of the TestData.
     * Is only a value bean
     */
    public static class TestSet implements Comparable<TestSet> {

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

    private static class MatchedValueRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Value v = (Value) value;
            if (v != null) {
                comp.setText(v.toString());
                comp.setHorizontalAlignment(JLabel.CENTER);

                if (v instanceof MatchedValue) {
                    if (((MatchedValue) v).isPassed())
                        comp.setBackground(PASSED_COLOR);
                    else
                        comp.setBackground(FAILED_COLOR);
                } else
                    comp.setBackground(Color.WHITE);
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
