/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.toolchain;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Runs all the commands in all configs in the hdl folder.
 * Tests only the file creation and ensures the templates are ok.
 * Does not ensure the generated files are correct!
 */
public class RegressionTest extends TestCase {
    private ToBreakRunner br;
    private int[] frequencies = new int[]{20, 10000000};
    private VisualElement clock;
    private ConfigurationTest.TestIOInterface ioInterface;

    public void testSimple() throws Exception {
        ioInterface = new ConfigurationTest.TestIOInterface();
        br = new ToBreakRunner(new File(Resources.getRoot(), "toolchain/ff.dig"));
        clock = br.getCircuit().getElements(v -> v.equalsDescription(Clock.DESCRIPTION)).get(0);
        File root = new File(Resources.getRoot(), "../../main/dig/hdl");
        int fc = new FileScanner(this::doCheck).setSuffix(".config").scan(root);

        assertEquals(3, fc);
    }

    private void doCheck(File f) throws IOException, InterruptedException {
        Configuration configuration = Configuration.load(f)
                .setFilenameProvider(() -> new File("z/test.dig"))
                .setLibraryProvider(br::getLibrary)
                .setCircuitProvider(br::getCircuit)
                .setIoInterface(ioInterface);

        for (int freq : frequencies) {
            ioInterface.clear();
            clock.setAttribute(Keys.FREQUENCY, freq);
            for (Command command : configuration.getCommands())
                configuration.executeCommand(command, null, null).join();
        }
    }
}
