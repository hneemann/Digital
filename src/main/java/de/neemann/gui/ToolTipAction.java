package de.neemann.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Action to handle tool tips.
 * C@author hneemann on 06.03.15.
 */
public abstract class ToolTipAction extends AbstractAction {
    private Icon icon;
    private String toolTipText;

    /**
     * Creates a new instance
     *
     * @param name the name of the action
     */
    public ToolTipAction(String name) {
        super(name);
    }

    /**
     * Creates a new instance
     *
     * @param name the name of the action
     * @param icon the icon
     */
    public ToolTipAction(String name, Icon icon) {
        super(name, icon);
        this.icon = icon;
    }

    /**
     * sets the icon
     *
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        putValue(Action.SMALL_ICON, icon);
        this.icon = icon;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Sets the tool tip text
     *
     * @param text the tool tip text
     * @return this for call chaining
     */
    public ToolTipAction setToolTip(String text) {
        this.toolTipText = text;
        return this;
    }

    /**
     * Sets the activated state for this action
     *
     * @param newValue the new state
     * @return this for call chaining
     */
    public ToolTipAction setActive(boolean newValue) {
        super.setEnabled(newValue);
        return this;
    }

    /**
     * @return a JButton associated with this action
     */
    public JButton createJButton() {
        JButton b = new JButton(this);
        if (toolTipText != null) {
            b.setToolTipText(toolTipText);
        }
        return b;
    }

    /**
     * @return a JButton associated with this action, contains only the icon
     */
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

    /**
     * @return a JButton associated with this action, contains only the icon
     */
    public JButton createJButtonNoTextSmall() {
        JButton b = createJButtonNoText();
        b.setPreferredSize(new Dimension(icon.getIconWidth() + 4, icon.getIconHeight() + 4));
        return b;
    }

    /**
     * @return a JMenuItem associated with this action
     */
    public JMenuItem createJMenuItem() {
        JMenuItem i = new JMenuItem(this);
        if (toolTipText != null) {
            i.setToolTipText(toolTipText);
        }
        return i;
    }

    /**
     * @return a JMenuItem associated with this action, contains no icon
     */
    public JMenuItem createJMenuItemNoIcon() {
        JMenuItem i = createJMenuItem();
        i.setIcon(null);
        return i;
    }

}
