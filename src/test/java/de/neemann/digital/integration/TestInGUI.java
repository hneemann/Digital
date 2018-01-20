package de.neemann.digital.integration;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.karnaugh.KarnaughMapDialog;
import de.neemann.digital.gui.components.table.AllSolutionsDialog;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.gui.components.testing.ValueTableDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

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
                .add(new GuiTester.ColorPicker(695, 626, (c) -> assertEquals(Style.ERROR.getColor(), c)))
//                .ask("Is the driver output colored red?")
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
                .add(new GuiTester.ColorPicker(871,450, (c) -> assertEquals(Style.ERROR.getColor(), c)))
//                .ask("Is the driver output colored red?")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart4() {
        new GuiTester("dig/manualError/07_creationPhase.dig")
                .press("SPACE")
                .add(new CheckErrorDialog("07_creationPhase.dig", "ErrorY"))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.ColorPicker(597, 459, (c) -> assertEquals(Style.ERROR.getColor(), c)))
//                .ask("Is the output circled red?")
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
                .add(new GuiTester.ColorPicker(873, 546, (c) -> assertEquals(Style.ERROR.getColor(), c)))
//                .ask("Is the driver output colored red?")
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
                .add(new GuiTester.ColorPicker(821, 585, (c) -> assertEquals(Style.ERROR.getColor(), c)))
//                .ask("Is the driver output colored red?")
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
                .add(new GuiTester.WindowCheck<>(KarnaughMapDialog.class, (guiTester, kMapDialog) -> {
                    List<ExpressionListenerStore.Result> res = kMapDialog.getResults();
                    assertEquals(1, res.size());
                    Expression r = res.get(0).getExpression();
                    assertEquals("and(B,C)", r.toString());
                }))
                .add(new GuiTester.CloseTopMost())
                .press("F2")
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(4, main.getCircuitComponent().getCircuit().getElements().size())))
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
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(7, main.getCircuitComponent().getCircuit().getElements().size())))
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
            .add(new GuiTester.WindowCheck<>(AllSolutionsDialog.class, (gt, asd) -> asd.getParent().requestFocus()))
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

    public void testDraw() {
        new GuiTester()
                .add(new DrawCircuit("../../main/dig/sequential/JK-MS.dig"))
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 8))
                .add(new GuiTester.CloseTopMost())
                .press("control typed z", 65)
                .delay(1000)
                .press("control typed y", 65)
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 8))
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testCopyPaste() {
        new GuiTester("../../main/dig/sequential/JK-MS.dig")
                .add(new SelectAll())
                .press("control typed c")
                .press("F10")
                .press("RIGHT", 2)
                .press("DOWN", 9)
                .press("ENTER")
                .delay(500)
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 8))
                .add(new GuiTester.CloseTopMost())
                .add(new SelectAll())
                .press("DELETE")
                .press("control typed v")
                .mouseClick(InputEvent.BUTTON1_MASK)
                .press("F1")
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 8))
                .add(new GuiTester.CloseTopMost())
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
                .add(new GuiTester.CheckTextInWindow<>(JDialog.class, "Design fits successfully"))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testTestEditor() {
        new GuiTester("dig/manualError/11_editTest.dig")
                .mouseMove(200, 200)
                .mouseClick(InputEvent.BUTTON3_MASK)
                .type("testIdentzz")
                .press("TAB")
                .press("SPACE")
                .type("A B C\n0 0 0\n0 1 0\n1 0 0\n1 1 1")
                .press("F1")
                .press("TAB")
                .press("SPACE")
                .press("TAB", 4)
                .press("SPACE")
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "testIdentzz")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 4))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testSplitWire() {
        new GuiTester()
                .mouseMove(100, 100)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .press('d')
                .mouseMove(300, 300)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .mouseClick(InputEvent.BUTTON3_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(1, main.getCircuitComponent().getCircuit().getWires().size())))
                .mouseMove(200, 200)
                .press('s')
                .mouseMove(250, 150)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(2, main.getCircuitComponent().getCircuit().getWires().size())))
                .execute();
    }

    public void testMoveSelected() {
        new GuiTester()
                .mouseMove(110, 110)
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, main) -> {
                    final CircuitComponent cc = main.getCircuitComponent();
                    final VisualElement ve = new VisualElement(And.DESCRIPTION.getName())
                            .setShapeFactory(cc.getLibrary().getShapeFactory());
                    cc.setPartToInsert(ve);
                }))
                .mouseClick(InputEvent.BUTTON1_MASK)
                .mouseMove(100, 100)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .mouseMove(400, 400)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, main) -> {
                    final Circuit c = main.getCircuitComponent().getCircuit();
                    assertEquals(1, c.getElements().size());
                    final Vector pos = c.getElements().get(0).getPos();
                    assertTrue(pos.x > 300);
                    assertTrue(pos.y > 300);

                }))
                .execute();
    }

    public void testGroupEdit() {
        new GuiTester("dig/manualError/12_groupEdit.dig")
                .add(new SelectAll())
                .mouseClick(InputEvent.BUTTON3_MASK)
                .press("TAB", 2)
                .type("6")
                .press("ENTER")
                .add(new GuiTester.WindowCheck<>(Main.class, (guiTester, main) -> {
                    ArrayList<VisualElement> l = main.getCircuitComponent().getCircuit().getElements();
                    assertEquals(8, l.size());
                    for (VisualElement e : l)
                        assertEquals(16, (int) e.getElementAttributes().get(Keys.BITS));
                }))
                .execute();
    }


    public static class CheckErrorDialog extends GuiTester.WindowCheck<ErrorMessage.ErrorDialog> {
        private final String[] expected;

        public CheckErrorDialog(String... expected) {
            super(ErrorMessage.ErrorDialog.class);
            this.expected = expected;
        }

        @Override
        public void checkWindow(GuiTester guiTester, ErrorMessage.ErrorDialog errorDialog) {
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
        public void checkWindow(GuiTester guiTester, TableDialog td) {
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

    private class DrawCircuit extends GuiTester.WindowCheck<Main> {
        private final String filename;

        public DrawCircuit(String filename) {
            super(Main.class);
            this.filename = filename;
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) throws InterruptedException, IOException {
            File file = new File(Resources.getRoot(), filename);
            Circuit circuit = Circuit.loadCircuit(file, main.getCircuitComponent().getLibrary().getShapeFactory());

            int xMin = Integer.MAX_VALUE;
            int yMin = Integer.MAX_VALUE;
            for (Wire w : circuit.getWires()) {
                if (w.p1.x < xMin) xMin = w.p1.x;
                if (w.p2.x < xMin) xMin = w.p2.x;
                if (w.p1.y < yMin) yMin = w.p1.y;
                if (w.p2.y < yMin) yMin = w.p2.y;
            }

            Point loc = main.getCircuitComponent().getLocation();
            xMin -= loc.x + SIZE * 5;
            yMin -= loc.y + SIZE * 2;

            for (Wire w : circuit.getWires()) {
                guiTester.mouseClickNow(w.p1.x - xMin, w.p1.y - yMin, InputEvent.BUTTON1_MASK);
                Thread.sleep(100);
                if (w.p1.x != w.p2.x && w.p1.y != w.p2.y)
                    guiTester.typeNow("typed d");

                guiTester.mouseClickNow(w.p2.x - xMin, w.p2.y - yMin, InputEvent.BUTTON1_MASK);
                guiTester.mouseClickNow(w.p2.x - xMin, w.p2.y - yMin, InputEvent.BUTTON3_MASK);
                Thread.sleep(100);
            }

            for (VisualElement v : circuit.getElements()) {
                Vector pos = v.getPos();
                v.setPos(new Vector(0, 0));
                final GraphicMinMax minMax = v.getMinMax(false);
                pos = pos.add(minMax.getMax());
                main.getCircuitComponent().setPartToInsert(v);
                guiTester.mouseClickNow(pos.x - xMin, pos.y - yMin, InputEvent.BUTTON1_MASK);
                Thread.sleep(200);
            }
        }
    }

    private class SelectAll extends GuiTester.WindowCheck<Main> {
        /**
         * Creates a new instance
         */
        public SelectAll() {
            super(Main.class);
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) {
            final CircuitComponent c = main.getCircuitComponent();
            Point loc = c.getLocation();
            guiTester.mouseMoveNow(loc.x + 2, loc.y + 2);
            guiTester.mouseDownNow(InputEvent.BUTTON1_MASK);
            guiTester.mouseMoveNow(loc.x + c.getWidth() - 2, loc.y + c.getHeight() - 2);
            guiTester.mouseReleaseNow(InputEvent.BUTTON1_MASK);
        }
    }
}
