package de.neemann.digital.gui.components.expression;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.builder.circuit.CircuitBuilder;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog to enter an expression
 * @author hneemann
 */
public class ExpressionDialog extends JDialog {

    /**
     * Creates a new instance
     *
     * @param parent       the parent
     * @param shapeFactory the shapeFactory used for new circuits
     */
    public ExpressionDialog(JFrame parent, ShapeFactory shapeFactory) {
        super(parent, Lang.get("expression"), false);

        JTextField text = new JTextField("(C ∨ B) ∧ (A ∨ C) ∧ (B ∨ !C) * (C + !A)", 40);
        getContentPane().add(text, BorderLayout.CENTER);
        getContentPane().add(new JLabel(Lang.get("msg_enterAnExpression")), BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        buttons.add(new ToolTipAction(Lang.get("btn_create")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Expression exp = new Parser(text.getText()).parse();
                    CircuitBuilder circuitBuilder = new CircuitBuilder(shapeFactory);
                    circuitBuilder.addCombinatorial("Y", exp);
                    Circuit circuit = circuitBuilder.createCircuit();
                    SwingUtilities.invokeLater(() -> new Main(null, circuit).setVisible(true));
                } catch (Exception ex) {
                    new ErrorMessage().addCause(ex).show(ExpressionDialog.this);
                }
            }
        }.setToolTip(Lang.get("btn_create_tt")).createJButton());

        pack();
        setLocationRelativeTo(parent);
    }
}
