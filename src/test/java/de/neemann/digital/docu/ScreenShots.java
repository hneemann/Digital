package de.neemann.digital.docu;

import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.AttributeDialog;
import de.neemann.digital.gui.components.graphics.GraphicDialog;
import de.neemann.digital.gui.components.karnaugh.KarnaughMapDialog;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.gui.components.testing.TestCaseDescriptionDialog;
import de.neemann.digital.integration.GuiTester;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.language.Language;

import javax.imageio.ImageIO;
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
//        mainScreenShot();
//        firstSteps();
//        hierarchicalDesign();
    }

    private static void mainScreenShot() {
        Lang.setActualRuntimeLanguage(new Language("en"));
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
                .add(new MainScreenShot())
                .execute();
    }

    // Set all settings as needed before start this method
    private static void firstSteps() {
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
                .add(new ScreenShot<>(Main.class))
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
                .execute();
        new GuiTester("dig/test/docu/rcAdder.dig", "rcAdder.dig")
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, w) -> w.setSize(WIN_DX, WIN_DY)))
                .delay(500)
                .add(new ScreenShot<>(Main.class))
                .add(new ClickAtCircuit(600, 380, InputEvent.BUTTON3_MASK))
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


    private static class MainScreenShot extends GuiTester.WindowCheck<Main> {

        MainScreenShot() {
            super(Main.class);
        }

        @Override
        public void checkWindow(GuiTester guiTester, Main main) throws Exception {
            BufferedImage image = guiTester.getRobot().createScreenCapture(main.getBounds());
            File file = new File(Resources.getRoot(), "../../../screenshot.png");
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
