package de.neemann.gui;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.JarFile;

/**
 * Helper to show the info dialog!
 * Reads Build-Version and Build-Date from the Manifest.
 * Created by hneemann on 23.03.15.
 */
public final class InfoDialog implements Iterable<InfoDialog.Manifest> {
    private static InfoDialog instance;
    private final ArrayList<Manifest> infos;
    private String revision = "unknown";

    /**
     * @return the singleton instance
     */
    public static InfoDialog getInstance() {
        if (instance == null)
            try {
                instance = new InfoDialog();
            } catch (IOException e) {
                System.out.println("error reading InfoDialog " + e.getMessage());
            }
        return instance;
    }

    private InfoDialog() throws IOException {
        infos = new ArrayList<>();
        Enumeration<URL> resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
        while (resEnum.hasMoreElements()) {
            Manifest m = new Manifest(resEnum.nextElement());
            if (m.get(Manifest.REVISION) != null) {
                infos.add(m);
                revision = m.get(Manifest.REVISION);
            }
        }
    }

    /**
     * Creates a message by taking the given message and adding the manifest infos to it
     *
     * @param message the given message
     * @return message and added manifest infos
     */
    private String createMessage(String message) {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append(message.replace("\n", "<br/>"));
        sb.append("\n\n");
        for (Manifest m : infos) {
            m.createInfoString(sb);
        }
        return sb.append("</html>").toString();
    }

    /**
     * Shows the message in a JOptioPane dialog
     *
     * @param parent  the parent component
     * @param message the message
     */
    private void showInfo(Component parent, String message) {
        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), Lang.get("menu_about"), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JEditorPane ta = new JEditorPane("text/html", createMessage(message));
        ta.setCaretPosition(0);
        ta.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        ta.setEditable(false);
        ta.setBackground(new JLabel().getBackground());
        Font font = ta.getFont().deriveFont(Font.BOLD);
        ta.setFont(font);
        int border = font.getSize();
        ta.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        dialog.getContentPane().add(ta);
        ta.addHyperlinkListener(hyperlinkEvent -> {
            if (hyperlinkEvent.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        URL url = hyperlinkEvent.getURL();
                        url = new URL(url.toString().replace("{{version}}", revision));
                        desktop.browse(url.toURI());
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton button = new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.dispose();
            }
        });
        buttons.add(button);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);

        JLabel l = new JLabel(IconCreator.create("icon64.png"));
        l.setVerticalAlignment(JLabel.TOP);
        l.setBorder(BorderFactory.createEmptyBorder(border, border, border, 0));
        dialog.getContentPane().add(l, BorderLayout.WEST);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * Adds the help menu to a JFrame
     *
     * @param frame   the frame
     * @param message the message
     * @return the help menu
     */
    public JMenu addToFrame(final JFrame frame, final String message) {
        JMenuBar bar = frame.getJMenuBar();
        JMenu help = new JMenu(Lang.get("menu_help"));
        bar.add(help);
        help.add(new AbstractAction(Lang.get("menu_about")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showInfo(frame, message);
            }
        });
        return help;
    }

    @Override
    public Iterator<Manifest> iterator() {
        return infos.iterator();
    }

    /**
     * A simple Manifest parser
     */
    public static final class Manifest {
        private static final String REVISION = "Build-SCM-Revision";
        private static final String TIME = "Build-Time";

        private final HashMap<String, String> manifest;
        private final URL url;

        private Manifest(URL url) throws IOException {
            this.url = url;
            manifest = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int p = line.indexOf(':');
                    if (p >= 0) {
                        String key = line.substring(0, p).trim();
                        String value = line.substring(p + 1).trim();
                        manifest.put(key, value);
                    }
                }
            }
        }

        private String get(String key) {
            return manifest.get(key);
        }

        /**
         * @return returns all entries
         */
        public HashMap<String, String> getEntries() {
            return manifest;
        }

        /**
         * @return the manifest url
         */
        public URL getUrl() {
            return url;
        }

        private void createInfoString(StringBuilder sb) {
            String path = url.getPath();
            int p = path.lastIndexOf("!/");
            path = path.substring(0, p);
            p = path.lastIndexOf('/');
            sb.append("<p>");
            sb.append(path.substring(p + 1)).append("<br/>");
            sb.append("Build git-Revision").append(": ");
            sb.append(get(REVISION)).append("<br/>");
            sb.append("Build Time").append(": ");
            sb.append(get(TIME)).append("<br/></p>");
        }

        @Override
        public String toString() {
            return "Manifest{"
                    + "manifest=" + manifest
                    + ", url=" + url + '}';
        }
    }
}
