package de.neemann.digital.integration;

import de.neemann.digital.gui.Main;
import de.neemann.gui.ErrorMessage;
import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TestInGUI extends TestCase {

    private Main main;

    private boolean isDisplay() {
        final boolean isDisplay = !GraphicsEnvironment.isHeadless();
        if (!isDisplay)
            System.err.println("is running headless, skip tests!");
        return isDisplay;
    }

    public void testErrorAtStart() {
        if (isDisplay()) {
            expectErrorAtStart("dig/manualError/01_fastRuntime.dig", "01_fastRuntime.dig");
            expectErrorAtStart("dig/manualError/02_fastRuntimeEmbed.dig", "short.dig");
            expectErrorAtStart("dig/manualError/06_initPhase.dig", "06_initPhase.dig");
            expectErrorAtStart("dig/manualError/07_creationPhase.dig", "07_creationPhase.dig");
            expectErrorAtStart("dig/manualError/08_twoFastClocks.dig");
        }
    }

    private void expectErrorAtStart(String name, String... content) {
        execute(name, new TestSet()
                .add(() -> main.start(null)));
        final String message = ErrorMessage.getLastErrorMessage();
        assertNotNull(name, message);
        for (String c : content)
            assertTrue(name, message.contains(c));
    }

    public void testErrorAtTestExecution() {
        if (isDisplay()) {
            execute("dig/manualError/04_testExecution.dig", new TestSet()
                    .add(() -> main.startTests()));
            assertNotNull(ErrorMessage.getLastErrorMessage());
        }
    }

    public void testErrorAtRunToBreak() {
        if (isDisplay()) {
            assertFalse(execute("dig/manualError/05_runToBreak.dig", new TestSet()
                    .add(() -> main.start(null))
                    .delay(100)
                    .add(() -> main.runToBreak())
            ).wasOk());
        }
    }

    private TestSet execute(String name, TestSet builder) {
        File file = new File(Resources.getRoot(), name);
        return new TestSet()
                .add(() -> main = new Main.MainBuilder().setFileToOpen(file).build())
                .add(() -> main.setVisible(true))
                .delay(500)
                .add(builder)
                .delay(500)
                .add(() -> main.dispose())
                .delay(500)
                .execute();
    }

    interface TestStep {
        void step() throws Exception;
    }

    class TestSet {
        private ArrayList<TestStepContainer> steps;
        private ArrayList<Exception> exceptions;

        private TestSet() {
            this.steps = new ArrayList<>();
            this.exceptions = new ArrayList<>();
        }

        public TestSet add(TestStep step) {
            steps.add(new TestStepContainer(step, true));
            return this;
        }

        public TestSet add(TestSet builder) {
            if (builder != null)
                steps.addAll(builder.steps);
            return this;
        }

        private TestSet execute() {
            for (TestStepContainer ts : steps)
                if (ts.gui) {
                    try {
                        SwingUtilities.invokeAndWait(() -> {
                            try {
                                ts.step.step();
                            } catch (Exception e) {
                                addException(e);
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException e) {
                        addException(e);
                    }
                } else {
                    try {
                        ts.step.step();
                    } catch (Exception e) {
                        addException(e);
                    }
                }
            return this;
        }

        private void addException(Exception e) {
            exceptions.add(e);
        }

        private boolean wasOk() {
            return exceptions.isEmpty();
        }

        private TestSet delay(int delay) {
            steps.add(new TestStepContainer(() -> Thread.sleep(delay), false));
            return this;
        }

        private class TestStepContainer {
            private final TestStep step;
            private final boolean gui;

            private TestStepContainer(TestStep step, boolean gui) {
                this.step = step;
                this.gui = gui;
            }
        }
    }

}
