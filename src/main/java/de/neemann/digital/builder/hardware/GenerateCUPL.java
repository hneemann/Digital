package de.neemann.digital.builder.hardware;

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
 * Creates CUPL code
 */
public class GenerateCUPL implements HardwareDescriptionGenerator {
    private CuplExporter cupl;
    private String path;

    /**
     * Creates e new instance
     *
     * @param cupl the CUPL exporter
     * @param path the gui menu path
     */
    public GenerateCUPL(CuplExporter cupl, String path) {
        this.cupl = cupl;
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return Lang.get("menu_table_createCUPL_tt");
    }

    @Override
    public void create(JDialog parent, File circuitFile, TruthTable table, ExpressionListenerStore expressions) throws Exception {
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
        cupl.setProjectName(circuitFile.getName());
        cupl.getPinMapping().addAll(table.getPins());
        new BuilderExpressionCreator(cupl.getBuilder(), ExpressionModifier.IDENTITY).create(expressions);
        try (FileOutputStream out = new FileOutputStream(f)) {
            cupl.writeTo(out);
        }
    }
}
