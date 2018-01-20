package de.neemann.digital.integration;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.karnaugh.KarnaughMapDialog;
import de.neemann.digital.gui.components.table.AllSolutionsDialog;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import junit.framework.TestCase;

import java.util.List;

/**
 * These tests are excluded from the maven build because gui tests are sometimes fragile.
 * They may not behave as expected on all systems.
 * Run this tests directly from your IDE.
 */
public class TestInGUI extends TestCase {

    public void testErrorAtStart1() {
        new GuiTester("dig/manualError/01_fastRuntime.dig")
                .press("SPACE")
                .add(new CheckErrorDialog("01_fastRuntime.dig", Lang.get("err_burnError")))
                .add(new GuiTester.CloseTopMost())
                .ask("Is the driver output colored red?")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart2() {
        new GuiTester("dig/manualError/02_fastRuntimeEmbed.dig")
                .press("SPACE")
                .add(new CheckErrorDialog("short.dig", Lang.get("err_burnError")))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart3() {
        new GuiTester("dig/manualError/06_initPhase.dig")
                .press("SPACE")
                .add(new CheckErrorDialog("06_initPhase.dig", Lang.get("err_burnError")))
                .add(new GuiTester.CloseTopMost())
                .ask("Is the driver output colored red?")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart4() {
        new GuiTester("dig/manualError/07_creationPhase.dig")
                .press("SPACE")
                .add(new CheckErrorDialog("07_creationPhase.dig", "ErrorY"))
                .add(new GuiTester.CloseTopMost())
                .ask("Is the output circled red?")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart5() {
        new GuiTester("dig/manualError/08_twoFastClocks.dig")
                .press("SPACE")
                .add(new CheckErrorDialog(Lang.get("err_moreThanOneFastClock")))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtTestExecution() {
        new GuiTester("dig/manualError/04_testExecution.dig")
                .press("F8")
                .add(new CheckErrorDialog("04_testExecution.dig", Lang.get("err_burnError")))
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testErrorAtRunToBreak() {
        new GuiTester("dig/manualError/05_runToBreak.dig")
                .press("SPACE")
                .delay(500)
                .press("F7")
                .add(new CheckErrorDialog("05_runToBreak.dig", Lang.get("err_burnError")))
                .add(new GuiTester.CloseTopMost())
                .ask("Is the driver output colored red?")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtButtonPress() {
        new GuiTester("dig/manualError/03_fastRuntimeButton.dig")
                .press("SPACE")
                .delay(500)
                .press('a')
                .add(new CheckErrorDialog("03_fastRuntimeButton.dig", Lang.get("err_burnError")))
                .add(new GuiTester.CloseTopMost())
                .ask("Is the driver output colored red?")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testAnalysis() {
        new GuiTester("dig/manualError/09_analysis.dig")
                .press("F9")
                .delay(500)
                .add(new TableDialogCheck("and(B,C)"))
                .press("F1")
                .delay(500)
                .add(new GuiTester.WindowCheck<KarnaughMapDialog>(KarnaughMapDialog.class) {
                    @Override
                    public void checkWindow(KarnaughMapDialog kMapDialog) {
                        List<ExpressionListenerStore.Result> res = kMapDialog.getResults();
                        assertEquals(1, res.size());
                        Expression r = res.get(0).getExpression();
                        assertEquals("and(B,C)", r.toString());
                    }
                })
                .add(new GuiTester.CloseTopMost())
                .press("F2")
                .add(new GuiTester.WindowCheck<Main>(Main.class) {
                    @Override
                    public void checkWindow(Main main) {
                        Circuit c = main.getCircuitComponent().getCircuit();
                        assertEquals(4, c.getElements().size());
                    }
                })
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.WindowCheck<>(TableDialog.class))
                .execute();
    }

    public void testExpression() {
        new GuiTester()
                .press("F10")
                .press("RIGHT", 4)
                .press("DOWN", 3)
                .press("ENTER")
                .press("control typed a")
                .type("a b + b c")
                .press("TAB", 2)
                .press("SPACE")
                .delay(500)
                .add(new GuiTester.WindowCheck<Main>(Main.class) {
                    @Override
                    public void checkWindow(Main main) {
                        Circuit c = main.getCircuitComponent().getCircuit();
                        assertEquals(7, c.getElements().size());
                    }
                })
                .press("F9")
                .delay(500)
                .add(new TableDialogCheck("or(and(a,b),and(b,c))"))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    private GuiTester createNew4VarTruthTable = new GuiTester()
            .press("F10")
            .press("RIGHT", 4)
            .press("DOWN", 2)
            .press("ENTER")
            .delay(500)
            .press("F10")
            .press("RIGHT", 1)
            .press("DOWN", 1)
            .press("RIGHT", 1)
            .press("DOWN", 2)
            .press("ENTER")
            .press("DOWN")
            .press("RIGHT", 4);


    public void testParity() {
        new GuiTester()
                .use(createNew4VarTruthTable)
                .add(new EnterTruthTable(0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1))
                .press("F1")
                .delay(500)
                .ask("Shows the k-map a checkerboard pattern?")
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testEdges() {
        new GuiTester()
                .use(createNew4VarTruthTable)
                .add(new EnterTruthTable(0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1))
                .press("F1")
                .delay(500)
                .ask("Are the edges covered in the k-map?")
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testTwoOutOfThree() {
        new GuiTester()
                .press("F10")
                .press("RIGHT", 4)
                .press("DOWN", 2)
                .press("ENTER")
                .delay(500)
                .press("DOWN")
                .press("RIGHT", 3)
                .add(new EnterTruthTable(0, 0, 0, 1, 0, 1, 1, 1))
                .press("F1")
                .delay(500)
                .ask("Shows the k-map a 'two out of three' pattern?")
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    private GuiTester create4BitCounterTruthTable = new GuiTester()
            .press("F10")
            .press("RIGHT", 4)
            .press("DOWN", 2)
            .press("ENTER")
            .delay(500)
            .press("F10")
            .press("RIGHT", 1)
            .press("DOWN", 2)
            .press("RIGHT", 1)
            .press("DOWN", 2)
            .press("ENTER")
            .delay(500)
            .add(new GuiTester.WindowCheck<AllSolutionsDialog>(AllSolutionsDialog.class){
                @Override
                public void checkWindow(AllSolutionsDialog asd) {
                    asd.getParent().requestFocus();
                }
            })
            .delay(500);

    public void testCounterJK() {
        new GuiTester()
                .use(create4BitCounterTruthTable)
                .press("F10")
                .press("RIGHT", 4)
                .press("DOWN", 2)
                .press("ENTER")
                .delay(500)
                .press("SPACE")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(Main.class))
                .ask("Does the 4 bit counter run correctly?")
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testCounterD() {
        new GuiTester()
                .use(create4BitCounterTruthTable)
                .press("F2")
                .press("SPACE")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(Main.class))
                .ask("Does the 4 bit counter run correctly?")
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testHardware() {
        new GuiTester("dig/manualError/10_hardware.dig")
                .press("F9")
                .delay(500)
                .press("F10")
                .press("RIGHT", 4)
                .press("DOWN", 5)
                .press("RIGHT")
                .press("DOWN", 2)
                .press("RIGHT")
                .press("DOWN")
                .press("RIGHT", 2)
                .press("DOWN")
                .press("ENTER")
                .press("control typed a")
                .typeTempFile("test")
                .press("ENTER")
                .delay(3000)
                .add(new GuiTester.CheckDialogText("Design fits successfully"))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public static class CheckErrorDialog extends GuiTester.WindowCheck<ErrorMessage.ErrorDialog> {
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

    private static class TableDialogCheck extends GuiTester.WindowCheck<TableDialog> {
        private final String expected;

        public TableDialogCheck(String expected) {
            super(TableDialog.class);
            this.expected = expected;
        }

        @Override
        public void checkWindow(TableDialog td) {
            ExpressionListenerStore exp = td.getLastGeneratedExpressions();
            assertEquals(1, exp.getResults().size());
            Expression res = exp.getResults().get(0).getExpression();
            assertEquals(expected, res.toString());
        }
    }

    private class EnterTruthTable implements GuiTester.Runnable {
        private final int[] values;

        public EnterTruthTable(int... values) {
            this.values = values;
        }

        @Override
        public void run(GuiTester guiTester) throws Exception {
            for (int v : values) {
                if (v == 1) {
                    guiTester.typeNow("typed 1");
                    Thread.sleep(400);
                } else
                    guiTester.typeNow("DOWN");
            }
        }
    }
}
