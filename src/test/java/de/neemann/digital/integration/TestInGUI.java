package de.neemann.digital.integration;

import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

public class TestInGUI extends TestCase {

    private Main main;

    public void testErrorAtStart1() {
        check("dig/manualError/01_fastRuntime.dig",
                new KeySeries().add(' '),
                new CheckErrorMessage("01_fastRuntime.dig", Lang.get("err_burnError"))
        );
    }

    public void testErrorAtStart2() {
        check("dig/manualError/02_fastRuntimeEmbed.dig",
                new KeySeries().add(' '),
                new CheckErrorMessage("short.dig", Lang.get("err_burnError"))
        );
    }

    public void testErrorAtStart3() {
        check("dig/manualError/06_initPhase.dig",
                new KeySeries().add(' '),
                new CheckErrorMessage("06_initPhase.dig", Lang.get("err_burnError"))
        );
    }

    public void testErrorAtStart4() {
        check("dig/manualError/07_creationPhase.dig",
                new KeySeries().add(' '),
                new CheckErrorMessage("07_creationPhase.dig", "ErrorY")
        );
    }

    public void testErrorAtStart5() {
        check("dig/manualError/08_twoFastClocks.dig",
                new KeySeries().add(' '),
                new CheckErrorMessage(Lang.get("err_moreThanOneFastClock")));
    }

    public void testErrorAtTestExecution() {
        check("dig/manualError/04_testExecution.dig",
                new KeySeries().add("F8"),
                new CheckErrorMessage("04_testExecution.dig", Lang.get("err_burnError")));
    }

    public void testErrorAtRunToBreak() {
        check("dig/manualError/05_runToBreak.dig",
                new KeySeries().add(' ').delay(500).add("F7"),
                new CheckErrorMessage("05_runToBreak.dig", Lang.get("err_burnError")));
    }

    public void testErrorAtButtonPress() {
        check("dig/manualError/03_fastRuntimeButton.dig",
                new KeySeries().add(' ').delay(500).add("A"),
                new CheckErrorMessage("03_fastRuntimeButton.dig", Lang.get("err_burnError")));
    }

    private boolean isDisplay() {
        final boolean isDisplay = !GraphicsEnvironment.isHeadless();
        if (!isDisplay)
            System.err.println("runs headless, skip test!");
        return isDisplay;
    }

    private void check(String filename, KeySeries keySeries, Check... checks) {
        if (isDisplay()) {
            File file = new File(Resources.getRoot(), filename);
            try {
                SwingUtilities.invokeAndWait(() -> {
                    main = new Main.MainBuilder().setFileToOpen(file).build();
                    main.setVisible(true);
                });
                Thread.sleep(500);
                try {
                    keySeries.execute();

                    Thread.sleep(500);

                    for (Check c : checks)
                        c.check();
                } finally {
                    SwingUtilities.invokeAndWait(() -> main.dispose());
                }
            } catch (Exception e) {
                System.err.println("error in file " + filename);
                e.printStackTrace();
                fail();
            }
        }
    }

    public static class KeySeries {
        private final ArrayList<Runnable> runnables;
        private Robot robot;

        public KeySeries() {
            runnables = new ArrayList<>();
        }

        public void execute() throws Exception {
            robot = new Robot();
            for (Runnable r : runnables)
                r.run();
        }

        public void add(Runnable runnable) {
            runnables.add(runnable);
        }

        public KeySeries delay(int ms) {
            add(() -> Thread.sleep(ms));
            return this;
        }

        public KeySeries add(String key) {
            return addCode(KeyStroke.getKeyStroke(key).getKeyCode());
        }

        public KeySeries add(char c) {
            return addCode(KeyEvent.getExtendedKeyCodeForChar(c));
        }

        private KeySeries addCode(int code) {
            add(() -> {
                robot.keyPress(code);
                robot.keyRelease(code);
                Thread.sleep(200);
            });
            return this;
        }

        interface Runnable {
            void run() throws Exception;
        }
    }

    public interface Check {
        void check() throws Exception;
    }

    public class CheckErrorMessage implements Check {
        private final String[] expected;

        public CheckErrorMessage(String... expected) {
            this.expected = expected;
        }

        @Override
        public void check() {
            String errorMessage = ErrorMessage.getLastErrorMessage();
            assertNotNull("no error detected!", errorMessage);
            for (String e : expected)
                assertTrue(errorMessage + " does not contain " + e, errorMessage.contains(e));

        }
    }
}
