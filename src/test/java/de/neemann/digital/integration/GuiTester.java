package de.neemann.digital.integration;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.gui.Main;
import junit.framework.Assert;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.awt.event.InputEvent.*;
import static org.junit.Assert.fail;

public class GuiTester {
    private static final long SLEEP_TIME = 200;
    private final ArrayList<Runnable> runnableList;
    private Main main;
    private String filename;
    private Robot robot;

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

    public GuiTester use(GuiTester tester) {
        runnableList.addAll(tester.runnableList);
        return this;
    }

    public GuiTester delay(int ms) {
        add((gt) -> Thread.sleep(ms));
        return this;
    }

    public GuiTester press(String key, int n) {
        KeyStroke stroke = strokeFromString(key);
        add(((gt) -> {
            for (int i = 0; i < n; i++)
                gt.typeNow(stroke);
        }));
        return this;
    }

    public GuiTester press(String key) {
        return addStroke(strokeFromString(key));
    }

    public GuiTester press(char c) {
        return addStroke(KeyStroke.getKeyStroke(c));
    }

    public GuiTester typeTempFile(String name) {
        File f = null;
        try {
            f = File.createTempFile(name, ".dig");
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return type(f.getPath());
    }

    public GuiTester type(String s) {
        add(((gt) -> {
            for (int i = 0; i < s.length(); i++) {
                final char ch = s.charAt(i);
                int code = KeyEvent.getExtendedKeyCodeForChar(ch);
                if (ch == '/') {
                    gt.keyPress(KeyEvent.VK_SHIFT);
                    gt.keyPress(code);
                    gt.keyRelease(code);
                    gt.keyRelease(KeyEvent.VK_SHIFT);
                } else {
                    gt.keyPress(code);
                    gt.keyRelease(code);
                }
            }
        }));
        return this;
    }

    private GuiTester addStroke(KeyStroke stroke) {
        Assert.assertNotNull("key stroke null is not allowed", stroke);
        add((gt) -> gt.typeNow(stroke));
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

    /**
     * executes the test
     */
    public void execute() {
        if (isDisplay()) {
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        }
    }

    private static KeyStroke strokeFromString(String key) {
        final KeyStroke keyStroke = KeyStroke.getKeyStroke(key);
        Assert.assertNotNull("invalid key code <" + key + ">", keyStroke);
        return keyStroke;
    }

    /**
     * Types the given key
     *
     * @param key the key to type
     */
    public void typeNow(String key) {
        typeNow(strokeFromString(key));
    }

    /**
     * Types the given key
     *
     * @param stroke the key to type
     */
    public void typeNow(KeyStroke stroke) {
        int mod = stroke.getModifiers();
        if ((mod & SHIFT_DOWN_MASK) != 0) keyPress(KeyEvent.VK_SHIFT);
        if ((mod & CTRL_DOWN_MASK) != 0) keyPress(KeyEvent.VK_CONTROL);
        if ((mod & ALT_DOWN_MASK) != 0) keyPress(KeyEvent.VK_ALT);
        int keyCode = stroke.getKeyCode();
        if (keyCode == 0) keyCode = KeyEvent.getExtendedKeyCodeForChar(stroke.getKeyChar());
        keyPress(keyCode);
        keyRelease(keyCode);
        if ((mod & SHIFT_DOWN_MASK) != 0) keyRelease(KeyEvent.VK_SHIFT);
        if ((mod & CTRL_DOWN_MASK) != 0) keyRelease(KeyEvent.VK_CONTROL);
        if ((mod & ALT_DOWN_MASK) != 0) keyRelease(KeyEvent.VK_ALT);
    }

    private void keyPress(int keyCode) {
        robot.keyPress(keyCode);
    }

    private void keyRelease(int keyCode) {
        robot.keyRelease(keyCode);
    }

    /**
     * Every test step implements this Runnable
     */
    interface Runnable {
        /**
         * Executed the test setp
         *
         * @param guiTester the gui tester
         * @throws Exception Exception
         */
        void run(GuiTester guiTester) throws Exception;
    }

    /**
     * Checks if the topmost window is a instance of <W>.
     *
     * @param <W> the type of the expected window class
     */
    public static class WindowCheck<W extends Window> implements Runnable {
        private final Class<W> expectedClass;

        /**
         * Creates a new instance
         *
         * @param expectedClass the expected window class
         */
        public WindowCheck(Class<W> expectedClass) {
            this.expectedClass = expectedClass;
        }

        @Override
        public void run(GuiTester guiTester) throws Exception {
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            if (activeWindow == null || !expectedClass.isAssignableFrom(activeWindow.getClass())) {
                Thread.sleep(500);
                activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            }
            Assert.assertNotNull("no java window on top!", activeWindow);

            Assert.assertTrue(getClass().getSimpleName()
                            + ": wrong dialog on top! expected: <"
                            + expectedClass.getSimpleName()
                            + "> but was: <"
                            + activeWindow.getClass().getSimpleName()
                            + ">",
                    expectedClass.isAssignableFrom(activeWindow.getClass()));
            checkWindow((W) activeWindow);
        }

        /**
         * Is called if the expected window was found.
         * Override this method to implement own tests of the window found.
         *
         * @param window the found window of expected type
         */
        public void checkWindow(W window) {
        }
    }

    /**
     * Checks if the topmost dialog contains the given strings.
     */
    public static class CheckDialogText extends WindowCheck<JDialog> {
        private final String[] expected;

        /**
         * Checks if the topmost dialog contains the given strings.
         *
         * @param expected test fails if one of the strings is missing
         */
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

    /**
     * Closes the topmost window
     */
    public static class CloseTopMost implements Runnable {
        @Override
        public void run(GuiTester guiTester) {
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            Assert.assertNotNull("no java window on top!", activeWindow);
            activeWindow.dispose();
        }
    }
}
