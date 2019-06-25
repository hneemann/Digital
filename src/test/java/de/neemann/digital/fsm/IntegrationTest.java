/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class IntegrationTest extends TestCase {

    public void testExamples() throws Exception {
        File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/fsm");
        assertEquals(9, new FileScanner(this::check).setSuffix("fsm").scan(examples));

    }

    /*
     * For now, only create the truth table.
     * Does not test if the truth table is correct!
     */
    private void check(File file) throws IOException, ExpressionException, FiniteStateMachineException {
        FSM.loadFSM(file).createTruthTable(null);
    }
}
