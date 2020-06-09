/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import junit.framework.TestCase;

public class BasicCommandTest extends TestCase {

    private static class TestCommand extends BasicCommand {
        private boolean wasExecuted;

        private TestCommand() {
            super("test");
        }

        @Override
        protected void execute() {
            wasExecuted = true;
        }

        public void testExecutes(boolean shouldBeExecuted) {
            assertEquals(shouldBeExecuted, wasExecuted);
        }
    }

    public void testOptional() throws CLIException {
        TestCommand tc = new TestCommand();
        Argument<String> n1 = tc.addArgument(new Argument<>("n1", "", false));
        Argument<String> n2 = tc.addArgument(new Argument<>("n2", "", false));

        tc.execute(new String[]{"name1", "name2"});

        assertEquals("name1", n1.get());
        assertEquals("name2", n2.get());
    }

    public void testOptional2() throws CLIException {
        TestCommand tc = new TestCommand();
        Argument<String> n1 = tc.addArgument(new Argument<>("n1", "", false));
        Argument<String> n2 = tc.addArgument(new Argument<>("n2", "", false));

        tc.execute(new String[]{"-n1", "name1", "-n2", "name2"});

        assertEquals("name1", n1.get());
        assertEquals("name2", n2.get());
    }

    public void testOptional3() {
        TestCommand tc = new TestCommand();
        Argument<String> n1 = tc.addArgument(new Argument<>("n1", "", false));
        Argument<String> n2 = tc.addArgument(new Argument<>("n2", "", false));

        try {
            tc.execute(new String[]{"name1"});
            fail();
        } catch (CLIException e) {
        }
    }

    public void testOptional4() throws CLIException {
        TestCommand tc = new TestCommand();
        Argument<String> n1 = tc.addArgument(new Argument<>("n1", "n1", true));
        Argument<String> n2 = tc.addArgument(new Argument<>("n2", "n2", true));

        tc.execute(new String[]{});
        assertEquals("n1", n1.get());
        assertEquals("n2", n2.get());
    }

    public void testOptional5() throws CLIException {
        TestCommand tc = new TestCommand();
        Argument<String> n1 = tc.addArgument(new Argument<>("n1", "n1", true));
        Argument<String> n2 = tc.addArgument(new Argument<>("n2", "n2", true));

        try {
            tc.execute(new String[]{"test"});
            fail();
        } catch (CLIException e) {
        }
    }

    public void testWrongArgument() {
        TestCommand tc = new TestCommand();
        Argument<String> n1 = tc.addArgument(new Argument<>("n1", "", true));
        Argument<String> n2 = tc.addArgument(new Argument<>("n2", "", true));

        try {
            tc.execute(new String[]{"-n3", "test"});
            fail();
        } catch (CLIException e) {
        }

        try {
            tc.execute(new String[]{"-n1"});
            fail();
        } catch (CLIException e) {
        }
    }

}