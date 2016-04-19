package de.neemann.digital.gui.components.listing;

import javax.swing.event.ListDataListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a ROM listing
 *
 * @author hneemann
 */
public class Listing implements javax.swing.ListModel<String> {
    private final ArrayList<String> lines;
    private final HashMap<Integer, Integer> addrMap;

    /**
     * Creates a new instance.
     *
     * @param filename the file to read
     * @throws IOException IOException
     */
    public Listing(File filename) throws IOException {
        this(new FileInputStream(filename));
    }

    /**
     * Creates a new instance.
     * The given file is to read.
     *
     * @param inputStream the inputStream to read the file
     * @throws IOException IOException
     */
    public Listing(InputStream inputStream) throws IOException {
        lines = new ArrayList<>();
        addrMap = new HashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = r.readLine()) != null)
                addLine(line);
        }
    }

    private void addLine(String line) {
        int pStart = line.indexOf('|');
        int pEnd = line.indexOf(':', pStart);
        if (pEnd >= 0) {
            String addrStr = line.substring(pStart + 1, pEnd).trim();
            try {
                int addr = Integer.parseInt(addrStr, 16);
                addrMap.put(addr, lines.size());
            } catch (NumberFormatException e) {
                // do nothing on error;
            }
        }
        if (line.length() == 0)
            line = " ";  // JList has problems with empty lines, the line height becomes 2 in this case
        lines.add(line);
    }

    @Override
    public int getSize() {
        return lines.size();
    }

    @Override
    public String getElementAt(int index) {
        return lines.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }

    /**
     * returns the line belonging to the given address
     *
     * @param addr the address
     * @return the line or null if not found.
     */
    public Integer getLine(int addr) {
        return addrMap.get(addr);
    }
}
