package de.neemann.digital.integration;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.draw.elements.Circuit;
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

    public void testErrorAtStart1() throws Exception {
        new GuiTest("dig/manualError/01_fastRuntime.dig")
                .press(' ')
                .add(new CheckErrorDialog("01_fastRuntime.dig", Lang.get("err_burnError")))
                .add(new CloseTopMost())
                .add(new WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart2() throws Exception {
        new GuiTest("dig/manualError/02_fastRuntimeEmbed.dig")
                .press(' ')
                .add(new CheckErrorDialog("short.dig", Lang.get("err_burnError")))
                .add(new CloseTopMost())
                .add(new WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart3() throws Exception {
        new GuiTest("dig/manualError/06_initPhase.dig")
                .press(' ')
                .add(new CheckErrorDialog("06_initPhase.dig", Lang.get("err_burnError")))
                .add(new CloseTopMost())
                .add(new WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart4() throws Exception {
        new GuiTest("dig/manualError/07_creationPhase.dig")
                .press(' ')
                .add(new CheckErrorDialog("07_creationPhase.dig", "ErrorY"))
                .add(new CloseTopMost())
                .add(new WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart5() throws Exception {
        new GuiTest("dig/manualError/08_twoFastClocks.dig")
                .press(' ')
                .add(new CheckErrorDialog(Lang.get("err_moreThanOneFastClock")))
                .add(new CloseTopMost())
                .add(new WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtTestExecution() throws Exception {
        new GuiTest("dig/manualError/04_testExecution.dig")
                .press("F8")
                .add(new CheckErrorDialog("04_testExecution.dig", Lang.get("err_burnError")))
                .add(new CloseTopMost())
                .add(new WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtRunToBreak() throws Exception {
        new GuiTest("dig/manualError/05_runToBreak.dig")
                .press(' ')
                .delay(500)
                .press("F7")
                .add(new CheckErrorDialog("05_runToBreak.dig", Lang.get("err_burnError")))
                .add(new CloseTopMost())
                .add(new WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtButtonPress() throws Exception {
        new GuiTest("dig/manualError/03_fastRuntimeButton.dig")
                .press(' ')
                .delay(500)
                .press('A')
                .add(new CheckErrorDialog("03_fastRuntimeButton.dig", Lang.get("err_burnError")))
                .add(new CloseTopMost())
                .add(new WindowCheck<>(Main.class))
                .execute();
    }

    public void testAnalysis() throws Exception {
        new GuiTest("dig/manualError/09_analysis.dig")
                .press("F9")
                .add(new WindowCheck<TableDialog>(TableDialog.class) {
                    @Override
                    public void checkWindow(TableDialog td) {
                        ExpressionListenerStore exp = td.getLastGeneratedExpressions();
                        assertEquals(1, exp.getResults().size());
                        Expression res = exp.getResults().get(0).getExpression();
                        assertEquals("and(B,C)", res.toString());
                    }
                })
                .press("F1")
                .add(new WindowCheck<KarnaughMapDialog>(KarnaughMapDialog.class) {
                    @Override
                    public void checkWindow(KarnaughMapDialog kMapDialog) {
                        List<ExpressionListenerStore.Result> res = kMapDialog.getResults();
                        assertEquals(1, res.size());
                        Expression r = res.get(0).getExpression();
                        assertEquals("and(B,C)", r.toString());
                    }
                })
                .add(new CloseTopMost())
                .press("F2")
                .add(new WindowCheck<Main>(Main.class) {
                    @Override
                    public void checkWindow(Main main) {
                        Circuit c = main.getCircuitComponent().getCircuit();
                        assertEquals(4, c.getElements().size());
                    }
                })
                .add(new CloseTopMost())
                .add(new WindowCheck<>(TableDialog.class))
                .execute();
    }

    public static class GuiTest {
        private final ArrayList<Runnable> runnableList;
        private Main main;
        private Robot robot;
        private String filename;

        public GuiTest(String filename) {
            this.filename = filename;
            runnableList = new ArrayList<>();
        }

        public GuiTest add(Runnable runnable) {
            runnableList.add(runnable);
            return this;
        }

        public GuiTest delay(int ms) {
            add(() -> Thread.sleep(ms));
            return this;
        }

        public GuiTest press(String key) {
            return addCode(KeyStroke.getKeyStroke(key).getKeyCode());
        }

        public GuiTest press(char c) {
            return addCode(KeyEvent.getExtendedKeyCodeForChar(c));
        }

        private GuiTest addCode(int code) {
            add(() -> {
                robot.keyPress(code);
                robot.keyRelease(code);
                Thread.sleep(200);
            });
            return this;
        }

        private boolean isDisplay() {
            final boolean isDisplay = !GraphicsEnvironment.isHeadless();
            if (!isDisplay)
                System.err.println("running headless, skip test!");
            return isDisplay;
        }

        public void execute() throws Exception {
            if (isDisplay()) {
                File file = new File(Resources.getRoot(), filename);
                SwingUtilities.invokeAndWait(() -> {
                    main = new Main.MainBuilder().setFileToOpen(file).build();
                    main.setVisible(true);
                });
                Thread.sleep(500);
                try {
                    robot = new Robot();
                    for (Runnable r : runnableList)
                        r.run();
                } finally {
                    SwingUtilities.invokeAndWait(() -> main.dispose());
                }
            }
        }

    }

    interface Runnable {
        void run() throws Exception;
    }

    private static class WindowCheck<W extends Window> implements Runnable {
        private final Class<W> clazz;

        public WindowCheck(Class<W> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void run() throws Exception {
            Thread.sleep(500);
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            assertTrue("wrong dialog on top!", clazz.isAssignableFrom(activeWindow.getClass()));
            checkWindow((W) activeWindow);
        }

        public void checkWindow(W window) {
        };
    }

    public static class CheckErrorDialog extends WindowCheck<ErrorMessage.ErrorDialog> {
        private final String[] expected;

        public CheckErrorDialog(String... expected) {
            super(ErrorMessage.ErrorDialog.class);
            this.expected = expected;
        }

        @Override
        public void checkWindow(ErrorMessage.ErrorDialog errorDialog) {
            String errorMessage = errorDialog.getErrorMessage();
            for (String e : expected)
                assertTrue(errorMessage + " does not contain " + e, errorMessage.contains(e));
        }
    }

    public static class CloseTopMost implements Runnable {
        @Override
        public void run() throws InterruptedException {
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            activeWindow.dispose();
            Thread.sleep(200);
        }
    }
}
