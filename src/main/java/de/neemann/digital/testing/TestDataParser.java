package de.neemann.digital.testing;

import de.neemann.digital.lang.Lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * The Parser used to read the test vector string.
 *
 * @author hneemann
 */
public class TestDataParser {

    private final BufferedReader r;
    private final ArrayList<Value[]> lines;
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
     * Parses the string.
     * The data then can be found in lines and names.
     *
     * @return this for chained calls
     * @throws TestingDataException DataException
     */
    public TestDataParser parse() throws TestingDataException {
        try {
            String header = readNonEmptyLine(r);
            if (header != null) {
                StringTokenizer tok = new StringTokenizer(header);
                while (tok.hasMoreElements())
                    names.add(tok.nextToken());

                String line;
                while ((line = readNonEmptyLine(r)) != null) {
                    line = line.toUpperCase();
                    addLine(line);
                }
            }

        } catch (IOException e) {
            throw new TestingDataException(e);
        }
        return this;
    }

    private void addLine(String line) throws TestingDataException {
        StringTokenizer tok;
        tok = new StringTokenizer(line);
        Value[] row = new Value[names.size()];
        int cols = tok.countTokens();
        if (cols != names.size())
            throw new TestingDataException(Lang.get("err_testDataExpected_N0_found_N1_numbersInLine_N2", names.size(), cols, lineNumber));

        for (int i = 0; i < cols; i++) {
            String numStr = null;
            try {
                numStr = tok.nextToken();
                row[i] = new Value(numStr);
            } catch (NumberFormatException e) {
                throw new TestingDataException(Lang.get("err_notANumber_N0_inLine_N1", numStr, lineNumber));
            }
        }
        lines.add(row);
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
    public ArrayList<Value[]> getLines() {
        return lines;
    }

    /**
     * @return the signal names
     */
    public ArrayList<String> getNames() {
        return names;
    }
}
