/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table.hardware;

import de.neemann.digital.analyse.ModelAnalyserInfo;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.modify.ExpressionModifier;
import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.components.table.BuilderExpressionCreator;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.LineBreaker;
import de.neemann.gui.MyFileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;

/**
 * Generates a file. Used for JEDEC and TT2 generation
 */
public class GenerateFile implements HardwareDescriptionGenerator {

    private final String suffix;
    private final String path;
    private final String description;
    private final ExpressionToFileExporterFactory factory;

    /**
     * Creates a new instance.
     *
     * @param suffix      the file suffix
     * @param factory     creates the ExpressionToFileExporter
     * @param path        then menu path
     * @param description the description, used as a tool tip
     */
    public GenerateFile(String suffix, ExpressionToFileExporterFactory factory, String path, String description) {
        this.suffix = suffix;
        this.path = path;
        this.description = description;
        this.factory = factory;
    }

    @Override
    public String getMenuPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void generate(JDialog parent, File circuitFile, TruthTable table, ExpressionListenerStore expressions) throws Exception {
        ModelAnalyserInfo mai = table.getModelAnalyzerInfo();
        if (mai == null) {
            JOptionPane.showMessageDialog(parent,
                    new LineBreaker().toHTML().breakLines(Lang.get("msg_circuitIsRequired")),
                    Lang.get("msg_warning"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            ArrayList<String> pinsWithoutNumber = mai.getPinsWithoutNumber();
            if (!pinsWithoutNumber.isEmpty()) {
                int res = JOptionPane.showConfirmDialog(parent,
                        new LineBreaker().toHTML().breakLines(Lang.get("msg_thereAreMissingPinNumbers", pinsWithoutNumber)),
                        Lang.get("msg_warning"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (res != JOptionPane.OK_OPTION)
                    return;
            }
        }

        if (circuitFile == null)
            circuitFile = new File("circuit." + suffix);
        else
            circuitFile = SaveAsHelper.checkSuffix(circuitFile, suffix);

        JFileChooser fileChooser = new MyFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JEDEC", suffix));
        fileChooser.setSelectedFile(circuitFile);
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            ExpressionToFileExporter expressionExporter = factory.create();
            expressionExporter.getPinMapping().addAll(mai.getPins());
            expressionExporter.getPinMapping().setClockPin(mai.getClockPinInt());
            new BuilderExpressionCreator(expressionExporter.getBuilder(), ExpressionModifier.IDENTITY).create(expressions);
            expressionExporter.export(SaveAsHelper.checkSuffix(fileChooser.getSelectedFile(), suffix));
        }
    }

    /**
     * Factory to create a ExpressionToFileExporter
     */
    public interface ExpressionToFileExporterFactory {
        /**
         * @return creates a new instance
         */
        ExpressionToFileExporter create();
    }
}
