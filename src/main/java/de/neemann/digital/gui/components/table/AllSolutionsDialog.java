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
    private final JTextPane textPane;

    /**
     * Creates a new Frame
     *
     * @param owner the owner frame
     * @param font  the font to use
     */
    public AllSolutionsDialog(JFrame owner, Font font) {
        super(owner, Lang.get("win_allSolutions"), false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setFont(font);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        textPane.setEditable(false);
        textPane.setPreferredSize(new Dimension(600, 400));

        getContentPane().add(new JScrollPane(textPane));
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
        textPane.setText(text);
        return this;
    }
}
