package de.neemann.digital.gui.components.test;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.gui.components.EditorFactory;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author hneemann
 */
public class TestDataEditor extends EditorFactory.LabelEditor<TestData> {

    private final TestData data;
    private JButton editButton;

    /**
     * Creates a new editor
     *
     * @param data the data to edit
     * @param key  the data key
     */
    public TestDataEditor(TestData data, Key<DataField> key) {
        this.data = new TestData(data);
    }

    @Override
    public TestData getValue() {
        return data;
    }

    @Override
    protected JComponent getComponent(ElementAttributes elementAttributes) {
        editButton = new ToolTipAction(Lang.get("btn_edit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TestDataDialog(editButton, data).setVisible(true);
            }
        }.createJButton();
        return editButton;
    }
}
