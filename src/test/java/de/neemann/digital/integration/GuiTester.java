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

import static java.awt.event.InputEvent.*;

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
        add((GuiTester guiTester) -> Thread.sleep(ms));
        return this;
    }

    public GuiTester press(String key, int n) {
        KeyStroke stroke = strokeFromString(key);
        add(((GuiTester guiTester) -> {
            for (int i = 0; i < n; i++)
                pressNow(stroke);
        }));
        return this;
    }

    public GuiTester press(String key) {
        return addStroke(strokeFromString(key));
    }

    public GuiTester press(char c) {
        return addStroke(KeyStroke.getKeyStroke(c));
    }

    public GuiTester typeTempFile(String name) throws IOException {
        File f = File.createTempFile(name, ".dig");
        return type(f.getPath());
    }

    public GuiTester type(String s) {
        add(((GuiTester guiTester) -> {
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

    private GuiTester addStroke(KeyStroke stroke) {
        Assert.assertNotNull("key stroke null is not allowed", stroke);
        add((GuiTester guiTester) -> {
            pressNow(stroke);
        });
        return this;
    }

    public GuiTester ask(String question) {
        add((gt) -> {
            JOptionPane pane = new JOptionPane(question, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
            pane.setBackground(Color.RED.brighter());
            JDialog dialog = pane.createDialog("Testing-Question");
            dialog.setVisible(true);
            Object val = pane.getValue();
            dialog.dispose();

            Assert.assertTrue("user recognized fail!", (Integer) val == JOptionPane.YES_OPTION);
            Thread.sleep(500);
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
                    r.run(this);
                }
            } finally {
                SwingUtilities.invokeAndWait(() -> main.dispose());
            }
            System.err.println();
        }
    }

    private KeyStroke strokeFromString(String key) {
        final KeyStroke keyStroke = KeyStroke.getKeyStroke(key);
        Assert.assertNotNull("invalid key code <" + key + ">", keyStroke);
        return keyStroke;
    }

    public void pressNow(String key) {
        pressNow(strokeFromString(key));
    }

    public void pressNow(KeyStroke stroke) {
        int mod = stroke.getModifiers();
        if ((mod & SHIFT_DOWN_MASK) != 0) robot.keyPress(KeyEvent.VK_SHIFT);
        if ((mod & CTRL_DOWN_MASK) != 0) robot.keyPress(KeyEvent.VK_CONTROL);
        if ((mod & ALT_DOWN_MASK) != 0) robot.keyPress(KeyEvent.VK_ALT);
        int keyCode = stroke.getKeyCode();
        if (keyCode == 0) keyCode = KeyEvent.getExtendedKeyCodeForChar(stroke.getKeyChar());
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
        if ((mod & SHIFT_DOWN_MASK) != 0) robot.keyRelease(KeyEvent.VK_SHIFT);
        if ((mod & CTRL_DOWN_MASK) != 0) robot.keyRelease(KeyEvent.VK_CONTROL);
        if ((mod & ALT_DOWN_MASK) != 0) robot.keyRelease(KeyEvent.VK_ALT);
    }

    interface Runnable {
        void run(GuiTester guiTester) throws Exception;
    }

    static class WindowCheck<W extends Window> implements Runnable {
        private final Class<W> clazz;

        public WindowCheck(Class<W> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void run(GuiTester guiTester) throws Exception {
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
        public void run(GuiTester guiTester) {
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            Assert.assertNotNull("no java window on top!", activeWindow);
            activeWindow.dispose();
        }
    }
}
