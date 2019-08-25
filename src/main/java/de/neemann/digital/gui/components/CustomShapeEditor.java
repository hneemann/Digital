/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.draw.shapes.custom.svg.SvgException;
import de.neemann.digital.draw.shapes.custom.svg.SvgImporter;
import de.neemann.digital.draw.shapes.custom.svg.SvgTemplate;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.MyFileChooser;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Editor used to define a custom shape.
 * The actual implementation is only able to create a simple dummy shape.
 */
public class CustomShapeEditor extends EditorFactory.LabelEditor<CustomShapeDescription> {
    private CustomShapeDescription customShapeDescription;
    private ToolTipAction clear;
    private ToolTipAction load;
    private ToolTipAction template;
    private static File lastSVGFile;

    /**
     * Creates a new instance
     *
     * @param customShapeDescription the shape to edit
     * @param key                    the used key
     */
    public CustomShapeEditor(CustomShapeDescription customShapeDescription, Key<CustomShapeDescription> key) {
        this.customShapeDescription = customShapeDescription;
    }

    @Override
    public JComponent getComponent(ElementAttributes attr) {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        clear = new ToolTipAction(Lang.get("btn_clearData")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                customShapeDescription = new CustomShapeDescription.Builder().build();
            }
        };
        panel.add(clear.createJButton());
        load = new ToolTipAction(Lang.get("btn_loadSvg")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getjFileChooser();
                if (fc.showOpenDialog(getAttributeDialog()) == JFileChooser.APPROVE_OPTION) {
                    lastSVGFile = fc.getSelectedFile();
                    try {
                        CustomShapeDescription csd = new SvgImporter(fc.getSelectedFile()).create();
                        final Main main = getAttributeDialog().getMain();
                        if (main != null) {
                            csd.checkCompatibility(main.getCircuitComponent().getCircuit());
                            customShapeDescription = csd;
                        }
                    } catch (IOException | SvgException | PinException e1) {
                        new ErrorMessage(Lang.get("msg_errorImportingSvg")).addCause(e1).show(getAttributeDialog());
                    }
                }
            }
        }.setToolTip(Lang.get("btn_loadSvg_tt"));
        panel.add(load.createJButton());
        template = new ToolTipAction(Lang.get("btn_saveTemplate")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getjFileChooser();
                if (fc.showSaveDialog(getAttributeDialog()) == JFileChooser.APPROVE_OPTION) {
                    try {
                        final Main main = getAttributeDialog().getMain();
                        if (main != null) {
                            File file = SaveAsHelper.checkSuffix(fc.getSelectedFile(), "svg");
                            lastSVGFile = file;
                            try (SvgTemplate tc = new SvgTemplate(file, main.getCircuitComponent().getCircuit())) {
                                tc.create();
                            }
                        }
                    } catch (Exception e1) {
                        new ErrorMessage(Lang.get("msg_errorCreatingSvgTemplate")).addCause(e1).show(getAttributeDialog());
                    }
                }
            }
        }.setToolTip(Lang.get("btn_saveTemplate_tt"));
        panel.add(template.createJButton());
        return panel;
    }

    private JFileChooser getjFileChooser() {
        File path = null;
        if (lastSVGFile == null)
            lastSVGFile = SaveAsHelper.checkSuffix(getAttributeDialog().getMain().getBaseFileName(), "svg");
        if (lastSVGFile != null)
            path = lastSVGFile.getParentFile();

        JFileChooser fc = new MyFileChooser(path);
        if (lastSVGFile != null)
            fc.setSelectedFile(lastSVGFile);
        return fc;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        load.setEnabled(enabled);
        clear.setEnabled(enabled);
        template.setEnabled(enabled);
    }

    @Override
    public CustomShapeDescription getValue() {
        return customShapeDescription;
    }

    @Override
    public void setValue(CustomShapeDescription value) {
    }
}
