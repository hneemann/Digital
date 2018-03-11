/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Used to open documentation
 */
public class DocumentationLocator {

    private final File folder;

    /**
     * Creates a new instance
     */
    public DocumentationLocator() {
        File folder = null;
        try {
            String path = ElementLibrary.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replace('\\', '/');
            if (path.endsWith("/target/classes/"))
                folder = new File(path.substring(0, path.length() - 9) + "/docu");
            else if (path.endsWith("Digital.jar"))
                folder = new File(path.substring(0, path.length() - 12) + "/docu");
        } catch (URISyntaxException e) {
            // do nothing on error
        }
        if (folder != null && folder.exists())
            this.folder = folder;
        else
            this.folder = null;
    }

    /**
     * Adds the documentation to the given menu
     *
     * @param help the menu to add the documentation to
     */
    public void addMenuTo(JMenu help) {
        if (folder != null) {
            File[] files = folder.listFiles((file, name) -> name.endsWith(".pdf"));
            if (files != null && files.length > 0) {

                String language = "_" + Lang.currentLanguage().getName() + ".pdf";
                File found = null;
                for (File f : files)
                    if (f.getName().endsWith(language))
                        found = f;

                if (found == null) {
                    JMenu docu = new JMenu(Lang.get("menu_pdfDocumentation"));
                    help.add(docu);
                    for (File f : files)
                        docu.add(new OpenPDFAction(f).createJMenuItem());
                } else
                    help.add(new OpenPDFAction(found, Lang.get("menu_pdfDocumentation")));
            }
        }
    }

    private static final class OpenPDFAction extends ToolTipAction {
        private final File f;

        private OpenPDFAction(File f) {
            this(f, Lang.get("menu_openPdfDocumentation", f.getName()));
        }

        private OpenPDFAction(File f, String name) {
            super(name);
            this.f = f;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.OPEN))
                    desktop.open(f);
                else
                    throw new IOException("could not open pdf document");
            } catch (IOException e) {
                new ErrorMessage(Lang.get("msg_errorOpeningDocumentation")).addCause(e).show();
            }
        }
    }
}
