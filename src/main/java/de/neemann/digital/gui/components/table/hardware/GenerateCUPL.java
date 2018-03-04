/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table.hardware;

import de.neemann.digital.analyse.ModelAnalyserInfo;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.modify.ExpressionModifier;
import de.neemann.digital.builder.Gal16v8.CuplExporter;
import de.neemann.digital.gui.components.table.BuilderExpressionCreator;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.MyFileChooser;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Creates CUPL code.
 * The {@link GenerateFile} class is not usable for CUPL creation because CUPL files needed to be stored
 * in a separate folder. Here you will find the creation of this new folder.
 */
public class GenerateCUPL implements HardwareDescriptionGenerator {
    private CuplExporterFactory cuplExporterFactory;
    private String menuPath;

    /**
     * Creates e new instance
     *
     * @param cuplExporterFactory the CUPL exporter
     * @param menuPath            the gui menu path
     */
    public GenerateCUPL(CuplExporterFactory cuplExporterFactory, String menuPath) {
        this.cuplExporterFactory = cuplExporterFactory;
        this.menuPath = menuPath;
    }

    @Override
    public String getMenuPath() {
        return menuPath;
    }

    @Override
    public String getDescription() {
        return Lang.get("menu_table_createCUPL_tt");
    }

    @Override
    public void generate(JDialog parent, File circuitFile, TruthTable table, ExpressionListenerStore expressions) throws Exception {
        File cuplPath;
        if (circuitFile == null) {
            JFileChooser fc = new MyFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle(Lang.get("msg_selectAnEmptyFolder"));
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                cuplPath = fc.getSelectedFile();
                circuitFile = cuplPath;
            } else {
                return;
            }
        } else {
            if (circuitFile.isDirectory()) {
                cuplPath = circuitFile;
            } else {
                String name = circuitFile.getName();
                if (name.length() > 3 && name.charAt(name.length() - 4) == '.')
                    name = name.substring(0, name.length() - 4);
                cuplPath = new File(circuitFile.getParentFile(), "CUPL_" + name);
            }
        }

        if (!cuplPath.mkdirs())
            if (!cuplPath.exists())
                throw new IOException(Lang.get("err_couldNotCreateFolder_N0", cuplPath.getPath()));

        File f = new File(cuplPath, "CUPL.PLD");
        CuplExporter cuplExporter = cuplExporterFactory.create();
        cuplExporter.setProjectName(circuitFile.getName());
        final ModelAnalyserInfo modelAnalyzerInfo = table.getModelAnalyzerInfo();
        if (modelAnalyzerInfo != null)
            cuplExporter.getPinMapping().addAll(modelAnalyzerInfo.getPins());
        new BuilderExpressionCreator(cuplExporter.getBuilder(), ExpressionModifier.IDENTITY).create(expressions);
        try (FileOutputStream out = new FileOutputStream(f)) {
            cuplExporter.writeTo(out);
        }
    }

    /**
     * Interface used to create a {@link CuplExporter}
     */
    public interface CuplExporterFactory {
        /**
         * @return the created cupl exporter
         */
        CuplExporter create();
    }
}
