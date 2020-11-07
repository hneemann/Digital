/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.fsm.FiniteStateMachineException;
import de.neemann.digital.fsm.Optimizer;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog to show and control the fsm optimizer
 */
public class OptimizerDialog extends JDialog {

    private final Optimizer optimizer;
    private final JLabel bestLabel;

    /**
     * Creates a new instance.
     *
     * @param owner the owner
     * @throws FiniteStateMachineException FiniteStateMachineException
     * @throws FormatterException          FormatterException
     * @throws ExpressionException         ExpressionException
     */
    public OptimizerDialog(FSMFrame owner) throws FiniteStateMachineException, FormatterException, ExpressionException {
        super(owner, Lang.get("msg_fsm_optimizer"), true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        optimizer = new Optimizer(owner.getFSM()).optimizeFSMParallel(new SwingListener());

        GridLayout layout = new GridLayout(2, 2);
        JPanel panel = new JPanel(layout);
        panel.add(addBorder(new JLabel(Lang.get("msg_fsm_optimizer_initial"))));
        panel.add(addBorder(new JLabel(Integer.toString(optimizer.getInitialComplexity()))));
        panel.add(addBorder(new JLabel(Lang.get("msg_fsm_optimizer_best"))));
        bestLabel = new JLabel(Integer.toString(optimizer.getInitialComplexity()));
        panel.add(addBorder(bestLabel));

        getContentPane().add(panel, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.add(new ToolTipAction(Lang.get("cancel")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                optimizer.stop();
                this.setEnabled(false);
            }
        }.createJButton(), BorderLayout.SOUTH);
        getContentPane().add(addBorder(buttons), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private Component addBorder(JComponent component) {
        component.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        return component;
    }

    private final class SwingListener implements Optimizer.EventListener {

        @Override
        public void bestSoFar(int[] best, int bestComplexity) {
            SwingUtilities.invokeLater(() -> bestLabel.setText(Integer.toString(bestComplexity)));
        }

        @Override
        public void finished() {
            SwingUtilities.invokeLater(() -> {
                optimizer.applyBest();
                dispose();
            });
        }

    }
}
