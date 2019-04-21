/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;

public class StatsTest extends TestCase {

    public void testStats() throws PinException, NodeException, ElementNotFoundException, IOException {
        File file = new File(Resources.getRoot(), "../../main/dig/combinatorial/FullAdderRC.dig");
        final ToBreakRunner br = new ToBreakRunner(file);
        Circuit c = br.getCircuit();
        Stats stats = new Stats(br.getLibrary()).add(c);
        TableModel model = stats.getTableModel();
        assertEquals(5, model.getRowCount());
        assertEquals(200, model.getValueAt(4, 3));

        // set xor to 4 transistors
        for (int i = 0; i < 4; i++)
            if (intVal(model.getValueAt(i, 2)) == 16)
                model.setValueAt(4, i, 2);

        assertEquals(104, model.getValueAt(4, 3));
    }

    private int intVal(Object value) {
        if (value instanceof Number)
            return ((Number) value).intValue();
        return 0;
    }

}