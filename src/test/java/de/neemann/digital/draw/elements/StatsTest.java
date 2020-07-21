/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.stats.Statistics;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import javax.swing.table.TableModel;
import java.io.File;

public class StatsTest extends TestCase {

    public void testStats() throws Exception {
        File file = new File(Resources.getRoot(), "../../main/dig/combinatorial/FullAdderRC.dig");
        final ToBreakRunner br = new ToBreakRunner(file);
        Statistics stats = new Statistics(br.getModel());
        TableModel model = stats.getTableModel();
        assertEquals(3, model.getRowCount());
        assertEquals(8, model.getValueAt(0, 5));
        assertEquals(4, model.getValueAt(1, 5));
        assertEquals(8, model.getValueAt(2, 5));
    }

    private int intVal(Object value) {
        if (value instanceof Number)
            return ((Number) value).intValue();
        return 0;
    }

}