/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.fsm.FSM;
import de.neemann.digital.fsm.FSMDemos;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The dialog to show the FSM
 */
public class FSMDialog extends JDialog {

    private final FSM fsm;
    private final FSMComponent fsmComponent;
    private final Timer timer;
    private boolean moveStates = false;

    /**
     * Use only for tests!
     *
     * @param givenFsm the fsm to visualize
     */
    public FSMDialog(FSM givenFsm) {
        this(null, givenFsm, createLibrary());
    }

    private static ElementLibrary createLibrary() {
        ElementLibrary library = new ElementLibrary();
        new ShapeFactory(library);
        return library;
    }


    /**
     * Creates a new instance
     *
     * @param frame    the parents frame
     * @param givenFsm the fsm to visualize
     * @param library  the library used to show the table
     */
    public FSMDialog(Frame frame, FSM givenFsm, ElementLibrary library) {
        super(frame, Lang.get("fsm_title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        if (givenFsm == null)
            givenFsm = FSMDemos.rotDecoder();

        this.fsm = givenFsm;

        fsmComponent = new FSMComponent(fsm);
        getContentPane().add(fsmComponent, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(frame);

        fsmComponent.fitFSM();

        timer = new Timer(100, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (int i = 0; i < 100; i++)
                    fsm.move(10, moveStates, fsmComponent.getElementMoved());
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

        JMenuBar bar = new JMenuBar();

        JMenu create = new JMenu(Lang.get("menu_fsm_create"));
        bar.add(create);
        create.add(new ToolTipAction(Lang.get("menu_fsm_create_table")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    new TableDialog(FSMDialog.this, fsm.createTruthTable(), library, null).setVisible(true);
                } catch (Exception e) {
                    new ErrorMessage(Lang.get("msg_fsmCantCreateTable")).addCause(e).show(FSMDialog.this);
                }
            }
        }.createJMenuItem());

        JToolBar toolBar = new JToolBar();

        final JCheckBox moveCheck = new JCheckBox(Lang.get("fsm_move"));
        moveCheck.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                moveStates = moveCheck.isSelected();
                if (!moveStates) {
                    fsm.toRaster();
                    repaint();
                }
            }
        });
        moveCheck.setSelected(moveStates);
        toolBar.add(moveCheck);
        getContentPane().add(toolBar, BorderLayout.PAGE_START);


        setJMenuBar(bar);

    }

    /**
     * A simple test method
     *
     * @param args the programs arguments
     */
    public static void main(String[] args) {

        FSM fsm = FSMDemos.rotDecoder();

        ElementLibrary library = new ElementLibrary();
        new ShapeFactory(library);

        new FSMDialog(null, fsm, library).setVisible(true);

    }
}
