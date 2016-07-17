package de.neemann.digital.gui.components.table;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Dialog to show all possible functions of a truth table
 *
 * @author hneemann
 */
public class AllSolutionsDialog extends JDialog {
    private final JTextArea textArea;

    /**
     * Creates a new Frame
     *
     * @param owner the owner frame
     * @param font  the font to use
     */
    public AllSolutionsDialog(JFrame owner, Font font) {
        super(owner, Lang.get("win_allSolutions"), false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        textArea = new JTextArea(6, 30);
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setTabSize(3);

        getContentPane().add(new JScrollPane(textArea));
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Sets the given text to the frame
     *
     * @param text the text
     * @return this for call chaining
     */
    public AllSolutionsDialog setText(String text) {
        textArea.setText(text);
        return this;
    }
}
