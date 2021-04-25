/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.ElementTypeDescriptionCustom;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Used to export zip files
 */
public class ExportZipAction extends ToolTipAction {
    private final Main main;
    private final ArrayList<Key<File>> fileKeyList;
    private ElementLibrary lib;
    private HashSet<String> elementSet;
    private HashSet<File> addedFiles;
    private File origin;

    /**
     * creates a new instance
     *
     * @param main the main window
     */
    public ExportZipAction(Main main) {
        super(Lang.get("menu_exportZIP"));
        this.main = main;
        setToolTip(Lang.get("menu_exportZIP_tt"));

        fileKeyList = new ArrayList<>();
        for (Key<?> k : Keys.getKeys())
            if (k instanceof Key.KeyFile)
                fileKeyList.add((Key.KeyFile) k);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser(main.getBaseFileName());
        fc.setFileFilter(new FileNameExtensionFilter("ZIP", "zip"));
        new SaveAsHelper(main, fc, "zip").checkOverwrite(file -> {
            try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                Circuit circuit = main.getCircuitComponent().getCircuit();
                lib = main.getCircuitComponent().getLibrary();
                origin = circuit.getOrigin();
                elementSet = new HashSet<>();
                addedFiles = new HashSet<>();
                addCircuitFile(zip, origin, circuit);

                addFilesInAttributes(zip, circuit.getAttributes());

                if (origin != null)
                    addToZip(zip, "MANIFEST.TXT", "Main-Circuit: " + origin.getName() + "\n");
            } catch (ElementNotFoundException e1) {
                throw new IOException(Lang.get("err_errorExportingZip"), e1);
            }
        });
    }

    private void addCircuitFile(ZipOutputStream zip, File file, Circuit circuit) throws ElementNotFoundException, IOException {
        addToZip(zip, file);
        for (VisualElement ve : circuit.getElements()) {
            String name = ve.getElementName();
            if (!elementSet.contains(name)) {
                elementSet.add(name);
                ElementTypeDescription desc = lib.getElementType(name);
                if (desc instanceof ElementTypeDescriptionCustom) {
                    ElementTypeDescriptionCustom custom = (ElementTypeDescriptionCustom) desc;
                    addCircuitFile(zip, custom.getFile(), custom.getCircuit());
                }

                addFilesInAttributes(zip, ve.getElementAttributes());
            }
        }
    }

    private void addFilesInAttributes(ZipOutputStream zip, ElementAttributes attr) throws IOException {
        for (Key<File> k : fileKeyList) {
            if (attr.contains(k)) {
                File f = attr.getFile(k, origin);
                addToZip(zip, f);
            }
        }
    }

    private void addToZip(ZipOutputStream zip, File file) throws IOException {
        if (file != null && !addedFiles.contains(file)) {
            zip.putNextEntry(new ZipEntry(file.getName()));
            try (InputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zip.write(buffer, 0, len);
                }
            }
            addedFiles.add(file);
        }
    }

    private void addToZip(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
    }
}
