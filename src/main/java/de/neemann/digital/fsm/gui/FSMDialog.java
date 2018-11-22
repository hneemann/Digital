/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.fsm.FSM;
import de.neemann.digital.fsm.State;
import de.neemann.digital.gui.components.table.TableDialog;

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
        State top = new State("top");
        State topSetLeft = new State("topSetLeft").val("L", 1);
        State topSetRight = new State("topSetRight").val("R", 1);
        State leftA = new State("leftA");
        State leftB = new State("leftB");
        State bottom = new State("bottom");
        State bottomSetLeft = new State("bottomSetRight").val("R", 1);
        State bottomSetRight = new State("bottomSetLeft").val("L", 1);
        State rightA = new State("rightA");
        State rightB = new State("rightB");
        FSM fsm = new FSM(top, topSetLeft, leftA, leftB, bottomSetLeft, bottom, bottomSetRight, rightB, rightA, topSetRight)
                .transition(top, leftA, e("A & !B"))
                .transition(top, rightA, e("!A & B"))
                .transition(topSetLeft, top, null)
                .transition(topSetRight, top, null)

                .transition(rightA, top, e("!A & !B"))
                .transition(rightB, topSetRight, e("!A & !B"))
                .transition(leftA, top, e("!A & !B"))
                .transition(leftB, topSetLeft, e("!A & !B"))

                .transition(bottom, leftB, e("A & !B"))
                .transition(bottom, rightB, e("!A & B"))
                .transition(bottomSetLeft, bottom, null)
                .transition(bottomSetRight, bottom, null)

                .transition(rightB, bottom, e("A & B"))
                .transition(rightA, bottomSetRight, e("A & B"))
                .transition(leftB, bottom, e("A & B"))
                .transition(leftA, bottomSetLeft, e("A & B"));


        ElementLibrary lib = new ElementLibrary();
        ShapeFactory shapeFactory = new ShapeFactory(lib);
        new TableDialog(null, fsm.createTruthTable(), lib, shapeFactory, null).setVisible(true);

        //new FSMDialog(null, fsm).setVisible(true);

    }

    private static Expression e(String s) throws IOException, ParseException {
        return new Parser(s).parse().get(0);
    }
}
