/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import javax.swing.table.TableModel;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Helper to write a {@link TableModel} to a csv file
 */
public class CSVWriter {
    private final TableModel tableModel;
    private boolean newLine = true;

    /**
     * Creates a new instance
     *
     * @param tableModel the table model to be written to a csv file
     */
    public CSVWriter(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    /**
     * Writes the {@link TableModel} to the given writer
     *
     * @param w the writer to use
     * @throws IOException IOException
     */
    public void writeTo(BufferedWriter w) throws IOException {
        for (int c = 0; c < tableModel.getColumnCount(); c++)
            writeCell(w, tableModel.getColumnName(c));
        newLine(w);

        for (int r = 0; r < tableModel.getRowCount(); r++) {
            for (int c = 0; c < tableModel.getColumnCount(); c++)
                writeCell(w, tableModel.getValueAt(r, c));
            newLine(w);
        }
        w.close();
    }

    private void newLine(BufferedWriter w) throws IOException {
        w.newLine();
        newLine = true;
    }

    private void writeCell(BufferedWriter w, Object value) throws IOException {
        if (!newLine)
            w.write(',');
        if (value != null) {
            String v = value.toString();
            boolean quotes = v.contains(",") || v.contains(";") || v.contains("\t") || v.contains("\n");
            if (quotes) w.write('"');
            w.write(v);
            if (quotes) w.write('"');
        }
        newLine = false;
    }
}
