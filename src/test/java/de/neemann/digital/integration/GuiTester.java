package de.neemann.digital.integration;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.gui.Main;
import junit.framework.Assert;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GuiTester {
    private static final long SLEEP_TIME = 200;
    private final ArrayList<Runnable> runnableList;
    private Main main;
    private Robot robot;
    private String filename;

    public GuiTester() {
        this(null);
    }

    public GuiTester(String filename) {
        this.filename = filename;
        runnableList = new ArrayList<>();
    }

    public GuiTester add(Runnable runnable) {
        runnableList.add(runnable);
        return this;
    }

    public GuiTester delay(int ms) {
        add(() -> Thread.sleep(ms));
        return this;
    }

    public GuiTester press(String key, int n) {
        final KeyStroke keyStroke = KeyStroke.getKeyStroke(key);
        Assert.assertNotNull("invalid key code <" + key + ">", keyStroke);
        int code = keyStroke.getKeyCode();
        add((() -> {
            for (int i = 0; i < n; i++) {
                robot.keyPress(code);
                robot.keyRelease(code);
            }
        }));
        return this;
    }

    public GuiTester press(String key) {
        final KeyStroke keyStroke = KeyStroke.getKeyStroke(key);
        Assert.assertNotNull("invalid key code <" + key + ">", keyStroke);
        return addCode(keyStroke.getKeyCode());
    }

    public GuiTester press(char c) {
        return addCode(KeyEvent.getExtendedKeyCodeForChar(c));
    }

    public GuiTester pressCTRL(char c) {
        int code = KeyEvent.getExtendedKeyCodeForChar(c);
        Assert.assertTrue("keycode 0 is not allowed", code != 0);
        add(() -> {
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(code);
            robot.keyRelease(code);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        });
        return this;
    }

    public GuiTester typeTempFile(String name) throws IOException {
        File f = File.createTempFile(name, ".dig");
        return type(f.getPath());
    }

    public GuiTester type(String s) {
        add((() -> {
            for (int i = 0; i < s.length(); i++) {
                final char ch = s.charAt(i);
                int code = KeyEvent.getExtendedKeyCodeForChar(ch);
                if (ch == '/') {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(code);
                    robot.keyRelease(code);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } else {
                    robot.keyPress(code);
                    robot.keyRelease(code);
                }
            }
        }));
        return this;
    }

    private GuiTester addCode(int code) {
        Assert.assertTrue("keycode 0 is not allowed", code != 0);
        add(() -> {
            robot.keyPress(code);
            robot.keyRelease(code);
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
            SwingUtilities.invokeAndWait(() -> {
                if (filename != null) {
                    File file = new File(Resources.getRoot(), filename);
                    main = new Main.MainBuilder().setFileToOpen(file).build();
                } else
                    main = new Main.MainBuilder().setCircuit(new Circuit()).build();
                main.setVisible(true);
            });
            Thread.sleep(500);
            try {
                robot = new Robot();
                robot.setAutoWaitForIdle(true);
                int step = 0;
                for (Runnable r : runnableList) {
                    if (step > 0) {
                        System.err.print("-");
                        Thread.sleep(SLEEP_TIME);
                    }
                    step++;
                    System.err.print(step);
                    r.run();
                }
            } finally {
                SwingUtilities.invokeAndWait(() -> main.dispose());
            }
            System.err.println();
        }
    }

    interface Runnable {
        void run() throws Exception;
    }

    static class WindowCheck<W extends Window> implements Runnable {
        private final Class<W> clazz;

        public WindowCheck(Class<W> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void run() throws Exception {
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            if (activeWindow == null || !clazz.isAssignableFrom(activeWindow.getClass())) {
                Thread.sleep(500);
                activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            }
            Assert.assertNotNull("no java window on top!", activeWindow);

            Assert.assertTrue(getClass().getSimpleName()
                            + ": wrong dialog on top! expected: <"
                            + clazz.getSimpleName()
                            + "> but was: <"
                            + activeWindow.getClass().getSimpleName()
                            + ">",
                    clazz.isAssignableFrom(activeWindow.getClass()));
            checkWindow((W) activeWindow);
        }

        public void checkWindow(W window) {
        }
    }

    public static class CheckDialogText extends WindowCheck<JDialog> {
        private final String[] expected;

        public CheckDialogText(String... expected) {
            super(JDialog.class);
            this.expected = expected;
        }

        @Override
        public void checkWindow(JDialog dialog) {
            StringBuilder text = new StringBuilder();
            collectText(dialog.getContentPane(), text);
            String t = text.toString();
            for (String e : expected)
                Assert.assertTrue(t + " does not contain " + e, t.contains(e));
        }

        void collectText(Container cp, StringBuilder text) {
            for (int i = 0; i < cp.getComponentCount(); i++) {
                Component component = cp.getComponent(i);
                if (component instanceof JLabel) {
                    text.append(((JLabel) component).getText());
                } else if (component instanceof JTextComponent) {
                    text.append((((JTextComponent) component).getText()));
                } else if (component instanceof Container) {
                    collectText((Container) component, text);
                }
            }
        }
    }

    public static class CloseTopMost implements Runnable {
        @Override
        public void run() {
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            Assert.assertNotNull("no java window on top!", activeWindow);
            activeWindow.dispose();
        }
    }
}
