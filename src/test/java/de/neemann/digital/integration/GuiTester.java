/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.gui.FileHistory;
import de.neemann.digital.gui.Main;
import junit.framework.Assert;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.awt.event.InputEvent.*;
import static org.junit.Assert.*;

public class GuiTester {
    private static final long SLEEP_TIME = 200;
    private final ArrayList<Runnable> runnableList;
    private Main main;
    private String filename;
    private final String displayName;
    private Robot robot;

    public GuiTester() {
        this(null, null);
    }

    public GuiTester(String filename) {
        this(filename, null);
    }

    public GuiTester(String filename, String displayName) {
        this.filename = filename;
        this.displayName = displayName;
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
            for (int i = 0; i < n; i++) {
                Thread.sleep(100);
                gt.typeNow(stroke);
            }
        }));
        return this;
    }

    public GuiTester press(String... keys) {
        add((gt) -> {
            for (String key : keys) {
                Thread.sleep(100);
                gt.typeNow(strokeFromString(key));
            }
        });
        return this;
    }

    public GuiTester press(char c) {
        return addStroke(KeyStroke.getKeyStroke(c));
    }

    public GuiTester typeTempFile(String name) {
        File f = null;
        try {
            f = File.createTempFile(name, ".dig");
            f.delete();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return type(f.getPath().replace('\\', '/'));
    }

    public GuiTester type(String s) {
        add(((gt) -> {
            for (int i = 0; i < s.length(); i++) {
                final char ch = s.charAt(i);
                switch (ch) {
                    case ':':
                        gt.keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD);
                        break;
                    case '/':
                        gt.keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_7);
                        break;
                    case '!':
                        gt.keyType(KeyEvent.VK_SHIFT, KeyEvent.VK_1);
                        break;
                    case '~':
                        gt.keyType(KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_PLUS);
                        break;
                    default:
                        if (Character.isUpperCase(ch))
                            gt.keyType(KeyEvent.VK_SHIFT, KeyEvent.getExtendedKeyCodeForChar(Character.toLowerCase(ch)));
                        else
                            gt.keyType(KeyEvent.getExtendedKeyCodeForChar(ch));
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

    public GuiTester mouseMove(int x, int y) {
        add((gs) -> gs.mouseMoveNow(x, y));
        return this;
    }

    public GuiTester mouseClick(int buttons) {
        add((gs) -> gs.mouseClickNow(buttons));
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
            FileHistory.setGuiTest();
            try {
                SwingUtilities.invokeAndWait(() -> {
                    if (filename != null) {
                        File file = new File(Resources.getRoot(), filename);
                        main = new Main.MainBuilder().setFileToOpen(file).build();
                        if (displayName != null)
                            SwingUtilities.invokeLater(() -> main.setTitle(displayName + " - Digital"));
                    } else
                        main = new Main.MainBuilder().setCircuit(new Circuit()).build();
                    main.setSize(1024, 768);
                    main.setLocationRelativeTo(null);
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
        try {
            int keyCode = stroke.getKeyCode();
            if (keyCode == 0) keyCode = KeyEvent.getExtendedKeyCodeForChar(stroke.getKeyChar());
            keyType(keyCode);
        } finally {
            if ((mod & SHIFT_DOWN_MASK) != 0) keyRelease(KeyEvent.VK_SHIFT);
            if ((mod & CTRL_DOWN_MASK) != 0) keyRelease(KeyEvent.VK_CONTROL);
            if ((mod & ALT_DOWN_MASK) != 0) keyRelease(KeyEvent.VK_ALT);
        }
    }

    public void mouseClickNow(int x, int y, int buttons) throws InterruptedException {
        mouseMoveNow(x, y);
        mouseClickNow(buttons);
    }

    /**
     * Clicks the mouse
     *
     * @param buttons the button mask
     */
    public void mouseClickNow(int buttons) throws InterruptedException {
        mousePressNow(buttons);
        Thread.sleep(100);
        mouseReleaseNow(buttons);
    }

    public void mouseReleaseNow(int buttons) {
        robot.mouseRelease(buttons);
    }

    public void mousePressNow(int buttons) {
        robot.mousePress(buttons);
    }

    /**
     * Moves the mouse
     *
     * @param x the x coordinate relative to the topmost window
     * @param y the x coordinate relative to the topmost window
     */
    public void mouseMoveNow(int x, int y) {
        Container baseContainer = getBaseContainer();

        Point p = new Point(x, y);
        SwingUtilities.convertPointToScreen(p, baseContainer);
        robot.mouseMove(p.x, p.y);
    }

    public static Container getBaseContainer() {
        Container baseContainer = FocusManager.getCurrentManager().getActiveWindow();
        if (baseContainer instanceof JDialog)
            baseContainer = ((JDialog) baseContainer).getContentPane();
        else if (baseContainer instanceof JFrame)
            baseContainer = ((JFrame) baseContainer).getContentPane();
        return baseContainer;
    }


    private void keyType(int code1, int code2) {
        robot.keyPress(code1);
        try {
            robot.keyPress(code2);
            robot.keyRelease(code2);
        } finally {
            robot.keyRelease(code1);
        }
    }

    private void keyType(int code1, int code2, int code3) {
        robot.keyPress(code1);
        try {
            keyType(code2, code3);
        } finally {
            robot.keyRelease(code1);
        }
    }

    private void keyType(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    private void keyPress(int keyCode) {
        robot.keyPress(keyCode);
    }

    private void keyRelease(int keyCode) {
        robot.keyRelease(keyCode);
    }

    public Robot getRobot() {
        return robot;
    }

    /**
     * Every test step implements this Runnable
     */
    public interface Runnable {
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
        private final WindowCheckInterface<W> windowCheckInterface;

        /**
         * Creates a new instance
         *
         * @param expectedClass the expected window class
         */
        public WindowCheck(Class<W> expectedClass) {
            this(expectedClass, null);
        }

        /**
         * Creates a new instance
         *
         * @param expectedClass the expected window class
         */
        public WindowCheck(Class<W> expectedClass, WindowCheckInterface<W> windowCheckInterface) {
            this.expectedClass = expectedClass;
            this.windowCheckInterface = windowCheckInterface;
        }

        @Override
        public void run(GuiTester guiTester) throws Exception {
            Window activeWindow = FocusManager.getCurrentManager().getActiveWindow();
            if (activeWindow == null || !expectedClass.isAssignableFrom(activeWindow.getClass())) {
                Thread.sleep(1000);
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
            try {
                checkWindow(guiTester, (W) activeWindow);
            } catch (Exception e) {
                Thread.sleep(1000);
                checkWindow(guiTester, (W) activeWindow);
            }
        }

        /**
         * Is called if the expected window was found.
         * Override this method to implement own tests of the window found.
         *
         * @param guiTester the GuiTester
         * @param window    the found window of expected type
         */
        public void checkWindow(GuiTester guiTester, W window) throws Exception {
            if (windowCheckInterface != null)
                windowCheckInterface.checkWindow(guiTester, window);
        }
    }

    public interface WindowCheckInterface<W extends Window> {
        void checkWindow(GuiTester guiTester, W window) throws Exception;
    }

    /**
     * Traverses all the components in the topmost window.
     */
    public static abstract class ComponentTraverse<W extends Window> extends WindowCheck<W> {

        private boolean found;
        private W dialog;

        /**
         * creates a new instance
         */
        public ComponentTraverse(Class<W> expected) {
            super(expected);
        }

        @Override
        public void checkWindow(GuiTester guiTester, W dialog) throws Exception {
            this.dialog = dialog;
            traverseComponents(dialog);
            assertTrue("no component found", found);
        }

        void traverseComponents(Container cp) throws Exception {
            for (int i = 0; i < cp.getComponentCount(); i++) {
                Component component = cp.getComponent(i);
                visit(component);
                if (component instanceof Container) {
                    traverseComponents((Container) component);
                }
            }
        }

        void found() {
            found = true;
        }

        public W getWindow() {
            return dialog;
        }

        public abstract void visit(Component component) throws Exception;
    }

    public static class SetFocusTo<W extends Window> extends ComponentTraverse<W> {

        private final ComponentFilter filter;

        /**
         * creates a new instance
         *
         * @param expected the expected window
         * @param filter   filter
         */
        public SetFocusTo(Class<W> expected, ComponentFilter filter) {
            super(expected);
            this.filter = filter;
        }

        @Override
        public void visit(Component component) {
            if (filter.accept(component)) {
                component.requestFocus();
                found();
            }
        }
    }

    /**
     * filters a certain component
     */
    public interface ComponentFilter {
        boolean accept(Component component);
    }

    public final static class WaitFor implements Runnable {
        private Condition cond;

        public WaitFor(Condition cond) {
            this.cond = cond;
        }

        @Override
        public void run(GuiTester guiTester) throws Exception {
            int n = 0;
            while (!cond.proceed()) {
                Thread.sleep(100);
                if (n++ > 20)
                    throw new RuntimeException("time out!");
            }
        }
    }

    public interface Condition {
        boolean proceed() throws Exception;
    }

    /**
     * Checks if the topmost dialog contains the given strings.
     */
    public static class CheckTextInWindow<W extends Window> extends ComponentTraverse<W> {
        private final String[] expected;
        private StringBuilder text;

        /**
         * Checks if the topmost dialog contains the given strings.
         *
         * @param expected test fails if one of the strings is missing
         */
        public CheckTextInWindow(Class<W> expectedClass, String... expected) {
            super(expectedClass);
            this.expected = expected;
            text = new StringBuilder();
        }

        @Override
        public void checkWindow(GuiTester guiTester, W window) throws Exception {
            super.checkWindow(guiTester, window);
            String t = text.toString();
            for (String e : expected)
                Assert.assertTrue("<" + t + "> does not contain <" + e + ">", t.contains(e));
        }

        @Override
        public void visit(Component component) {
            if (component instanceof JTabbedPane) {
                JTabbedPane t = ((JTabbedPane) component);
                for (int j = 0; j < t.getTabCount(); j++)
                    text.append(t.getTitleAt(j));
                found();
            } else if (component instanceof JLabel) {
                text.append(((JLabel) component).getText());
                found();
            } else if (component instanceof JTextComponent) {
                text.append((((JTextComponent) component).getText()));
                found();
            }
        }
    }

    public static class CheckTableRows<W extends Window> extends ComponentTraverse<W> {
        private final int expectedRows;
        int rows;
        int tableCount;

        public CheckTableRows(Class<W> expectedClass, int expectedRows) {
            super(expectedClass);
            this.expectedRows = expectedRows;
        }

        @Override
        public void checkWindow(GuiTester guiTester, W window) throws Exception {
            super.checkWindow(guiTester, window);
            assertEquals("only one table allowed", 1, tableCount);
            assertEquals("row count does not match", expectedRows, rows);
        }

        @Override
        public void visit(Component component) {
            if (component instanceof JTable) {
                rows = ((JTable) component).getModel().getRowCount();
                tableCount++;
                found();
            }
        }
    }

    public static class CheckListRows<W extends Window> extends CheckTableRows<W> {

        public CheckListRows(Class<W> expectedClass, int expectedRows) {
            super(expectedClass, expectedRows);
        }

        @Override
        public void visit(Component component) {
            if (component instanceof JList) {
                rows = ((JList) component).getModel().getSize();
                tableCount++;
                found();
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

    /**
     * Checks a color in a window
     */
    public static class ColorPicker implements Runnable {
        private final Class<? extends Component> target;
        private final int x;
        private final int y;
        private final ColorCheckInterface cpi;


        /**
         * Create a new color picker
         *
         * @param x             x coordinate.
         * @param y             y coordinate.
         * @param expectedColor the expected color
         */
        public ColorPicker(Class<? extends Component> target, int x, int y, Color expectedColor) {
            this(target, x, y, (c) -> {
                boolean ok = (Math.abs(expectedColor.getRed() - c.getRed()) < 5)
                        && (Math.abs(expectedColor.getGreen() - c.getGreen()) < 5)
                        && (Math.abs(expectedColor.getBlue() - c.getBlue()) < 5);
                assertTrue("expected:<" + expectedColor + "> but was:<" + c + ">", ok);
            });
        }

        /**
         * Create a new color picker
         *
         * @param x   x coordinate.
         * @param y   y coordinate.
         * @param cpi the color test
         */
        public ColorPicker(Class<? extends Component> target, int x, int y, ColorCheckInterface cpi) {
            this.target = target;
            this.x = x;
            this.y = y;
            this.cpi = cpi;
        }

        @Override
        public void run(GuiTester guiTester) throws Exception {
            Point p = new Point(x, y);

            Component t = searchComponent(FocusManager.getCurrentManager().getActiveWindow(), target);
            if (t == null)
                throw new RuntimeException("Component " + target.getSimpleName() + " not found!");

            SwingUtilities.convertPointToScreen(p, t);
            Thread.sleep(100);
            guiTester.getRobot().mouseMove(p.x, p.y);
            Thread.sleep(100);
            cpi.checkColor(guiTester.getRobot().getPixelColor(p.x, p.y));
        }
    }

    private static Component searchComponent(Component c, Class<? extends Component> target) {
        if (c.getClass() == target)
            return c;
        if (c instanceof Container) {
            Container con = (Container) c;
            for (int i = 0; i < con.getComponentCount(); i++) {
                Component s = searchComponent(con.getComponent(i), target);
                if (s != null)
                    return s;
            }
        }
        return null;
    }

    public interface ColorCheckInterface {
        void checkColor(Color pixelColor);
    }

    /**
     * Helper to create a ColorPicker
     */
    public static class ColorPickerCreator extends JDialog implements Runnable {
        private final JLabel label;
        private final Class<? extends Component> target;

        /**
         * Creates a new instance
         */
        public ColorPickerCreator(Class<? extends Component> target) {
            super(null, "Position picker", ModalityType.APPLICATION_MODAL);
            this.target = target;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            label = new JLabel("<html>Move mouse to pos<br>and press ENTER</html>");
            label.setFocusable(true);
            getContentPane().add(label);
            pack();
            setLocationRelativeTo(null);
        }

        @Override
        public void run(GuiTester gt) throws Exception {
            Component baseContainer = searchComponent(FocusManager.getCurrentManager().getActiveWindow(), target);
            if (baseContainer == null)
                throw new RuntimeException("Component " + target.getSimpleName() + " not found!");

            label.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                        PointerInfo inf = MouseInfo.getPointerInfo();
                        Point p = inf.getLocation();
                        Color col = gt.getRobot().getPixelColor(p.x, p.y);
                        SwingUtilities.convertPointFromScreen(p, baseContainer);
                        System.out.print(".add(new GuiTester.ColorPicker(");
                        System.out.print(target.getSimpleName());
                        System.out.print(".class, ");
                        System.out.print(p.x + ", " + p.y);
                        System.out.print(", new Color(");
                        System.out.print(col.getRed() + "," + col.getGreen() + "," + col.getBlue());
                        System.out.println(")))");
                    } else if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                        System.exit(1);
                }
            });
            setVisible(true);
        }
    }

}
