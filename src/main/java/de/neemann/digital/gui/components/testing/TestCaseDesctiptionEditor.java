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
 * @author hneemann
 */
public class TestCaseDesctiptionEditor extends EditorFactory.LabelEditor<TestCaseDescription> {

    private final TestCaseDescription data;
    private final Key<TestCaseDescription> key;

    /**
     * Creates a new editor
     *
     * @param data the data to edit
     * @param key  the data key
     */
    public TestCaseDesctiptionEditor(TestCaseDescription data, Key<TestCaseDescription> key) {
        this.data = new TestCaseDescription(data);
        this.key = key;
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
                new TestCaseDescriptionDialog(panel, data, null).setVisible(true);
            }
        }.createJButton());

        panel.add(new ToolTipAction(Lang.get("btn_editDetached")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getAttributeDialog().fireOk();
                VisualElement visualElement = TestCaseDesctiptionEditor.this.getAttributeDialog().getVisualElement();
                TestCaseDescriptionDialog dialog = new TestCaseDescriptionDialog(getAttributeDialog().getDialogParent(), data, visualElement);
                Main main = getAttributeDialog().getMain();
                if (main != null)
                    main.getWindowPosManager().register("testdata", dialog);
                dialog.setVisible(true);
                } catch (EditorParseException e1) {
                    e1.printStackTrace();
                }
            }
        }.setToolTip(Lang.get("btn_editDetached_tt"))
                .createJButton());

        return panel;
    }
}
