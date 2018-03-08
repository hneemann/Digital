/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.TestExamples;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GHDLProcessTest extends TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GHDLProcessTest.class);

    public void testGHDLIntegration() throws Exception {
        if (GHDLProcess.isInstalled()) {
            File f = new File(Resources.getRoot(), "dig/external/ghdl.dig");
            TestExamples.check(f);
        } else {
            LOGGER.info("ghdl is not installed!");
        }
    }

}