package de.neemann.digital.gui.components.test;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class TestResultDialog extends JDialog {

    public TestResultDialog(JFrame owner, ArrayList<TestSet> tsl, Model model) throws NodeException, DataException {
        super(owner, Lang.get("msg_testResult"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTabbedPane tp = new JTabbedPane();
        for (TestSet ts : tsl) {
            TestResult tr = new TestResult(ts.data).create(model);
            String tabName;
            if (tr.isAllPassed())
                tabName = Lang.get("msg_test_N_Passed", ts.name);
            else
                tabName = Lang.get("msg_test_N_Failed", ts.name);
            tp.addTab(tabName, new JScrollPane(new JTable(tr)));
        }

        getContentPane().add(tp);
        pack();
        setLocationRelativeTo(owner);
    }

    public static class TestSet {

        private final TestData data;
        private final String name;

        public TestSet(TestData data, String name) {
            this.data = data;
            this.name = name;
        }
    }
}
