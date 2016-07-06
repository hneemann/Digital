package de.neemann.digital.gui.components.test;

import de.neemann.digital.lang.Lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author hneemann
 */
public class TestDataParser {

    private final BufferedReader r;
    private final ArrayList<int[]> lines;
    private final ArrayList<String> names;
    private int lineNumber;

    /**
     * Create a new parser
     *
     * @param data the string to parse
     */
    public TestDataParser(String data) {
        lines = new ArrayList<>();
        names = new ArrayList<>();
        r = new BufferedReader(new StringReader(data));
        lineNumber = 0;
    }

    /**
     * Parses the string
     *
     * @return this for chained calls
     * @throws DataException DataException
     */
    public TestDataParser parse() throws DataException {
        try {
            String header = readNonEmptyLine(r);
            if (header != null) {
                StringTokenizer tok = new StringTokenizer(header);
                while (tok.hasMoreElements())
                    names.add(tok.nextToken());

                String line;
                while ((line = readNonEmptyLine(r)) != null) {
                    int[] row = new int[names.size()];
                    tok = new StringTokenizer(line);
                    int cols = tok.countTokens();
                    if (cols != names.size())
                        throw new DataException(Lang.get("err_testDataExpected_N0_found_N1_numbersInLine_N2", names.size(), cols, lineNumber));

                    for (int i = 0; i < cols; i++) {
                        String num = null;
                        try {
                            num = tok.nextToken();
                            if (num.toUpperCase().equals("X"))
                                row[i] = -1;
                            else
                                row[i] = Integer.parseInt(num);
                        } catch (NumberFormatException e) {
                            throw new DataException(Lang.get("err_notANumber_N0_inLine_N1", num, lineNumber));
                        }
                    }
                    lines.add(row);
                }
            }

        } catch (IOException e) {
            throw new DataException(e);
        }
        return this;
    }

    private String readNonEmptyLine(BufferedReader r) throws IOException {
        while (true) {
            lineNumber++;
            String line = r.readLine();
            if (line == null || (line.length() > 0 && line.charAt(0) != '#'))
                return line;
        }
    }

    /**
     * @return Returns the data lines
     */
    public ArrayList<int[]> getLines() {
        return lines;
    }

    /**
     * @return the signal names
     */
    public ArrayList<String> getNames() {
        return names;
    }
}
