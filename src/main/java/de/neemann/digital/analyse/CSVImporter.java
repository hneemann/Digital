/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.ArrayList;

/**
 * Used to read a CSV file
 */
public final class CSVImporter {

    private CSVImporter() {
    }

    /**
     * Reads a CSV file
     *
     * @param file the file
     * @return the truth table
     * @throws IOException IOException
     */
    public static TruthTable readCSV(File file) throws IOException {
        return readCSV(new FileReader(file));
    }

    /**
     * Reads a CSV file
     *
     * @param csv the string to read
     * @return the truth table
     * @throws IOException IOException
     */
    public static TruthTable readCSV(String csv) throws IOException {
        return readCSV(new StringReader(csv));
    }

    /**
     * Reads a CSV file
     *
     * @param csv the reader
     * @return the truth table
     * @throws IOException IOException
     */
    public static TruthTable readCSV(Reader csv) throws IOException {
        BufferedReader r = new BufferedReader(csv);
        TruthTable tt = readHeader(r);

        while (true) {
            String line = r.readLine();
            if (line == null)
                return tt;
            line = line.trim();
            if (!line.isEmpty())
                parseLine(tt, line);
        }

    }

    private static TruthTable readHeader(BufferedReader r) throws IOException {
        String header;
        do {
            header = r.readLine();
        } while (header != null && header.length() == 0);

        if (header == null)
            throw new IOException(Lang.get("err_csvNoHeaderFound"));

        ArrayList<Variable> vars = new ArrayList<>();

        TruthTable tt = null;
        for (String ss : header.split(",")) {
            String h = ss.trim();
            if (h.isEmpty())
                tt = new TruthTable(vars);
            else {
                if (tt == null)
                    vars.add(new Variable(h));
                else
                    tt.addResult(h, new BoolTableByteArray(1 << vars.size()));

            }
        }

        if (tt == null || tt.getResultCount() == 0)
            throw new IOException(Lang.get("err_csvNoOutputValuesFound"));

        return tt;
    }

    private static void parseLine(TruthTable tt, String line) throws IOException {
        int resNum = tt.getResultCount();
        int varNum = tt.getVars().size();
        int mask = 1 << (varNum - 1);
        ArrayList<Integer> dc = new ArrayList<>();
        int row = 0;
        int rCol = 0;
        Generator generator = null;
        for (String ss : line.split(",")) {
            String e = ss.trim().toLowerCase();
            if (e.isEmpty())
                generator = new Generator(row, dc);
            else {
                if (generator == null) {
                    if (mask == 0)
                        throw new IOException(Lang.get("err_csvToManyValues"));
                    if (e.equals("1"))
                        row |= mask;
                    else if (e.equals("x"))
                        dc.add(mask);
                    mask = mask >> 1;
                } else {
                    if (rCol >= resNum)
                        throw new IOException(Lang.get("err_csvToManyValues"));
                    if (e.equals("1"))
                        generator.addCol(rCol, 1);
                    else if (e.equals("x"))
                        generator.addCol(rCol, 2);
                    rCol++;
                }
            }
        }

        if (mask != 0)
            throw new IOException(Lang.get("err_csvNotEnoughValues"));

        if (generator != null)
            generator.applyTo(tt);
    }

    private static final class ColHolder {
        private final int rCol;
        private final int val;

        private ColHolder(int rCol, int val) {
            this.rCol = rCol;
            this.val = val;
        }
    }

    private static final class Generator {
        private final int row;
        private final ArrayList<Integer> dc;
        private final ArrayList<ColHolder> cols;

        private Generator(int row, ArrayList<Integer> dc) {
            this.row = row;
            this.dc = dc;
            cols = new ArrayList<>();
        }

        public void addCol(int rCol, int val) {
            cols.add(new ColHolder(rCol, val));
        }

        public void applyTo(TruthTable tt) {
            if (cols.isEmpty())
                return;

            int vars = tt.getVars().size();

            if (dc.isEmpty())
                for (ColHolder col : cols)
                    tt.setValue(row, vars + col.rCol, col.val);
            else {
                int dcRows = 1 << dc.size();
                for (int i = 0; i < dcRows; i++) {
                    int r = row;
                    int bitMask = 1;
                    for (int orMask : dc) {
                        if ((i & bitMask) != 0)
                            r = r | orMask;
                        bitMask *= 2;
                    }

                    for (ColHolder col : cols)
                        tt.setValue(r, vars + col.rCol, col.val);

                }
            }
        }
    }

}
