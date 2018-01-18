package de.neemann.digital.integration;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.karnaugh.KarnaughMapDialog;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestInGUI extends TestCase {

    private Main main;

    public void testErrorAtStart1() throws Exception {
        check("dig/manualError/01_fastRuntime.dig",
                new TestSteps()
                        .add(' ')
                        .add(new CheckErrorMessage("01_fastRuntime.dig", Lang.get("err_burnError")))
        );
    }

    public void testErrorAtStart2() throws Exception {
        check("dig/manualError/02_fastRuntimeEmbed.dig",
                new TestSteps()
                        .add(' ')
                        .add(new CheckErrorMessage("short.dig", Lang.get("err_burnError")))
        );
    }

    public void testErrorAtStart3() throws Exception {
        check("dig/manualError/06_initPhase.dig",
                new TestSteps()
                        .add(' ')
                        .add(new CheckErrorMessage("06_initPhase.dig", Lang.get("err_burnError")))
        );
    }

    public void testErrorAtStart4() throws Exception {
        check("dig/manualError/07_creationPhase.dig",
                new TestSteps()
                        .add(' ')
                        .add(new CheckErrorMessage("07_creationPhase.dig", "ErrorY"))
        );
    }

    public void testErrorAtStart5() throws Exception {
        check("dig/manualError/08_twoFastClocks.dig",
                new TestSteps()
                        .add(' ')
                        .add(new CheckErrorMessage(Lang.get("err_moreThanOneFastClock")))
        );
    }

    public void testErrorAtTestExecution() throws Exception {
        check("dig/manualError/04_testExecution.dig",
                new TestSteps()
                        .add("F8")
                        .add(new CheckErrorMessage("04_testExecution.dig", Lang.get("err_burnError")))
        );
    }

    public void testErrorAtRunToBreak() throws Exception {
        check("dig/manualError/05_runToBreak.dig",
                new TestSteps()
                        .add(' ')
                        .delay(500)
                        .add("F7")
                        .add(new CheckErrorMessage("05_runToBreak.dig", Lang.get("err_burnError")))
        );
    }

    public void testErrorAtButtonPress() throws Exception {
        check("dig/manualError/03_fastRuntimeButton.dig",
                new TestSteps()
                        .add(' ')
                        .delay(500)
                        .add("A")
                        .add(new CheckErrorMessage("03_fastRuntimeButton.dig", Lang.get("err_burnError")))
        );
    }

    public void testAnalysis() throws Exception {
        check("dig/manualError/09_analysis.dig",
                new TestSteps()
                        .add("F9")
                        .add(new DialogCheck<TableDialog>(TableDialog.class) {
                            @Override
                            public void checkDialog(TableDialog td) {
                                ExpressionListenerStore exp = td.getLastGeneratedExpressions();
                                assertEquals(1, exp.getResults().size());
                                Expression res = exp.getResults().get(0).getExpression();
                                assertEquals("and(B,C)", res.toString());
                            }
                        })
                        .add("F1")
                        .add(new DialogCheck<KarnaughMapDialog>(KarnaughMapDialog.class) {
                            @Override
                            public void checkDialog(KarnaughMapDialog dialog) throws Exception {
                                List<ExpressionListenerStore.Result> res = dialog.getResults();
                                assertEquals(1, res.size());
                                Expression r = res.get(0).getExpression();
                                assertEquals("and(B,C)", r.toString());
                            }
                        })
        );
    }

    private boolean isDisplay() {
        final boolean isDisplay = !GraphicsEnvironment.isHeadless();
        if (!isDisplay)
            System.err.println("runs headless, skip test!");
        return isDisplay;
    }

    private void check(String filename, TestSteps testSteps) throws Exception {
        if (isDisplay()) {
            File file = new File(Resources.getRoot(), filename);
            SwingUtilities.invokeAndWait(() -> {
                main = new Main.MainBuilder().setFileToOpen(file).build();
                main.setVisible(true);
            });
            Thread.sleep(500);
            try {
                testSteps.execute();
            } finally {
                SwingUtilities.invokeAndWait(() -> main.dispose());
            }
        }
    }

    public static class TestSteps {
        private final ArrayList<Runnable> runnables;
        private Robot robot;

        public TestSteps() {
            runnables = new ArrayList<>();
        }

        public void execute() throws Exception {
            robot = new Robot();
            for (Runnable r : runnables)
                r.run();
        }

        public TestSteps add(Runnable runnable) {
            runnables.add(runnable);
            return this;
        }

        public TestSteps delay(int ms) {
            add(() -> Thread.sleep(ms));
            return this;
        }

        public TestSteps add(String key) {
            return addCode(KeyStroke.getKeyStroke(key).getKeyCode());
        }

        public TestSteps add(char c) {
            return addCode(KeyEvent.getExtendedKeyCodeForChar(c));
        }

        private TestSteps addCode(int code) {
            add(() -> {
                robot.keyPress(code);
                robot.keyRelease(code);
                Thread.sleep(200);
            });
            return this;
        }

    }

    interface Runnable {
        void run() throws Exception;
    }

    private abstract class DialogCheck<D extends JDialog> implements Runnable {
        private final Class<D> clazz;

        public DialogCheck(Class<D> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void run() throws Exception {
            Thread.sleep(500);
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            assertEquals("wrong dialog on top!", clazz, activeWindow.getClass());
            checkDialog((D) activeWindow);
        }

        public abstract void checkDialog(D dialog) throws Exception;
    }

    public class CheckErrorMessage extends DialogCheck<ErrorMessage.ErrorDialog> {
        private final String[] expected;

        public CheckErrorMessage(String... expected) {
            super(ErrorMessage.ErrorDialog.class);
            this.expected = expected;
        }

        @Override
        public void checkDialog(ErrorMessage.ErrorDialog dialog) {
            String errorMessage = dialog.getErrorMessage();
            dialog.dispose();
            for (String e : expected)
                assertTrue(errorMessage + " does not contain " + e, errorMessage.contains(e));
        }
    }

}
