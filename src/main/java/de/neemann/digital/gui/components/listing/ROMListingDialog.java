package de.neemann.digital.gui.components.listing;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author hneemann
 */
public class ROMListingDialog extends JDialog implements Observer {

    private final ROM rom;
    private final Listing listing;
    private final JList<String> list;
    private int lastAddr = -1;

    public ROMListingDialog(JFrame parent, ROM rom) throws IOException {
        super(parent, Lang.get("win_listing"), false);
        this.rom = rom;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        File filename = rom.getListFile();
        String name = filename.getName();
        int p = name.lastIndexOf('.');
        if (p >= 0) {
            name = name.substring(0, p);
            filename = new File(filename.getParentFile(), name + ".lst");
        }

        listing = new Listing(filename);
        list = new JList<>(listing);
        list.setFont(new Font("monospaced", Font.PLAIN, 12));

        rom.getAddrIn().addObserver(this);
        hasChanged();

        getContentPane().add(new JScrollPane(list));
        pack();
        setLocationRelativeTo(parent);
    }

    @Override
    public void hasChanged() {
        int addr = (int) rom.getAddrIn().getValueIgnoreBurn();
        if (addr != lastAddr) {
            Integer line = listing.getLine(addr);
            if (line != null) {
                list.ensureIndexIsVisible(line);
                list.setSelectedIndex(line);
            }

            lastAddr = addr;
        }
    }

}
