/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.graphics.MoveFocusTo;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static de.neemann.digital.gui.components.terminal.ConsoleTerminal.MAX_TERMINAL_STORED;

/**
 * The dialog which shows the terminal
 */
public final class TerminalDialog extends JDialog {
    private final JTextArea textArea;
    private final int width;
    private int pos;

    /**
     * Creates a new GUI terminal
     *
     * @param model the model
     * @param attr  the terminals attributes
     * @return the terminal interface
     */
    static TerminalInterface getTerminal(Model model, ElementAttributes attr) {
        return new MyTerminal(model, attr);
    }

    private static String getDialogTitle(ElementAttributes attr) {
        String t = attr.getLabel();
        if (t.length() > 0) return t;

        return Lang.get("elem_Terminal");
    }

    /**
     * Creates a new instance
     *
     * @param parent the parent window
     * @param attr   the terminals attributes
     */
    private TerminalDialog(JFrame parent, ElementAttributes attr) {
        super(parent, getDialogTitle(attr), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        width = attr.get(Keys.TERM_WIDTH);
        textArea = new JTextArea(attr.get(Keys.TERM_HEIGHT), width);
        textArea.setFont(new Font("monospaced", Font.PLAIN, Screen.getInstance().getFontSize()));
        getContentPane().add(new JScrollPane(textArea));

        JToolBar toolBar = new JToolBar();
        toolBar.add(new ToolTipAction(Lang.get("menu_terminalDelete"), CircuitComponent.ICON_DELETE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        }.setToolTip(Lang.get("menu_terminalDelete_tt")).createJButtonNoText());
        getContentPane().add(toolBar, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        addWindowFocusListener(new MoveFocusTo(parent));
    }

    /**
     * Adds a char to the terminal dialog
     *
     * @param value the character
     */
    private void addChar(char value) {
        switch (value) {
            case 13:
            case 10:
                pos = 0;
                textArea.append("\n");
                break;
            case 8:
                CharDeleter cd = new CharDeleter(textArea.getText(), pos).delete();
                textArea.setText(cd.getText());
                pos = cd.getPos();
                break;
            case 12:
                pos = 0;
                textArea.setText("");
                break;
            default:
                textArea.append("" + value);
                pos++;
                if (pos == width) {
                    pos = 0;
                    textArea.append("\n");
                }
        }
    }

    private static final class MyTerminal implements TerminalInterface {
        private final Model model;
        private final ElementAttributes attr;
        private final StringBuilder text;
        private TerminalDialog terminalDialog;

        private MyTerminal(Model model, ElementAttributes attr) {
            this.model = model;
            this.attr = attr;
            text = new StringBuilder();
        }

        @Override
        public void addChar(char value) {
            SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
                if (terminalDialog == null || !terminalDialog.isVisible()) {
                    terminalDialog = new TerminalDialog(model.getWindowPosManager().getMainFrame(), attr);
                    model.getWindowPosManager().register("terminal_" + attr.getLabel(), terminalDialog);
                }
                terminalDialog.addChar(value);
            }));
            if (text.length() < MAX_TERMINAL_STORED)
                text.append(value);
        }

        @Override
        public String getText() {
            return text.toString();
        }
    }
}
