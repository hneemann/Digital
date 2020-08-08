/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.External;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.wiring.Driver;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.fsm.FSM;
import de.neemann.digital.fsm.State;
import de.neemann.digital.fsm.Transition;
import de.neemann.digital.fsm.gui.FSMFrame;
import de.neemann.digital.gui.DigitalRemoteInterface;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.NumberingWizard;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.gui.components.*;
import de.neemann.digital.gui.components.data.GraphDialog;
import de.neemann.digital.gui.components.karnaugh.KarnaughMapComponent;
import de.neemann.digital.gui.components.karnaugh.KarnaughMapDialog;
import de.neemann.digital.gui.components.table.AllSolutionsDialog;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.gui.components.terminal.KeyboardDialog;
import de.neemann.digital.gui.components.terminal.Terminal;
import de.neemann.digital.gui.components.testing.TestAllDialog;
import de.neemann.digital.gui.components.testing.ValueTableDialog;
import de.neemann.digital.gui.remote.RemoteException;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.gui.ErrorMessage;
import junit.framework.TestCase;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static de.neemann.digital.integration.GuiTester.getBaseContainer;
import static de.neemann.digital.testing.TestCaseElement.TESTDATA;

/**
 * These tests are excluded from the maven build because gui tests are sometimes fragile.
 * They may not behave as expected on all systems.
 * Run this tests directly from your IDE.
 * <p>
 * maven: mvn -Dtest=TestInGUI test
 */
public class TestInGUI extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Settings.getInstance().getAttributes().set(Keys.SETTINGS_DEFAULT_TREESELECT, false);
        Settings.getInstance().getAttributes().set(Keys.SETTINGS_JAR_PATH, new File(""));
    }

    public void testErrorAtStart1() {
        new GuiTester("dig/manualError/01_fastRuntime.dig")
                .press("SPACE")
                .add(new CheckErrorDialog("01_fastRuntime.dig", Lang.get("err_burnError")))
                .add(new GuiTester.CloseTopMost())
                .add(new CheckColorInCircuit(Driver.DESCRIPTION, 0, SIZE + SIZE2, (c) -> assertEquals(Style.ERROR.getColor(), c)))
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
                .add(new CheckColorInCircuit(Driver.DESCRIPTION, 0, SIZE + SIZE2, (c) -> assertEquals(Style.ERROR.getColor(), c)))
//                .ask("Is the driver output colored red?")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testErrorAtStart4() {
        new GuiTester("dig/manualError/07_creationPhase.dig")
                .press("SPACE")
                .add(new CheckErrorDialog("07_creationPhase.dig", "ErrorY"))
                .add(new GuiTester.CloseTopMost())
                .add(new CheckColorInCircuit(Out.DESCRIPTION, SIZE * 2, 0, (c) -> assertEquals(Style.ERROR.getColor(), c)))
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
                .add(new CheckColorInCircuit(Driver.DESCRIPTION, 0, SIZE + SIZE2, (c) -> assertEquals(Style.ERROR.getColor(), c)))
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
                .add(new CheckColorInCircuit(Driver.DESCRIPTION, 0, SIZE + SIZE2, (c) -> assertEquals(Style.ERROR.getColor(), c)))
//                .ask("Is the driver output colored red?")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .execute();
    }

    public void testTreeView() {
        new GuiTester()
                .delay(500)
                .press("F5")
                .mouseMove(100, 100)
                .delay(300)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(200)
                .mouseMove(400, 200)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(500)
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, main) -> {
                    Circuit c = main.getCircuitComponent().getCircuit();
                    assertEquals(1, c.getElements().size());
                }))
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
                .press("F1", 1)
                .press("TAB", 1)
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

    private final GuiTester createNew4VarTruthTable = new GuiTester()
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
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 197, 110, new Color(255, 127, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 317, 108, new Color(127, 255, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 137, 169, new Color(191, 127, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 257, 170, new Color(127, 127, 191)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 197, 228, new Color(227, 227, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 316, 228, new Color(127, 255, 255)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 137, 290, new Color(127, 127, 255)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 257, 290, new Color(255, 127, 255)))
//                .add(new GuiTester.ColorPickerCreator(KarnaughMapComponent.class))
//                .ask("Shows the k-map a checkerboard pattern?")
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testEdges() {
        new GuiTester()
                .use(createNew4VarTruthTable)
                .add(new EnterTruthTable(1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0))
                .press("F1")
                .delay(500)
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 137, 98, new Color(255, 127, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 265, 95, new Color(255, 127, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 265, 291, new Color(255, 127, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 137, 296, new Color(255, 127, 127)))
//                .add(new GuiTester.ColorPickerCreator(KarnaughMapComponent.class))
//                .ask("Are the edges covered in the k-map?")
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
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 213, 179, new Color(191, 127, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 144, 231, new Color(255, 127, 127)))
                .add(new GuiTester.ColorPicker(KarnaughMapComponent.class, 315, 230, new Color(127, 255, 127)))
//                .add(new GuiTester.ColorPickerCreator(KarnaughMapComponent.class))
//                .ask("Shows the k-map a 'two out of three' pattern?")
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    private final GuiTester create4BitCounterTruthTable = new GuiTester()
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

    // constant is not possible because of language access
    private GuiTester createCheck4BitCounterCircuit() {
        return new GuiTester()
                .add(new AddTestCaseToCircuit(
                        "C Q_3 Q_2 Q_1 Q_0\n" +
                                "repeat(32) C bits(4,n+1)"))
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 32))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost());
    }

    public void testCounterJK() {
        new GuiTester()
                .use(create4BitCounterTruthTable)
                .press("F10")
                .press("RIGHT", 3)
                .press("DOWN", 2)
                .press("ENTER")
                .delay(500)
                .use(createCheck4BitCounterCircuit())
                .execute();
    }

    public void testCounterD() {
        new GuiTester()
                .use(create4BitCounterTruthTable)
                .press("F2")
                .delay(500)
                .use(createCheck4BitCounterCircuit())
                .execute();
    }

    public void testAutoWire() {
        new GuiTester()
                .add(new DrawCircuit("dig/autoWire.dig"))
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 4))
                .add(new GuiTester.CloseTopMost())
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
                .press("control typed s")
                .add(new GuiTester.WindowCheck<>(JDialog.class))
                .typeTempFile("jk-ms")
                .press("ENTER")
                .press("control typed z", 65)
                .delay(1000)
                .press("control typed y", 65)
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 8))
                .press("F10", "RIGHT", "DOWN", "ENTER")
                .add(new GuiTester.WindowCheck<>(GraphDialog.class))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testCopyPaste() {
        new GuiTester("../../main/dig/sequential/JK-MS.dig")
                .add(new SelectAll())
                .press("control typed c")
                .press("F10")
                .press("RIGHT", 1)
                .press("DOWN", 10)
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
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
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
        new GuiTester("dig/manualError/16_hardware.dig")
                .press("F9")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(Window.class, (gt, w) -> {
                    if (w instanceof AllSolutionsDialog) w.getParent().requestFocus();
                }))
                .delay(500)
                .press("F10")
                .press("RIGHT", 3)
                .press("DOWN", 7)
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
                .delay(600)
                .add(new GuiTester.WaitFor(() -> {
                    Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
                    return !(activeWindow instanceof Main || activeWindow instanceof TableDialog);
                }))
                .add(new GuiTester.CheckTextInWindow<>(JDialog.class, "Design fits successfully"))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testTestEditor() {
        new GuiTester("dig/manualError/11_editTest.dig")
                .delay(300)
                .add(new SetMouseToElement((v) -> v.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION)))
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .delay(300)
                .type("testIdentzz")
                .delay(300)
                .press("TAB")
                .press("SPACE")
                .delay(600)
                .type("A B C\n0 0 0\n0 1 0\n1 0 0\n1 1 1")
                .delay(300)
                .press("F1")
                .press("TAB", 2)
                .press("SPACE")
                .delay(500)
                .press("TAB", 5)
                .press("SPACE")
                .delay(500)
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
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .press('d')
                .mouseMove(300, 300)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(1, main.getCircuitComponent().getCircuit().getWires().size())))
                .mouseMove(200, 200)
                .press('s')
                .mouseMove(250, 150)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(2, main.getCircuitComponent().getCircuit().getWires().size())))
                .execute();
    }

    public void testKeyboard() {
        new GuiTester("dig/io/keyboard.dig")
                .press("SPACE")
                .delay(500)
                .press("A")
                .delay(500)
                .add(new GuiTester.CloseTopMost())
                .delay(500)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals('a', main.getModel().getOutput("akt").getValue())))
                .execute();
    }

    private KeyboardDialog keyboard;

    public void testKeyboard2() {
        new GuiTester("dig/io/keyboard2.dig")
                .press("SPACE")
                .delay(200)
                .add(new GuiTester.WindowCheck<>(KeyboardDialog.class,
                        (gt, kb) -> keyboard = kb))
                .press("ENTER")
                .delay(200)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, td) -> keyboard.toFront()))
                .delay(200)
                .type("Hello World!")
                .delay(200)
                .add(new GuiTester.WindowCheck<>(KeyboardDialog.class,
                        (gt, kb) -> kb.dispose()))
                .delay(200)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> {
                            List<Terminal> n = main.getModel().findNode(Terminal.class);
                            assertEquals(1, n.size());
                            Terminal t = n.get(0);
                            assertEquals("\nHello World!", t.getTerminalInterface().getText());
                        }))
                .execute();
    }

    public void testMoveSelectedComponent() {
        new GuiTester()
                .mouseMove(110, 110)
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, main) -> {
                    final CircuitComponent cc = main.getCircuitComponent();
                    final VisualElement ve = new VisualElement(And.DESCRIPTION.getName())
                            .setShapeFactory(cc.getLibrary().getShapeFactory());
                    cc.setPartToInsert(ve);
                }))
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .mouseMove(100, 100)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .mouseMove(400, 400)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, main) -> {
                    final Circuit c = main.getCircuitComponent().getCircuit();
                    assertEquals(1, c.getElements().size());
                    final Vector pos = c.getElements().get(0).getPos();
                    assertTrue(pos.x > 300);
                    assertTrue(pos.y > 300);

                }))
                .execute();
    }

    public void testShortcutsPlusMinus() {
        new GuiTester()
                .mouseMove(100 + SIZE * 2, 100 + SIZE * 2)
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, main) -> {
                    final CircuitComponent cc = main.getCircuitComponent();
                    final VisualElement ve = new VisualElement(And.DESCRIPTION.getName())
                            .setShapeFactory(cc.getLibrary().getShapeFactory());
                    cc.setPartToInsert(ve);
                }))
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .mouseMove(100, 100)
                .press("PLUS")
                .press("PLUS")
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, main) -> {
                    final Circuit c = main.getCircuitComponent().getCircuit();
                    assertEquals(1, c.getElements().size());
                    assertEquals(4, (int) c.getElements().get(0).getElementAttributes().get(Keys.INPUT_COUNT));

                }))
                .press("MINUS")
                .press("MINUS")
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, main) -> {
                    final Circuit c = main.getCircuitComponent().getCircuit();
                    assertEquals(1, c.getElements().size());
                    assertEquals(2, (int) c.getElements().get(0).getElementAttributes().get(Keys.INPUT_COUNT));

                }))
                .execute();
    }

    public void testShortcutsLD() {
        new GuiTester()
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", "RIGHT", "ENTER")
                .mouseMove(100, 150)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(1, main.getCircuitComponent().getCircuit().getElements().size())))
                .mouseMove(200, 150)
                .press('l')
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(2, main.getCircuitComponent().getCircuit().getElements().size())))
                .mouseMove(80, 130)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .press("control typed d")
                .mouseMove(100, 250)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(3, main.getCircuitComponent().getCircuit().getElements().size())))
                .execute();
    }

    public void testSaveDialog() {
        new GuiTester()
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", "RIGHT", "ENTER")
                .mouseMove(100, 150)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(100)

                // aboard with escape
                .press("control typed n")
                .delay(100)
                .add(new GuiTester.WindowCheck<>(JDialog.class,
                        (guiTester, window) -> assertEquals(Lang.get("win_stateChanged"), window.getTitle())))
                .press("ESCAPE")
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(1, main.getCircuitComponent().getCircuit().getElements().size())))

                // press edit further
                .press("control typed n")
                .delay(100)
                .add(new GuiTester.SetFocusTo<>(JDialog.class,
                        c -> c instanceof JButton && ((JButton) c).getText().equals(Lang.get("btn_editFurther"))))
                .press("SPACE")
                .delay(100)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(1, main.getCircuitComponent().getCircuit().getElements().size())))

                // press save and the escape the save dialog (JFileChooser)
                .press("control typed n")
                .delay(100)
                .add(new GuiTester.SetFocusTo<>(JDialog.class,
                        c -> c instanceof JButton && ((JButton) c).getText().equals(Lang.get("btn_save"))))
                .press("SPACE")
                .delay(100)
                .add(new GuiTester.WindowCheck<>(JDialog.class))
                .press("ESCAPE")
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(1, main.getCircuitComponent().getCircuit().getElements().size())))

                // press save and save the file
                .press("control typed n")
                .delay(100)
                .add(new GuiTester.SetFocusTo<>(JDialog.class,
                        c -> c instanceof JButton && ((JButton) c).getText().equals(Lang.get("btn_save"))))
                .press("SPACE")
                .delay(100)
                .add(new GuiTester.WindowCheck<>(JDialog.class))
                .typeTempFile("save.dig")
                .press("ENTER")
                .delay(100)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(0, main.getCircuitComponent().getCircuit().getElements().size())))

                .execute();
    }

    public void testSaveDialog2() {
        new GuiTester()
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", "RIGHT", "ENTER")
                .mouseMove(100, 150)
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(100)

                // discard changes
                .press("control typed n")
                .delay(100)
                .add(new GuiTester.SetFocusTo<>(JDialog.class,
                        c -> c instanceof JButton && ((JButton) c).getText().equals(Lang.get("btn_discard"))))
                .press("SPACE")
                .delay(100)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(0, main.getCircuitComponent().getCircuit().getElements().size())))
                .execute();
    }


    public void test74xxFunctions() {
        new GuiTester("dig/manualError/10_74xx.dig")
                .press("F10")
                .press("RIGHT", 1)
                .press("DOWN", 4)
                .press("RIGHT")
                .press("DOWN", 2)
                .press("ENTER")
                .add(new ClickInputsAndOutputs())
                .press("ESCAPE")
                .add(new GuiTester.WindowCheck<>(Main.class, (guiTester, main) -> {
                    final CircuitComponent cc = main.getCircuitComponent();
                    ArrayList<VisualElement> el = cc.getCircuit().getElements();
                    int n = 0;
                    for (VisualElement ve : el)
                        if (ve.equalsDescription(In.DESCRIPTION) || ve.equalsDescription(Out.DESCRIPTION)) {
                            n++;
                            assertEquals("" + n, ve.getElementAttributes().get(Keys.PINNUMBER));
                        }
                }))
                .add(new SelectAll())
                .press("F10")
                .press("RIGHT", 1)
                .press("DOWN", 5)
                .press("RIGHT")
                .press("ENTER")
                .type("U")
                .press("ENTER")
                .add(new PinNameChecker("UC"))
                .press("F10")
                .press("RIGHT", 1)
                .press("DOWN", 5)
                .press("RIGHT")
                .press("DOWN")
                .press("ENTER")
                .add(new PinNameChecker("C"))
                .press("F10")
                .press("RIGHT", 1)
                .press("DOWN", 5)
                .press("RIGHT")
                .press("DOWN", 3)
                .press("ENTER")
                .add(new GuiTester.WindowCheck<>(Main.class, (guiTester, main) -> {
                    final CircuitComponent cc = main.getCircuitComponent();
                    ArrayList<VisualElement> el = cc.getCircuit().getElements();
                    for (VisualElement ve : el)
                        if (ve.equalsDescription(In.DESCRIPTION) || ve.equalsDescription(Out.DESCRIPTION))
                            assertEquals("", ve.getElementAttributes().get(Keys.PINNUMBER));
                }))
                .press("F10")
                .press("RIGHT", 1)
                .press("DOWN", 5)
                .press("RIGHT")
                .press("DOWN", 4)
                .press("ENTER")
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> assertEquals(7, main.getCircuitComponent().getCircuit().getElements().size())))
                .execute();

    }

    public void test74xxUsage() {
        new GuiTester()
                .add(new DrawCircuit("../../main/dig/74xx/74xx_xor.dig"))
                .press("F8")
                .delay(500)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 4))
                .add(new GuiTester.CloseTopMost())
                .execute();
    }


    public void testSingleValueDialog() {
        new GuiTester("dig/manualError/13_singleValueDialog.dig")
                .press("SPACE")
                .delay(500)
                .add(new SetMouseToElement(v -> v.equalsDescription(In.DESCRIPTION)))

                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(500)
                .type("0x44")
                .press("ENTER")
                .delay(500)
                .add(new CheckOutputValue(0x44))

                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(500)
                .type("44")
                .press("ENTER")
                .delay(500)

                .add(new CheckOutputValue(44))
                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(500)
                .type("0b111")
                .press("ENTER")
                .delay(500)
                .add(new CheckOutputValue(7))

                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(500)
                .press("shift typed #")   // works only on german keyboard layout
                .press("shift typed A")
                .press("shift typed #")
                .press("ENTER")
                .delay(500)
                .add(new CheckOutputValue('A'))

                .mouseClick(InputEvent.BUTTON1_DOWN_MASK)
                .delay(500)
                .type("0")
                .press("TAB", 5)
                .press("SPACE", "ENTER")
                .delay(500)
                .add(new CheckOutputValue(8))

                .execute();
    }

    public void testMeasurementTable() {
        new GuiTester("dig/manualError/13_singleValueDialog.dig")
                .press("SPACE")
                .delay(500)
                .press("F10")
                .press("RIGHT", 3)
                .press("DOWN")
                .press("ENTER")
                .delay(500)
                .add(new GuiTester.CheckTableRows<>(ProbeDialog.class, 2))
                .press("DOWN")
                .press("RIGHT")
                .press('\b')
                .press('8')
                .press("ENTER")
                .delay(500)
                .add(new GuiTester.CloseTopMost())
                .add(new CheckOutputValue(8))
                .execute();
    }

    public void testGroupEdit() {
        new GuiTester("dig/manualError/12_groupEdit.dig")
                .add(new SelectAll())
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .delay(500)
                .press("TAB", 2)
                .type("6")
                .press("ENTER")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(Main.class, (guiTester, main) -> {
                    ArrayList<VisualElement> l = main.getCircuitComponent().getCircuit().getElements();
                    assertEquals(8, l.size());
                    for (VisualElement e : l)
                        assertEquals(16, (int) e.getElementAttributes().get(Keys.BITS));
                }))
                .execute();
    }

    public void testInputInvertEdit() {
        new GuiTester("dig/manualError/14_inputInvert.dig")
                .add(new SetMouseToElement((v) -> v.equalsDescription(And.DESCRIPTION)))
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .delay(500)
                .press("TAB", 3)
                .press("SPACE")
                .delay(200)
                .press("TAB", "SPACE", "TAB", "SPACE")
                .delay(200)
                .press("TAB", "TAB", "TAB", "SPACE")
                .delay(200)
                .press("F8")
                .delay(200)
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 4))
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testDataEditor() {
        new GuiTester("dig/manualError/15_romDataEditor.dig")
                .add(new SetMouseToElement((v) -> v.equalsDescription(ROM.DESCRIPTION)))
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .delay(500)
                .press("TAB", "SPACE")
                .delay(500)
                .press("TAB", "TAB")
                .type("7")
                .press("TAB", "TAB")
                .type("6")
                .press("TAB", "TAB")
                .type("5")
                .press("TAB", "TAB")
                .type("4")
                .press("TAB", "TAB")
                .type("3")
                .press("TAB", "TAB")
                .type("2")
                .press("TAB", "TAB")
                .type("1")
                .press("TAB")
                .add(new GuiTester.SetFocusTo<>(DataEditor.class,
                        (c) -> c instanceof JButton && ((JButton) c).getText().equals(Lang.get("ok"))))
                .press("SPACE")
                .delay(500)
                .add(new GuiTester.SetFocusTo<>(AttributeDialog.class,
                        (c) -> c instanceof JButton && ((JButton) c).getText().equals(Lang.get("ok"))))
                .press("SPACE")
                .delay(500)
                .press("F8")
                .add(new GuiTester.CheckTextInWindow<>(ValueTableDialog.class, Lang.get("msg_test_N_Passed", "")))
                .add(new GuiTester.CheckTableRows<>(ValueTableDialog.class, 8))
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testRomDialog() {
        new GuiTester("dig/test/romContent/rom.dig")
                .press("F10")
                .press("RIGHT", 1)
                .press("DOWN", 1)
                .press("ENTER", 1)
                .press("control TAB", 7)
                .press("RIGHT", 1)
                .add(new GuiTester.SetFocusTo<>(AttributeDialog.class,
                        b -> b instanceof JButton && Lang.get("btn_edit").equals(((JButton) b).getText())))
                .press("SPACE")
                .delay(200)
                .add(new GuiTester.CheckListRows<>(ROMEditorDialog.class, 8))
                .press("DOWN")
                .add(new GuiTester.SetFocusTo<>(ROMEditorDialog.class,
                        b -> b instanceof JButton && Lang.get("btn_edit").equals(((JButton) b).getText())))
                .press("SPACE")
                .delay(100)
                .add(new GuiTester.WindowCheck<>(DataEditor.class))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.SetFocusTo<>(ROMEditorDialog.class,
                        b -> b instanceof JButton && Lang.get("btn_clearData").equals(((JButton) b).getText())))
                .press("SPACE")
                .delay(100)
                .add(new GuiTester.SetFocusTo<>(ROMEditorDialog.class,
                        b -> b instanceof JButton && Lang.get("btn_edit").equals(((JButton) b).getText())))
                .press("SPACE")
                .delay(100)
                .add(new GuiTester.ComponentTraverse<DataEditor>(DataEditor.class) {
                    @Override
                    public void visit(Component component) {
                        if (component instanceof JTable) {
                            TableModel model = ((JTable) component).getModel();
                            assertEquals(4, model.getRowCount());
                            for (int i = 0; i < 4; i++) {
                                final Object valueAt = model.getValueAt(i, 1);
                                assertEquals("0", valueAt.toString());
                            }
                            found();
                        }
                    }
                })

                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testGhdlCheckCode() {
        new GuiTester("dig/external/ghdl/ghdl.dig")
                .add(new SetMouseToElement(v -> v.equalsDescription(External.DESCRIPTION)))
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .delay(500)
                .add(new GuiTester.SetFocusTo<>(AttributeDialog.class,
                        c -> c instanceof JButton && ((JButton) c).getText().equals(Lang.get("btn_checkCode"))))
                .press("SPACE")
                .delay(1000)
                .add(new GuiTester.SetFocusTo<>(AttributeDialog.class, c -> c instanceof JTextArea))
                .delay(100)
                .type("\b")
                .delay(100)
                .add(new GuiTester.SetFocusTo<>(AttributeDialog.class,
                        c -> c instanceof JButton && ((JButton) c).getText().equals(Lang.get("btn_checkCode"))))
                .press("SPACE")
                .delay(1000)
                .add(new GuiTester.WindowCheck<>(ErrorMessage.ErrorDialog.class))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testTestAll() {
        new GuiTester("dig/test/arith/FullSub.dig")
                .press("F10")
                .press("F11")
                .add(new GuiTester.ComponentTraverse<TestAllDialog>(TestAllDialog.class) {
                    @Override
                    public void visit(Component component) throws InterruptedException {
                        if (component instanceof JTable) {
                            getWindow().getFolderTestRunner().waitUntilFinished();
                            JTable table = (JTable) component;
                            assertEquals(5, table.getModel().getRowCount());
                            found();
                        }
                    }
                })
                .press("DOWN")
                .press("SPACE")
                .add(new GuiTester.WindowCheck<>(Main.class))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testNetRename() {
        new GuiTester("dig/net/netRename.dig")
                .add(new SetMouseToElement(v -> v.equalsDescription(Tunnel.DESCRIPTION) && v.getPos().x < 400))
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .delay(200)
                .type("et")
                .press("ENTER")
                .delay(200)
                .add(new GuiTester.CheckTextInWindow<>(JDialog.class, "'net'"))
                .add(new GuiTester.CheckTextInWindow<>(JDialog.class, " 2 "))
                .delay(200)
                .press("ENTER")
                .delay(200)
                .add(new GuiTester.WindowCheck<Main>(Main.class) {
                    @Override
                    public void checkWindow(GuiTester guiTester, Main main) {
                        List<VisualElement> e = main.getCircuitComponent().getCircuit()
                                .getElements(v -> v.equalsDescription(Tunnel.DESCRIPTION));
                        assertEquals(3, e.size());
                        for (VisualElement v : e)
                            assertEquals("net", v.getElementAttributes().get(Keys.NETNAME));
                    }
                })
                .execute();
    }

    public void testFSM() {
        new GuiTester()
                .press("F10")
                .press("RIGHT", 4)
                .press("DOWN", 4)
                .press("SPACE")
                .delay(1000)
                .add(new GuiTester.WindowCheck<>(FSMFrame.class))
                .press("F10")
                .press("control N")
                .mouseMove(100, 200)
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(AttributeDialog.class))
                .type("Aus")
                .press("ENTER")
                .delay(100)
                .mouseMove(400, 200)
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .add(new GuiTester.WindowCheck<>(AttributeDialog.class))
                .type("Ein")
                .press("ENTER")
                .delay(100)
                .add(guiTester -> guiTester.mousePressNow(InputEvent.BUTTON3_DOWN_MASK))
                .mouseMove(100, 200)
                .add(guiTester -> guiTester.mouseReleaseNow(InputEvent.BUTTON3_DOWN_MASK))
                .delay(100)
                .mouseMove(250, 200)
                .mouseClick(InputEvent.BUTTON3_DOWN_MASK)
                .type("A")
                .press("ENTER")
                .delay(100)
                .mouseMove(100, 200)
                .add(guiTester -> guiTester.mousePressNow(InputEvent.BUTTON3_DOWN_MASK))
                .mouseMove(400, 200)
                .add(guiTester -> guiTester.mouseReleaseNow(InputEvent.BUTTON3_DOWN_MASK))
                .add(new GuiTester.SetFocusTo<>(FSMFrame.class, component -> component instanceof JComboBox))
                .press("DOWN", 2)
                .press("ENTER")
                .delay(100)
                .mouseMove(100, 200)
                .add(guiTester -> guiTester.mousePressNow(InputEvent.BUTTON1_DOWN_MASK))
                .mouseMove(100, 300)
                .add(guiTester -> guiTester.mouseReleaseNow(InputEvent.BUTTON3_DOWN_MASK))
                .add(new GuiTester.WindowCheck<>(FSMFrame.class, (guiTester, window) -> {
                    final FSM fsm = window.getFSM();
                    assertEquals(2, fsm.getStates().size());
                    State s0 = fsm.getStates().get(0);
                    State s1 = fsm.getStates().get(1);
                    assertTrue(Math.abs(s0.getPos().getY() - s1.getPos().getY()) > 50);
                    assertEquals(2, fsm.getTransitions().size());
                    Transition t0 = fsm.getTransitions().get(0);
                    Transition t1 = fsm.getTransitions().get(1);
                    assertTrue(Math.abs(t0.getPos().getY() - t1.getPos().getY()) > 50);
                }))
                .add(new GuiTester.CloseTopMost())
                .delay(100)
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    public void testRemoteInterface() throws InterruptedException, RemoteException {
        Main m = new Main.MainBuilder()
                .setFileToOpen(new File(Resources.getRoot(), "dig/remoteInterface/measure.dig"))
                .build();

        SwingUtilities.invokeLater(() -> m.setVisible(true));
        DigitalRemoteInterface ri = m;

        Thread.sleep(1000);
        ri.start(null);
        Thread.sleep(1000);
        String json = ri.measure();
        assertEquals("{\"Q\":0,\"C\":0}", json);
        Thread.sleep(1000);
        ri.doSingleStep();
        Thread.sleep(1000);
        json = ri.measure();
        assertEquals("{\"Q\":1,\"C\":0}", json);
        Thread.sleep(1000);
        ri.stop();
        m.dispose();
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

    private static class EnterTruthTable implements GuiTester.Runnable {
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

    private static class DrawCircuit extends GuiTester.WindowCheck<Main> {
        private final String filename;

        public DrawCircuit(String filename) {
            super(Main.class);
            this.filename = filename;
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) throws InterruptedException, IOException {
            File file = new File(Resources.getRoot(), filename);
            final ElementLibrary library = main.getCircuitComponent().getLibrary();
            Circuit circuit = Circuit.loadCircuit(file, library.getShapeFactory());

            int xMin = Integer.MAX_VALUE;
            int yMin = Integer.MAX_VALUE;
            for (Wire w : circuit.getWires()) {
                if (w.p1.x < xMin) xMin = w.p1.x;
                if (w.p2.x < xMin) xMin = w.p2.x;
                if (w.p1.y < yMin) yMin = w.p1.y;
                if (w.p2.y < yMin) yMin = w.p2.y;
            }

            Point loc = getCircuitPos(main);
            xMin -= loc.x + SIZE * 5;
            yMin -= loc.y + SIZE * 2;

            for (Wire w : circuit.getWires()) {
                guiTester.mouseClickNow(w.p1.x - xMin, w.p1.y - yMin, InputEvent.BUTTON1_DOWN_MASK);
                if (w.p1.x != w.p2.x && w.p1.y != w.p2.y)
                    guiTester.typeNow("typed d");

                guiTester.mouseClickNow(w.p2.x - xMin, w.p2.y - yMin, InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(50);
                guiTester.mouseClickNow(w.p2.x - xMin, w.p2.y - yMin, InputEvent.BUTTON3_DOWN_MASK);
            }

            for (VisualElement v : circuit.getElements()) {
                Vector pos = v.getPos();
                v.setPos(new Vector(0, 0));
                final GraphicMinMax minMax = v.getMinMax(false);
                pos = pos.add(minMax.getMax());
                main.getCircuitComponent().setPartToInsert(v);
                guiTester.mouseClickNow(pos.x - xMin, pos.y - yMin, InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(400);
            }
        }
    }

    private static Point getCircuitPos(Main main) {
        Point ci = new Point();
        SwingUtilities.convertPointToScreen(ci, main.getCircuitComponent());
        Point ma = new Point();
        SwingUtilities.convertPointToScreen(ma, getBaseContainer());
        return new Point(ci.x - ma.x, ci.y - ma.y);
    }

    private static class SelectAll extends GuiTester.WindowCheck<Main> {
        /**
         * Creates a new instance
         */
        public SelectAll() {
            super(Main.class);
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) {
            Point loc = getCircuitPos(main);
            CircuitComponent c = main.getCircuitComponent();
            guiTester.mouseMoveNow(loc.x + 2, loc.y + 2);
            guiTester.mousePressNow(InputEvent.BUTTON1_DOWN_MASK);
            guiTester.mouseMoveNow(loc.x + c.getWidth() - 2, loc.y + c.getHeight() - 2);
            guiTester.mouseReleaseNow(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    private static class CheckColorInCircuit extends GuiTester.WindowCheck<Main> {
        private final ElementTypeDescription description;
        private final int dx;
        private final int dy;
        private final GuiTester.ColorCheckInterface cpi;

        public CheckColorInCircuit(ElementTypeDescription description, int dx, int dy, GuiTester.ColorCheckInterface cpi) {
            super(Main.class);
            this.description = description;
            this.dx = dx;
            this.dy = dy;
            this.cpi = cpi;
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) throws Exception {
            Thread.sleep(200);
            List<VisualElement> el = main
                    .getCircuitComponent()
                    .getCircuit()
                    .getElements(v -> v.equalsDescription(description));

            assertEquals("not exact one " + description.getName() + " found in circuit", 1, el.size());

            final Vector posInCirc = el.get(0).getPos().add(dx, dy);
            Point p = main.getCircuitComponent().transform(posInCirc);
            SwingUtilities.convertPointToScreen(p, main.getCircuitComponent());

            guiTester.getRobot().mouseMove(p.x, p.y);
            Thread.sleep(500);
            cpi.checkColor(guiTester.getRobot().getPixelColor(p.x, p.y));
        }
    }

    public static class SetMouseToElement extends GuiTester.WindowCheck<Main> {
        private final Circuit.ElementFilter filter;

        public SetMouseToElement(Circuit.ElementFilter filter) {
            super(Main.class);
            this.filter = filter;
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) throws Exception {
            Thread.sleep(200);

            List<VisualElement> el = main
                    .getCircuitComponent()
                    .getCircuit()
                    .getElements(filter);

            assertEquals("not exact one element found in circuit", 1, el.size());

            final VisualElement ve = el.get(0);
            GraphicMinMax mm = ve.getMinMax(false);
            Vector pos = mm.getMin().add(mm.getMax()).div(2);
            Point p = main.getCircuitComponent().transform(pos);
            SwingUtilities.convertPointToScreen(p, main.getCircuitComponent());

            guiTester.getRobot().mouseMove(p.x, p.y);
        }
    }

    private static class ClickInputsAndOutputs extends GuiTester.WindowCheck<NumberingWizard> {
        public ClickInputsAndOutputs() {
            super(NumberingWizard.class);
        }

        @Override
        public void checkWindow(GuiTester guiTester, NumberingWizard window) throws Exception {
            assertTrue(window.getParent() instanceof Main);
            Main main = (Main) window.getParent();

            final CircuitComponent cc = main.getCircuitComponent();
            ArrayList<VisualElement> el = cc.getCircuit().getElements();
            for (VisualElement ve : el)
                if (ve.equalsDescription(In.DESCRIPTION) || ve.equalsDescription(Out.DESCRIPTION)) {
                    GraphicMinMax mm = ve.getMinMax(false);
                    Point p = cc.transform(mm.getMin().add(mm.getMax()).div(2));
                    SwingUtilities.convertPointToScreen(p, cc);
                    guiTester.getRobot().mouseMove(p.x, p.y);
                    Thread.sleep(100);
                    guiTester.mouseClickNow(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(500);
                }
        }
    }

    private static class PinNameChecker extends GuiTester.WindowCheck<Main> {
        private final String prefix;

        public PinNameChecker(String prefix) {
            super(Main.class);
            this.prefix = prefix;
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) {
            final CircuitComponent cc = main.getCircuitComponent();
            ArrayList<VisualElement> el = cc.getCircuit().getElements();
            for (VisualElement ve : el)
                if (ve.equalsDescription(In.DESCRIPTION) || ve.equalsDescription(Out.DESCRIPTION))
                    assertTrue(ve.getElementAttributes().get(Keys.LABEL).startsWith(prefix));
        }
    }

    private static class CheckOutputValue extends GuiTester.WindowCheck<Main> {
        private final int val;

        public CheckOutputValue(int val) {
            super(Main.class);
            this.val = val;
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) {
            final ArrayList<Signal> outputs = main.getModel().getOutputs();
            assertEquals(1, outputs.size());
            assertEquals(val, outputs.get(0).getValue().getValue());
        }
    }

    private static class AddTestCaseToCircuit extends GuiTester.WindowCheck<Main> {
        private final String testdata;

        private AddTestCaseToCircuit(String testdata) {
            super(Main.class);
            this.testdata = testdata;
        }

        @Override
        public void checkWindow(GuiTester gt, Main main) {
            main.getCircuitComponent().getCircuit().add(
                    new VisualElement(TestCaseElement.TESTCASEDESCRIPTION.getName())
                            .setAttribute(TESTDATA, new TestCaseDescription(testdata))
                            .setShapeFactory(main.getCircuitComponent().getLibrary().getShapeFactory()));
        }
    }
}
