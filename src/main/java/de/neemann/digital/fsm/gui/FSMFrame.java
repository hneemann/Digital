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
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * The dialog to show the FSM
 */
public class FSMFrame extends JFrame implements ClosingWindowListener.ConfirmSave, FSM.ModifiedListener {
    private static final Icon ICON_NEW = IconCreator.create("document-new.png");
    private static final Icon ICON_OPEN = IconCreator.create("document-open.png");
    private static final Icon ICON_SAVE = IconCreator.create("document-save.png");
    private static final Icon ICON_SAVE_AS = IconCreator.create("document-save-as.png");
    private static final Icon ICON_EXPAND = IconCreator.create("View-zoom-fit.png");
    private static final Icon ICON_ZOOM_IN = IconCreator.create("View-zoom-in.png");
    private static final Icon ICON_ZOOM_OUT = IconCreator.create("View-zoom-out.png");

    private FSM fsm;
    private final FSMComponent fsmComponent;
    private final Timer timer;
    private final JComboBox<String> moveControl;
    private boolean moveStates = false;
    private ToolTipAction save;
    private File filename;
    private boolean lastModified;

    /**
     * Use only for tests!
     *
     * @param givenFsm the fsm to visualize
     */
    public FSMFrame(FSM givenFsm) {
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
     * @param parent   the parents frame
     * @param givenFsm the fsm to visualize
     * @param library  the library used to show the table
     */
    public FSMFrame(JFrame parent, FSM givenFsm, ElementLibrary library) {
        super(Lang.get("fsm_title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        if (givenFsm == null) {
            givenFsm = FSMDemos.rotDecoder();
            givenFsm.circle();
        }

        fsmComponent = new FSMComponent();
        getContentPane().add(fsmComponent, BorderLayout.CENTER);

        timer = new Timer(100, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (int i = 0; i < 100; i++)
                    fsm.move(10, moveStates, fsmComponent.getElementMoved());
                repaint();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                timer.stop();
            }
        });

        JMenuBar bar = new JMenuBar();
        JToolBar toolBar = new JToolBar();

        createFileMenu(bar, toolBar);
        toolBar.addSeparator();
        createViewMenu(bar, toolBar);
        toolBar.addSeparator();

        JMenu create = new JMenu(Lang.get("menu_fsm_create"));
        bar.add(create);
        create.add(new ToolTipAction(Lang.get("menu_fsm_create_table")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    new TableDialog(FSMFrame.this, fsm.createTruthTable(), library, null).setVisible(true);
                } catch (Exception e) {
                    new ErrorMessage(Lang.get("msg_fsmCantCreateTable")).addCause(e).show(FSMFrame.this);
                }
            }
        }.createJMenuItem());


        moveControl = new JComboBox<>(new String[]{
                Lang.get("fsm_noMove"), Lang.get("fsm_moveTrans"), Lang.get("fsm_moveStates")});
        moveControl.setSelectedIndex(0);
        moveControl.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                switch (moveControl.getSelectedIndex()) {
                    case 0:
                        timer.stop();
                        fsm.toRaster();
                        fsmComponent.repaint();
                        break;
                    case 1:
                        if (moveStates)
                            fsm.toRaster();
                        moveStates = false;
                        timer.start();
                        break;
                    case 2:
                        moveStates = true;
                        timer.start();
                        break;
                }
            }
        });
        JPanel movePanel = new JPanel(new BorderLayout());
        movePanel.add(moveControl, BorderLayout.WEST);
        toolBar.add(movePanel);
        getContentPane().add(toolBar, BorderLayout.PAGE_START);

        setJMenuBar(bar);

        pack();
        setFSM(givenFsm);

        setLocationRelativeTo(parent);
    }

    private void createFileMenu(JMenuBar bar, JToolBar toolBar) {
        ToolTipAction newFile = new ToolTipAction(Lang.get("menu_new"), ICON_NEW) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(FSMFrame.this, FSMFrame.this)) {
                    setFSM(new FSM());
                    setFilename(null);
                }
            }
        }.setAcceleratorCTRLplus('N').setToolTip(Lang.get("menu_new_tt"));

        ToolTipAction open = new ToolTipAction(Lang.get("menu_open"), ICON_OPEN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(FSMFrame.this, FSMFrame.this)) {
                    JFileChooser fc = getJFileChooser(filename);
                    if (fc.showOpenDialog(FSMFrame.this) == JFileChooser.APPROVE_OPTION) {
                        loadFile(fc.getSelectedFile());
                    }
                }
            }
        }.setAcceleratorCTRLplus('O');

//        JMenu openRecent = new JMenu(Lang.get("menu_openRecent"));
//        JMenu openRecentNewWindow = new JMenu(Lang.get("menu_openRecentNewWindow"));
//        fileHistory.setMenu(openRecent, openRecentNewWindow);

        ToolTipAction saveAs = new ToolTipAction(Lang.get("menu_saveAs"), ICON_SAVE_AS) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getJFileChooser(filename);
                final SaveAsHelper saveAsHelper = new SaveAsHelper(FSMFrame.this, fc, "fsm");
                saveAsHelper.checkOverwrite(file -> saveFile(file));
            }
        };

        save = new ToolTipAction(Lang.get("menu_save"), ICON_SAVE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filename == null)
                    saveAs.actionPerformed(e);
                else
                    saveFile(filename);
            }
        }.setAcceleratorCTRLplus('S').setEnabledChain(false);

        JMenu file = new JMenu(Lang.get("menu_file"));
        bar.add(file);
        file.add(newFile.createJMenuItem());
        file.add(open.createJMenuItem());
        file.add(save.createJMenuItem());
        file.add(saveAs.createJMenuItem());

        toolBar.add(newFile.createJButtonNoText());
        toolBar.add(open.createJButtonNoText());
        toolBar.add(save.createJButtonNoText());
    }

    private void setFSM(FSM fsm) {
        this.fsm = fsm;
        fsmComponent.setFSM(fsm);
        fsm.setModifiedListener(this);
    }

    private static JFileChooser getJFileChooser(File filename) {
        File folder = null;
        if (filename != null)
            folder = filename.getParentFile();

        JFileChooser fileChooser = new MyFileChooser(folder);
        fileChooser.setFileFilter(new FileNameExtensionFilter("FSM", "fsm"));
        return fileChooser;
    }

    private void setFilename(File filename) {
        String fsmTitle;
        if (filename == null)
            fsmTitle = Lang.get("fsm_title");
        else
            fsmTitle = filename.toString() + " - " + Lang.get("fsm_title");

        if (fsm.hasChanged())
            fsmTitle = "*" + fsmTitle;
        setTitle(fsmTitle);

        this.filename = filename;
    }

    @Override
    public boolean isStateChanged() {
        return fsm.hasChanged();
    }

    @Override
    public void saveChanges() {
        save.actionPerformed(null);
    }

    @Override
    public void modifiedChanged(boolean modified) {
        if (lastModified != modified) {
            lastModified = modified;
            setFilename(filename);
            save.setEnabled(modified);
        }
    }

    private void loadFile(File file) {
        try {
            moveControl.setSelectedIndex(0);
            setFSM(FSM.loadFSM(file));
            setFilename(file);
        } catch (IOException e) {
            new ErrorMessage(Lang.get("msg_fsm_errorLoadingFile")).addCause(e).show(this);
        }
    }

    private void saveFile(File file) {
        try {
            fsm.save(file);
            setFilename(file);
            save.setEnabled(false);
        } catch (IOException e) {
            new ErrorMessage(Lang.get("msg_fsm_errorStoringFile")).addCause(e).show(this);
        }
    }

    private void createViewMenu(JMenuBar menuBar, JToolBar toolBar) {
        ToolTipAction maximize = new ToolTipAction(Lang.get("menu_maximize"), ICON_EXPAND) {
            @Override
            public void actionPerformed(ActionEvent e) {
                fsmComponent.fitFSM();
            }
        }.setAccelerator("F1");
        ToolTipAction zoomIn = new ToolTipAction(Lang.get("menu_zoomIn"), ICON_ZOOM_IN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                fsmComponent.scaleCircuit(1 / 0.9);
            }
        }.setAccelerator("control PLUS");
        // enable [+] which is SHIFT+[=] on english keyboard layout
        fsmComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK, false), zoomIn);
        fsmComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK, false), zoomIn);
        fsmComponent.getActionMap().put(zoomIn, zoomIn);

        ToolTipAction zoomOut = new ToolTipAction(Lang.get("menu_zoomOut"), ICON_ZOOM_OUT) {
            @Override
            public void actionPerformed(ActionEvent e) {
                fsmComponent.scaleCircuit(0.9);
            }
        }.setAccelerator("control MINUS");
        // enable [+] which is SHIFT+[=] on english keyboard layout
        fsmComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK, false), zoomOut);
        fsmComponent.getActionMap().put(zoomOut, zoomOut);

        toolBar.add(zoomIn.createJButtonNoText());
        toolBar.add(zoomOut.createJButtonNoText());
        toolBar.add(maximize.createJButtonNoText());

        JMenu view = new JMenu(Lang.get("menu_view"));
        menuBar.add(view);
        view.add(maximize.createJMenuItem());
        view.add(zoomOut.createJMenuItem());
        view.add(zoomIn.createJMenuItem());
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

        new FSMFrame(null, fsm.circle(), library).setVisible(true);
    }

}
