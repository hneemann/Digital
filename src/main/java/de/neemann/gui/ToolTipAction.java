package de.neemann.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by hneemann on 06.03.15.
 */
public abstract class ToolTipAction extends AbstractAction {
    private Icon icon;
    private String toolTipText;

    public ToolTipAction(String name) {
        super(name);
    }

    public ToolTipAction(String name, Icon icon) {
        super(name, icon);
        this.icon = icon;
    }

    public ToolTipAction setToolTip(String text) {
        this.toolTipText = text;
        return this;
    }

    public JButton createJButton() {
        JButton b = new JButton(this);
        if (toolTipText != null) {
            b.setToolTipText(toolTipText);
        }
        return b;
    }

    public JButton createJButtonNoText() {
        JButton b = new JButton(this);
        if (toolTipText != null) {
            b.setToolTipText(toolTipText);
        } else {
            b.setToolTipText(b.getText());
        }
        b.setText(null);
        return b;
    }

    public JButton createJButtonNoTextSmall() {
        JButton b = createJButtonNoText();
        b.setPreferredSize(new Dimension(icon.getIconWidth() + 4, icon.getIconHeight() + 4));
        return b;
    }

    public JMenuItem createJMenuItem() {
        JMenuItem i = new JMenuItem(this);
        if (toolTipText != null) {
            i.setToolTipText(toolTipText);
        }
        return i;
    }

    public JMenuItem createJMenuItemNoIcon() {
        JMenuItem i = createJMenuItem();
        i.setIcon(null);
        return i;
    }

}
