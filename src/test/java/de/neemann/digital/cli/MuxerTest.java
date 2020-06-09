/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.cli.cli.Muxer;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MuxerTest extends TestCase {

    /**
     * Creates the documentation.
     * Used to make the maven build fail, if a language key is missing.
     */
    public void testDocu() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Main().printDescription(new PrintStream(baos), "");
        assertTrue(baos.toByteArray().length > 100);
    }

    public void testErrors() throws CLIException {
        TestCommand tc = new TestCommand();
        Muxer m = new Muxer("test").addCommand(tc);

        try {
            m.execute(new String[]{"add"});
            fail();
        } catch (CLIException e) {
            assertTrue(true);
        }

        m.execute(new String[]{"add", "-arg", "zz"});
        assertEquals("zz", tc.arg.get());

        try {
            m.execute(new String[]{"add", "-foo", "zz"});
            fail();
        } catch (CLIException e) {
            assertTrue(true);
        }

        try {
            m.execute(new String[]{"sub"});
            fail();
        } catch (CLIException e) {
            assertTrue(true);
        }

        try {
            m.execute(new String[]{});
            fail();
        } catch (CLIException e) {
            assertTrue(true);
        }
    }

    public void testNesting() throws CLIException {
        TestCommand tc = new TestCommand();
        Muxer m = new Muxer("main")
                .addCommand(new Muxer("arith")
                        .addCommand(tc));

        m.execute(new String[]{"arith", "add", "-arg", "tt"});

        assertTrue(tc.wasExecuted);
    }

    private static class TestCommand extends BasicCommand {
        private final Argument<String> arg;
        private boolean wasExecuted = false;

        public TestCommand() {
            super("add");
            arg = addArgument(new Argument<>("arg", "def", false));
        }


        @Override
        public void printDescription(PrintStream out, String prefix) {
        }

        @Override
        protected void execute() {
            wasExecuted = true;
        }

    }
}