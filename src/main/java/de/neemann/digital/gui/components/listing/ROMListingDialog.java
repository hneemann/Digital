package de.neemann.digital.gui.components.listing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.gui.sync.Sync;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * The dialog to show the ROM listing while running.
 * Used to debug assembler code.
 *
 * @author hneemann
 */
public class ROMListingDialog extends JDialog implements Observer, ModelStateObserver {

    private final ROM rom;
    private final Listing listing;
    private final JList<String> list;
    private int lastAddr = -1;
    private boolean updateViewEnable = true;

    /**
     * Creates a new instance
     *
     * @param parent the parent frame
     * @param rom    the rom element
     * @param model  the model
     * @throws IOException IOException
     */
    public ROMListingDialog(JFrame parent, ROM rom, Model model, Sync modelSync) throws IOException {
        super(parent, Lang.get("win_listing"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        this.rom = rom;

        File filename = rom.getHexFile();
        String name = filename.getName();
        int p = name.lastIndexOf('.');
        if (p >= 0) {
            name = name.substring(0, p);
            filename = new File(filename.getParentFile(), name + ".lst");
        }

        listing = new Listing(filename);
        list = new JList<>(listing);
        list.setFont(new Font("monospaced", Font.PLAIN, 12));
        list.setVisibleRowCount(30);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                modelSync.access(() -> {
                    rom.addObserver(ROMListingDialog.this);
                    model.addObserver(ROMListingDialog.this);
                });
            }

            @Override
            public void windowClosed(WindowEvent e) {
                modelSync.access(() -> {
                    rom.removeObserver(ROMListingDialog.this);
                    model.removeObserver(ROMListingDialog.this);
                });
            }
        });


        hasChanged();

        getContentPane().add(new JScrollPane(list));
        pack();
        setLocationRelativeTo(parent);
    }

    @Override
    public void hasChanged() {
        int addr = (int) rom.getRomAddress();
        if (addr != lastAddr) {
            updateView(addr);
            lastAddr = addr;
        }
    }

    private void updateView(int addr) {
        if (updateViewEnable) {
            Integer line = listing.getLine(addr);
            if (line != null) {
                SwingUtilities.invokeLater(() -> {
                    list.ensureIndexIsVisible(line);
                    list.setSelectedIndex(line);
                });
            }
        }
    }

    @Override
    public void handleEvent(ModelEvent event) {
        switch (event) {
            case FASTRUN:
                updateViewEnable = false;
                break;
            case BREAK:
            case STOPPED:
                updateViewEnable = true;
                updateView(lastAddr);
                break;
        }
    }
}
