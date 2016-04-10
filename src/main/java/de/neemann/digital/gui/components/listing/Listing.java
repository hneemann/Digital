package de.neemann.digital.gui.components.listing;

import javax.swing.event.ListDataListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author hneemann
 */
public class Listing implements javax.swing.ListModel<String> {
    private final ArrayList<String> lines;
    private final HashMap<Integer, Integer> addrMap;

    public Listing(File filename) throws IOException {
        lines = new ArrayList<String>();
        addrMap = new HashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"))) {
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

    public Integer getLine(int addr) {
        return addrMap.get(addr);
    }
}
