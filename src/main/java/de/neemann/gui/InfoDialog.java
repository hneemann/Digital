package de.neemann.gui;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarFile;

/**
 * Helper to show the info dialog!
 * Reads Build-Version and Build-Date from the Manifest.
 * Created by hneemann on 23.03.15.
 */
public final class InfoDialog {
    private static InfoDialog instance;
    private final ArrayList<Manifest> infos;
    private String revision;

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
     * Creates amessage by taking the given message and adding the manifestinfos to it
     *
     * @param message the given message
     * @return message and added manifest infos
     */
    private String createMessage(String message) {
        StringBuilder sb = new StringBuilder(message);
        sb.append("\n\n");
        for (Manifest m : infos) {
            m.createInfoString(sb);
        }
        return sb.toString();
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

        JTextArea ta = new JTextArea(createMessage(message));
        ta.setEditable(false);
        ta.setBackground(new JLabel().getBackground());
        Font font = ta.getFont().deriveFont(Font.BOLD);
        ta.setFont(font);
        int border = font.getSize();
        ta.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
        dialog.getContentPane().add(ta);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            URI uri;
            try {
                if (revision == null)
                    uri = new URI("https://github.com/hneemann/Digital/issues/new");
                else
                    uri = new URI("https://github.com/hneemann/Digital/issues/new?body=revision:%20" + revision);

                JButton issue = new JButton(new AbstractAction(Lang.get("btn_fileIssue")) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            desktop.browse(uri);
                            dialog.dispose();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                buttons.add(issue);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
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

    private static class Manifest {
        private static final String REVISION = "Build-SCM-Revision";
        private static final String TIME = "Build-Time";

        private final HashMap<String, String> manifest;
        private final URL url;

        Manifest(URL url) throws IOException {
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

        public String get(String key) {
            return manifest.get(key);
        }

        private void createInfoString(StringBuilder sb) {
            String path = url.getPath();
            int p = path.lastIndexOf("!/");
            path = path.substring(0, p);
            p = path.lastIndexOf('/');
            sb.append(path.substring(p + 1)).append('\n');
            sb.append("Build git-Revision").append(": ");
            sb.append(get(REVISION)).append("\n");
            sb.append("Build Time").append(": ");
            sb.append(get(TIME)).append("\n\n");
        }

        @Override
        public String toString() {
            return "Manifest{"
                    + "manifest=" + manifest
                    + ", url=" + url + '}';
        }
    }
}
