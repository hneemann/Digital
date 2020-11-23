/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.testing;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.EditorFactory;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * The test case description editor
 */
public class TestCaseDescriptionEditor extends EditorFactory.LabelEditor<TestCaseDescription> {

    private final TestCaseDescription initialData;
    private TestCaseDescription data;

    /**
     * Creates a new editor
     *
     * @param data the data to edit
     * @param key  the data key
     */
    public TestCaseDescriptionEditor(TestCaseDescription data, Key<TestCaseDescription> key) {
        this.data = data;
        this.initialData = data;
    }

    @Override
    public TestCaseDescription getValue() {
        return data;
    }

    @Override
    protected JComponent getComponent(ElementAttributes elementAttributes) {
        JPanel panel = new JPanel(new FlowLayout());

        panel.add(new ToolTipAction(Lang.get("btn_edit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TestCaseDescriptionDialog tdd = new TestCaseDescriptionDialog(SwingUtilities.getWindowAncestor(panel), data);
                TestCaseDescription d = tdd.showDialog();
                if (d != null)
                    data = d;
            }
        }.createJButton());

        panel.add(new ToolTipAction(Lang.get("btn_editDetached")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getAttributeDialog().fireOk();
                    VisualElement visualElement = TestCaseDescriptionEditor.this.getAttributeDialog().getVisualElement();
                    Main main = getAttributeDialog().getMain();
                    if (main != null) {
                        TestCaseDescriptionDialog dialog = new TestCaseDescriptionDialog(main, data, visualElement);
                        main.getWindowPosManager().register("testdata", dialog);
                        dialog.setVisible(true);
                    }
                } catch (EditorParseException e1) {
                    e1.printStackTrace();
                }
            }
        }.setToolTip(Lang.get("btn_editDetached_tt"))
                .createJButton());

        return panel;
    }

    @Override
    public boolean invisibleModification() {
        return !initialData.equals(data);
    }

    @Override
    public void setValue(TestCaseDescription data) {
        this.data = data;
    }
}
