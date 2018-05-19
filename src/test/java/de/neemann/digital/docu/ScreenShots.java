/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.docu;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.gui.components.AttributeDialog;
import de.neemann.digital.gui.components.expression.ExpressionDialog;
import de.neemann.digital.gui.components.graphics.GraphicDialog;
import de.neemann.digital.gui.components.karnaugh.KarnaughMapDialog;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.gui.components.testing.TestCaseDescriptionDialog;
import de.neemann.digital.gui.components.testing.ValueTableDialog;
import de.neemann.digital.integration.GuiTester;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.TestInGUI;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.gui.language.Language;

import javax.imageio.ImageIO;
import javax.swing.FocusManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;


/**
 * Run this main method to create the screen shots required.
 */
public class ScreenShots {

    private static final int WIN_DX = 850;
    private static final int WIN_DY = 500;
    private static GraphicDialog graphic;

    public static void main(String[] args) {
        Settings.getInstance().getAttributes().set(Keys.SETTINGS_DEFAULT_TREESELECT, false);
        Settings.getInstance().getAttributes().set(Keys.SETTINGS_GRID, true);
//        mainScreenShot();
//        firstSteps();
//        hierarchicalDesign();
//        all();
    }

    private static void all() {
        // english
        Lang.setActualRuntimeLanguage(new Language("en"));
        Settings.getInstance().getAttributes()
                .set(Keys.SETTINGS_GRID, true)
                .set(Keys.SETTINGS_IEEE_SHAPES, true);
        mainScreenShot();
        firstSteps();
        hierarchicalDesign();

        // german
        Lang.setActualRuntimeLanguage(new Language("de"));
        Settings.getInstance().getAttributes()
                .set(Keys.SETTINGS_IEEE_SHAPES, false);
        firstSteps();
        hierarchicalDesign();
    }

    private static void mainScreenShot() {
        Lang.setActualRuntimeLanguage(new Language("en"));
        Settings.getInstance().getAttributes().set(Keys.SETTINGS_IEEE_SHAPES, true);
        new GuiTester("../../main/dig/processor/Processor.dig", "examples/processor/Processor.dig")
                .press(' ')
                .delay(500)
                .add(new GuiTester.WindowCheck<>(GraphicDialog.class, (gt, gd) -> {
                    graphic = gd;
                    final Main main = (Main) gd.getParent();
                    main.getCircuitComponent().requestFocus();
                }))
                .delay(500)
                .press(' ')
                .add((gt) -> graphic.dispose())
                .delay(500)
                .press("F1")
                .add(new MainScreenShot("screenshot.png"))
                .execute();
        new GuiTester()
                .press("F10")
                .press("RIGHT", 4)
                .press("DOWN", 3)
                .press("ENTER")
                .delay(500)
                .press("control typed a")
                .type("A B + B !C + A !C")
                .add(new GuiTester.SetFocusTo<>(ExpressionDialog.class,
                        comp -> comp instanceof JButton && ((JButton) comp).getText().equals(Lang.get("btn_create"))))
                .press("SPACE")
                .delay(500)
                .press("control typed -", 1)
                .add(new GuiTester.WindowCheck<>(Main.class,
                        (gt, main) -> main.getCircuitComponent().translateCircuit(-120, 0)))
                .delay(500)
                .press("F9")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(TableDialog.class, (gt, td) -> {
                    td.getContentPane().setPreferredSize(new Dimension(370, 280));
                    td.pack();
                    final Point location = td.getParent().getLocation();
                    location.x += 640;
                    location.y += 410;
                    td.setLocation(location);
                }))
                .delay(500)
                .press("F1")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(KarnaughMapDialog.class, (gt, td) -> {
                    final Point location = td.getParent().getLocation();
                    location.x -= 150;
                    location.y -= 340;
                    td.setLocation(location);
                    td.getContentPane().setPreferredSize(new Dimension(390, 300));
                    td.pack();
                }))
                .delay(500)
                .add(new MainScreenShot("screenshot2.png"))
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .execute();
    }

    // Set all settings as needed before start this method
    private static void firstSteps() {
        ScreenShot.n = 0;
        int x = 300;
        int y = 180;
        new GuiTester()
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, w) -> w.setSize(WIN_DX, WIN_DY)))
                // input
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", 2)
                .press("RIGHT")
                .press("DOWN", 2)
                .add(new ScreenShot<>(Main.class))
                .press("ENTER")
                .add(new ClickAtCircuit(x, y, InputEvent.BUTTON1_MASK))
                .add(new ScreenShot<>(Main.class))
                // input
                .press("typed l")
                .add(new ClickAtCircuit(x, y + SIZE * 2, InputEvent.BUTTON1_MASK))
                .add(new ScreenShot<>(Main.class))
                // xor
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", 1)
                .press("RIGHT")
                .press("DOWN", 4)
                .add(new ScreenShot<>(Main.class))
                .press("ENTER")
                .add(new ClickAtCircuit(x + SIZE * 5, y + SIZE, InputEvent.BUTTON1_MASK))
                .add(new ScreenShot<>(Main.class))
                // output
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", 2)
                .press("RIGHT")
                .add(new ScreenShot<>(Main.class))
                .press("ENTER")
                .add(new ClickAtCircuit(x + SIZE * 9, y + SIZE, InputEvent.BUTTON1_MASK))
                .add(new ScreenShot<>(Main.class))
                // wires
                .add(new ClickAtCircuit(x, y - SIZE, InputEvent.BUTTON1_MASK))
                .add(new ClickAtCircuit(x + SIZE * 2, y - SIZE, InputEvent.BUTTON1_MASK))
                .add(new ClickAtCircuit(x, y + SIZE, InputEvent.BUTTON1_MASK))
                .add(new ClickAtCircuit(x + SIZE * 2, y + SIZE, InputEvent.BUTTON1_MASK))
                .add(new ClickAtCircuit(x + SIZE * 5, y, InputEvent.BUTTON1_MASK))
                .add(new ClickAtCircuit(x + SIZE * 7, y, InputEvent.BUTTON1_MASK))
                .add(new ScreenShot<>(Main.class))
                // run circuit
                .press(' ')
                .add(new ScreenShot<>(Main.class))
                .add(new ClickAtCircuit(x - SIZE, y - SIZE, InputEvent.BUTTON1_MASK))
                .add(new ScreenShot<>(Main.class))
                .press(' ')
                // add labels
                .add(new ClickAtCircuit(x - SIZE, y - SIZE, InputEvent.BUTTON3_MASK))
                .press("shift typed a")
                .add(new ScreenShot<>(AttributeDialog.class).useParent())
                .press("ENTER")
                .add(new ClickAtCircuit(x - SIZE, y + SIZE, InputEvent.BUTTON3_MASK))
                .press("shift typed b", "ENTER")
                .add(new ClickAtCircuit(x + SIZE * 8, y, InputEvent.BUTTON3_MASK))
                .press("shift typed y", "ENTER")
                // analyse
                .press("F10")
                .press("RIGHT", 4)
                .press("DOWN", 1)
                .add(new ScreenShot<>(Main.class))
                .press("ENTER")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(TableDialog.class, (gt, td) -> {
                    final Point location = td.getParent().getLocation();
                    location.x += 10;
                    location.y += 10;
                    td.setLocation(location);
                    td.getContentPane().setPreferredSize(new Dimension(370, 400));
                    td.pack();
                }))
                .delay(500)
                .add(new ScreenShot<>(TableDialog.class).useParent())
                // k-map
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", 1)
                .add(new ScreenShot<>(TableDialog.class).useParent())
                .press("ENTER")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(KarnaughMapDialog.class, (gt, td) -> {
                    td.getContentPane().setPreferredSize(new Dimension(300, 300));
                    td.pack();
                }))
                .delay(500)
                .add(new ScreenShot<>(KarnaughMapDialog.class).useParent().useParent())
                .execute();
    }


    private static void hierarchicalDesign() {
        ScreenShot.n = 20;
        new GuiTester("dig/test/docu/halfAdder.dig", "halfAdder.dig")
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, w) -> w.setSize(WIN_DX, WIN_DY)))
                .press("control typed -", 4)
                .delay(500)
                .add(new ScreenShot<>(Main.class))
                .execute();
        new GuiTester("dig/test/docu/fullAdder.dig", "fullAdder.dig")
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, w) -> w.setSize(WIN_DX, WIN_DY)))
                .delay(500)
                .add(new ScreenShot<>(Main.class))
                .add(new TestInGUI.SetMouseToElement((v) -> v.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION)))
                .mouseClick(InputEvent.BUTTON3_MASK)
                .delay(500)
                .add(new GuiTester.WindowCheck<>(AttributeDialog.class, (gt, w) -> {
                    Point p = w.getLocation();
                    p.y -= 50;
                    p.x -= 70;
                    w.setLocation(p);
                }))
                .delay(500)
                .press("TAB", "SPACE")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(TestCaseDescriptionDialog.class, (gt, w) -> {
                    Point p = w.getLocation();
                    p.y -= 50;
                    p.x -= 50;
                    w.setLocation(p);
                    w.getContentPane().setPreferredSize(new Dimension(400, 300));
                    w.pack();
                }))
                .delay(500)
                .add(new ScreenShot<>(TestCaseDescriptionDialog.class).useParent().useParent())
                .add(new GuiTester.CloseTopMost())
                .add(new GuiTester.CloseTopMost())
                .press("F8")
                .add(new GuiTester.WindowCheck<>(ValueTableDialog.class, (gt, w) -> {
                    Point p = w.getLocation();
                    p.y += 90;
                    w.setLocation(p);
                    w.getContentPane().setPreferredSize(new Dimension(400, 300));
                    w.pack();
                }))
                .add(new ScreenShot<>(ValueTableDialog.class).useParent())
                .execute();
        new GuiTester("dig/test/docu/rcAdder.dig", "rcAdder.dig")
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, w) -> w.setSize(WIN_DX, WIN_DY)))
                .delay(500)
                .add(new ScreenShot<>(Main.class))
                .add(new TestInGUI.SetMouseToElement((v) -> v.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION)))
                .mouseClick(InputEvent.BUTTON3_MASK)
                .delay(500)
                .add(new GuiTester.WindowCheck<>(AttributeDialog.class, (gt, w) -> {
                    Point p = w.getLocation();
                    p.y -= 50;
                    w.setLocation(p);
                }))
                .delay(500)
                .press("TAB", "SPACE")
                .delay(500)
                .add(new GuiTester.WindowCheck<>(TestCaseDescriptionDialog.class, (gt, w) -> {
                    Point p = w.getLocation();
                    p.y -= 50;
                    w.setLocation(p);
                    w.getContentPane().setPreferredSize(new Dimension(400, 300));
                    w.pack();
                }))
                .delay(500)
                .add(new ScreenShot<>(TestCaseDescriptionDialog.class).useParent().useParent())
                .execute();
    }


    private static class MainScreenShot implements GuiTester.Runnable {

        private final String name;

        MainScreenShot(String name) {
            this.name = name;
        }

        @Override
        public void run(GuiTester guiTester) throws Exception {
            Window main = FocusManager.getCurrentManager().getActiveWindow();
            while (!(main instanceof Main)) {
                main = (Window) main.getParent();
                if (main == null)
                    throw new RuntimeException("Main not found!");
            }

            BufferedImage image = guiTester.getRobot().createScreenCapture(main.getBounds());
            File file = new File(Resources.getRoot().getParentFile().getParentFile().getParentFile(), name);
            ImageIO.write(image, "png", file);
        }
    }

    private static class ScreenShot<W extends Window> extends GuiTester.WindowCheck<W> {
        private static int n;
        private int parentCount;

        ScreenShot(Class<W> expectedClass) {
            super(expectedClass);
            parentCount = 0;
        }

        @Override
        public void checkWindow(GuiTester guiTester, W window) throws Exception {
            Window win = window;
            for (int i = 0; i < parentCount; i++)
                win = (Window) win.getParent();

            BufferedImage image = guiTester.getRobot().createScreenCapture(win.getBounds());
            String str = Integer.toString(n);
            if (str.length() == 1)
                str = '0' + str;
            File file = new File(Resources.getRoot(), "docu/images/" + Lang.currentLanguage().getName() + "/scr" + str + ".png");
            ImageIO.write(image, "png", file);
            n++;
        }

        ScreenShot<W> useParent() {
            parentCount++;
            return this;
        }
    }

    private static class ClickAtCircuit extends GuiTester.WindowCheck<Main> {
        private final int x;
        private final int y;
        private final int button;

        ClickAtCircuit(int x, int y, int button) {
            super(Main.class);
            this.x = x;
            this.y = y;
            this.button = button;
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) throws Exception {
            Point p = new Point(x, y);
            SwingUtilities.convertPointToScreen(p, main.getCircuitComponent());
            guiTester.getRobot().mouseMove(p.x, p.y);
            if (button != 0) {
                guiTester.mouseClickNow(button);
            }
        }
    }
}
