/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Editor used to define a custom shape.
 * The actual implementation is only able to create a simple dummy shape.
 */
public class CustomShapeEditor extends EditorFactory.LabelEditor<CustomShapeDescription> {
    private CustomShapeDescription customShapeDescription;
    private ToolTipAction clear;
    private ToolTipAction load;

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
                customShapeDescription = CustomShapeDescription.EMPTY;
            }
        };
        panel.add(clear.createJButton());
        load = new ToolTipAction(Lang.get("btn_load")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                customShapeDescription = CustomShapeDescription.createDummy();
            }
        };
        panel.add(load.createJButton());
        return panel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        load.setEnabled(enabled);
        clear.setEnabled(enabled);
    }

    @Override
    public CustomShapeDescription getValue() {
        return customShapeDescription;
    }

    @Override
    public void setValue(CustomShapeDescription value) {
    }
}
