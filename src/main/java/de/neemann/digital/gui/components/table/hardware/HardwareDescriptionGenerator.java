/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table.hardware;

import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;

import javax.swing.*;
import java.io.File;

/**
 * Interface for hardware description functions
 */
public interface HardwareDescriptionGenerator {

    /**
     * @return the gui menu path
     */
    String getMenuPath();

    /**
     * @return the description of the generator, used as a tool tip
     */
    String getDescription();

    /**
     * Creates the hardware description file.
     *
     * @param parent      the parent jDialog
     * @param circuitFile the circuit file, can be used as a file name base
     * @param table       the truth table used
     * @param expressions the expressions generated
     * @throws Exception Exception
     */
    void generate(JDialog parent, File circuitFile, TruthTable table, ExpressionListenerStore expressions) throws Exception;
}
