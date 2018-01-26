package de.neemann.digital.integration;

import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

public class ScreenShots {

    public static void main(String[] args) {
        int x = 300;
        int y = 200;
        new GuiTester()
                .add(new GuiTester.WindowCheck<>(Main.class, (gt, w) -> w.setSize(850, 500)))
                .add(new ScreenShot<>(Main.class))
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", 2)
                .press("RIGHT")
                .press("DOWN", 2)
                .add(new ScreenShot<>(Main.class))
                .press("ENTER")
                .mouseMove(x, y)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new ScreenShot<>(Main.class))
                .press("typed l")
                .mouseMove(x, y + SIZE * 2)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new ScreenShot<>(Main.class))
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", 1)
                .press("RIGHT")
                .press("DOWN", 4)
                .add(new ScreenShot<>(Main.class))
                .press("ENTER")
                .mouseMove(x + SIZE * 5, y + SIZE * 1 + SIZE2)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new ScreenShot<>(Main.class))
                .press("F10")
                .press("RIGHT", 5)
                .press("DOWN", 2)
                .press("RIGHT")
                //.press("DOWN", 4)
                .add(new ScreenShot<>(Main.class))
                .press("ENTER")
                .mouseMove(x + SIZE * 9, y + SIZE * 1 + SIZE2)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new ScreenShot<>(Main.class))
                .mouseMove(x, y - SIZE)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .mouseMove(x + SIZE * 2, y - SIZE)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new ScreenShot<>(Main.class))
                .mouseMove(x, y + SIZE)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .mouseMove(x + SIZE * 2, y + SIZE)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .mouseMove(x + SIZE * 5, y)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .mouseMove(x + SIZE * 7, y)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new ScreenShot<>(Main.class))
                .press(' ')
                .add(new ScreenShot<>(Main.class))
                .mouseMove(x - SIZE, y)
                .mouseClick(InputEvent.BUTTON1_MASK)
                .add(new ScreenShot<>(Main.class))
                .execute();
    }

    private static class ScreenShot<W extends Window> extends GuiTester.WindowCheck<W> {
        private static int n;

        public ScreenShot(Class<W> expectedClass) {
            super(expectedClass);
        }

        @Override
        public void checkWindow(GuiTester guiTester, W window) throws Exception {
            BufferedImage image = guiTester.getRobot().createScreenCapture(window.getBounds());
            String str = Integer.toString(n);
            if (str.length() == 1)
                str = '0' + str;
            File file = new File(Resources.getRoot(), "docu/images/"+ Lang.currentLanguage().getName()+"/scr" + str + ".png");
            ImageIO.write(image, "png", file);
            n++;
        }
    }
}
