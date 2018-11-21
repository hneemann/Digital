/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.fsm.FSM;
import de.neemann.digital.fsm.State;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * The dialog to show the FSM
 */
public class FSMDialog extends JDialog {

    private final FSM fsm;
    private final FSMComponent fsmComponent;
    private final Timer timer;

    /**
     * Creates a new instance
     *
     * @param frame the parents frame
     * @param fsm   the fsm to visualize
     */
    public FSMDialog(Frame frame, FSM fsm) {
        super(frame, "FSM");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.fsm = fsm;


        fsmComponent = new FSMComponent(fsm);
        getContentPane().add(fsmComponent);
        pack();
        setLocationRelativeTo(frame);

        fsmComponent.fitFSM();

        timer = new Timer(100, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fsm.calculateForces();
                fsm.move(100);
                repaint();
            }
        });

        timer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                timer.stop();
            }
        });
    }

    /**
     * A simple test method
     *
     * @param args the programs arguments
     * @throws Exception Exception
     */
    public static void main(String[] args) throws Exception {
        /*
        FSM fsm = new FSM()
                .add(new State("initial").val("Y", 0))
                .add(new State("1 match").val("Y", 0))
                .add(new State("2 matches").val("Y", 0))
                .add(new State("found").val("Y", 1))
                .transition(0, 1, new Parser("!E").parse().get(0))
                .transition(1, 2, new Parser("!E").parse().get(0))
                .transition(2, 3, new Parser("E").parse().get(0))

                .transition(1, 0, new Parser("E").parse().get(0))
                .transition(3, 0, new Parser("E").parse().get(0))
                .transition(3, 1, new Parser("!E").parse().get(0));*/

        State top = new State("top");
        State topSet = new State("topSet").val("Y", 1);
        State leftA = new State("leftA");
        State leftB = new State("leftB");
        State bottom = new State("bottom");
        State bottomSet = new State("bottomSet").val("Y", 1);
        State rightA = new State("rightA");
        State rightB = new State("rightB");
        FSM fsm = new FSM(top, topSet, leftA, leftB, bottom, bottomSet, rightA, rightB)
                .transition(top, leftA, e("A & !B"))
                .transition(top, rightA, e("!A & B"))
                .transition(topSet, top, null)

                .transition(rightA, top, e("!A & !B"))
                .transition(rightB, topSet, e("!A & !B"))
                .transition(leftA, top, e("!A & !B"))
                .transition(leftB, topSet, e("!A & !B"))

                .transition(bottom, leftB, e("A & !B"))
                .transition(bottom, rightB, e("!A & B"))
                .transition(bottomSet, bottom, null)

                .transition(rightB, bottom, e("A & B"))
                .transition(rightA, bottomSet, e("A & B"))
                .transition(leftB, bottom, e("A & B"))
                .transition(leftA, bottomSet, e("A & B"));


        new FSMDialog(null, fsm).setVisible(true);

    }

    private static Expression e(String s) throws IOException, ParseException {
        return new Parser(s).parse().get(0);
    }
}
