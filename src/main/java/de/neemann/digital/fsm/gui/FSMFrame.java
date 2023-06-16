/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.FileLocator;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.core.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.fsm.*;
import de.neemann.digital.gui.*;
import de.neemann.digital.gui.components.table.ShowStringDialog;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.Preferences;

/**
 * The dialog to show the FSM
 */
public class FSMFrame extends JFrame implements ClosingWindowListener.ConfirmSave, FSM.ModifiedListener, FileHistory.OpenInterface {
    private static final Preferences PREFS = Preferences.userRoot().node("dig").node("fsm");
    private static final String PREF_FOLDER = "folder";
    private static final Icon ICON_NEW = IconCreator.create("document-new.png");
    private static final Icon ICON_OPEN = IconCreator.create("document-open.png");
    private static final Icon ICON_SAVE = IconCreator.create("document-save.png");
    private static final Icon ICON_SAVE_AS = IconCreator.create("document-save-as.png");
    private static final Icon ICON_EXPAND = IconCreator.create("View-zoom-fit.png");
    private static final Icon ICON_ZOOM_IN = IconCreator.create("View-zoom-in.png");
    private static final Icon ICON_ZOOM_OUT = IconCreator.create("View-zoom-out.png");
    private static final Icon ICON_HELP = IconCreator.create("help.png");

    private final FileHistory fileHistory;
    private final FSMComponent fsmComponent;
    private final Timer timer;
    private final JComboBox<String> moveControl;
    private FSM fsm;
    private ToolTipAction save;
    private File filename;
    private File baseFilename;
    private boolean lastModified;
    private String probeLabelName;
    private final GlobalValues.GlobalValueListener stateListener = new StateListener();

    /**
     * Opens the given file in a new frame
     *
     * @param file the file to open
     */
    public static void openFile(File file) {
        ElementLibrary library = new ElementLibrary();
        new ShapeFactory(library);
        SwingUtilities.invokeLater(() -> new FSMFrame(null, library, file).setVisible(true));
    }

    /**
     * Creates a new instance
     *
     * @param parent  the parents frame
     * @param library the library used to show the table
     */
    public FSMFrame(JFrame parent, ElementLibrary library) {
        this(parent, library, null);
    }

    /**
     * Creates a new instance
     *
     * @param parent   the parents frame
     * @param library  the library used to show the table
     * @param filename the file to open
     */
    public FSMFrame(JFrame parent, ElementLibrary library, File filename) {
        super(Lang.get("fsm_title"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImages(IconCreator.createImages("icon32.png", "icon64.png", "icon128.png"));

        fileHistory = new FileHistory(this, PREFS.node("hist"));

        fsmComponent = new FSMComponent();
        getContentPane().add(fsmComponent, BorderLayout.CENTER);

        timer = new Timer(100, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (fsm.getMovingState() == FSM.MovingState.STOP)
                    timer.stop();
                else {
                    for (int i = 0; i < 100; i++)
                        fsm.move(10, fsmComponent.getElementMoved());
                    repaint();
                }
            }
        });

        GlobalValues.getInstance().addListener(stateListener);
        addWindowListener(new ClosingWindowListener(this, this));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent windowEvent) {
                timer.stop();
                GlobalValues.getInstance().removeListener(stateListener);
            }
        });

        JMenuBar bar = new JMenuBar();
        JToolBar toolBar = new JToolBar();

        createFileMenu(bar, toolBar);
        toolBar.addSeparator();
        createViewMenu(bar, toolBar);
        toolBar.addSeparator();
        createCreateMenu(bar, library);

        bar.add(WindowManager.getInstance().registerAndCreateMenu(this));

        createHelpMenu(bar, toolBar);
        toolBar.addSeparator();

        moveControl = new JComboBox<>(new String[]{
                Lang.get("fsm_noMove"), Lang.get("fsm_moveTrans"), Lang.get("fsm_moveStates")});
        moveControl.setSelectedIndex(0);
        moveControl.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                switch (moveControl.getSelectedIndex()) {
                    case 0:
                        fsm.setMovingState(FSM.MovingState.STOP);
                        fsmComponent.repaint();
                        break;
                    case 1:
                        fsm.setMovingState(FSM.MovingState.TRANSITIONS);
                        timer.start();
                        break;
                    case 2:
                        fsm.setMovingState(FSM.MovingState.BOTH);
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
        new WindowSizeStorage("fsm").setDefaultSize(600, 600).restore(this);

        setFSM(new FSM());

        SwingUtilities.invokeLater(() -> {
            if (filename != null)
                loadFile(filename);
            else {
                File f = new FileLocator(probeLabelName)
                        .setBaseFile(baseFilename)
                        .setHistory(fileHistory)
                        .setLibrary(library)
                        .locate();
                if (f != null)
                    loadFile(f);
            }
        });

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
                        loadFile(SaveAsHelper.checkSuffix(fc.getSelectedFile(), "fsm"));
                    }
                }
            }
        }.setAcceleratorCTRLplus('O');

        JMenu openRecent = new JMenu(Lang.get("menu_openRecent"));
        fileHistory.setMenu(openRecent, null);

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

        JMenu export = new JMenu(Lang.get("menu_export"));
        export.add(new ExportAction(Lang.get("menu_exportSVG"), "svg", GraphicSVG::new));


        JMenu file = new JMenu(Lang.get("menu_file"));
        bar.add(file);
        file.add(newFile.createJMenuItem());
        file.add(open.createJMenuItem());
        file.add(openRecent);
        file.add(save.createJMenuItem());
        file.add(saveAs.createJMenuItem());
        file.add(export);

        toolBar.add(newFile.createJButtonNoText());
        toolBar.add(open.createJButtonNoText());
        toolBar.add(save.createJButtonNoText());
    }

    private void setFSM(FSM fsm) {
        this.fsm = fsm;
        fsmComponent.setFSM(fsm);
        fsm.setModifiedListener(this);
    }

    private JFileChooser getJFileChooser(File filename) {
        File folder = null;
        if (filename != null)
            folder = filename.getParentFile();

        if (folder == null && baseFilename != null)
            folder = baseFilename.getParentFile();

        if (folder == null) {
            String folderStr = PREFS.get(PREF_FOLDER, null);
            if (folderStr != null)
                folder = new File(folderStr);
        }

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

        if (fsm.isModified())
            fsmTitle = "*" + fsmTitle;
        setTitle(fsmTitle);

        this.filename = filename;
        if (filename != null)
            fileHistory.add(filename);
    }

    @Override
    public boolean isStateChanged() {
        return fsm.isModified();
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

    /**
     * Loads a file.
     *
     * @param file the file to load
     */
    public void loadFile(File file) {
        try {
            moveControl.setSelectedIndex(0);
            setFSM(FSM.loadFSM(file));
            setFilename(file);
            lastModified = fsm.isModified();
            PREFS.put(PREF_FOLDER, file.getParent());
        } catch (IOException e) {
            new ErrorMessage(Lang.get("msg_fsm_errorLoadingFile")).addCause(e).show(this);
        }
    }

    private void saveFile(File file) {
        try {
            moveControl.setSelectedIndex(0);
            fsm.save(file);
            setFilename(file);
            save.setEnabled(false);
            lastModified = fsm.isModified();
            PREFS.put(PREF_FOLDER, file.getParent());
        } catch (IOException e) {
            new ErrorMessage(Lang.get("msg_fsm_errorStoringFile")).addCause(e).show(this);
        }
    }

    @Override
    public void open(File file, boolean newWindow) {
        if (ClosingWindowListener.checkForSave(FSMFrame.this, FSMFrame.this))
            loadFile(file);
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
        }.setAcceleratorCTRLplus("PLUS");
        // enable [+] which is SHIFT+[=] on english keyboard layout
        fsmComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK, false), zoomIn);
        fsmComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK, false), zoomIn);
        fsmComponent.getActionMap().put(zoomIn, zoomIn);

        ToolTipAction zoomOut = new ToolTipAction(Lang.get("menu_zoomOut"), ICON_ZOOM_OUT) {
            @Override
            public void actionPerformed(ActionEvent e) {
                fsmComponent.scaleCircuit(0.9);
            }
        }.setAcceleratorCTRLplus("MINUS");
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

    private void createCreateMenu(JMenuBar bar, ElementLibrary library) {
        JMenu create = new JMenu(Lang.get("menu_fsm_create"));
        bar.add(create);
        create.add(new ToolTipAction(Lang.get("menu_fsm_create_table")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    new TableDialog(FSMFrame.this, fsm.createTruthTable(getStateSignalName()), library, filename).setVisible(true);
                } catch (Exception e) {
                    new ErrorMessage(Lang.get("msg_fsmCantCreateTable")).addCause(e).show(FSMFrame.this);
                }
            }
        }.createJMenuItem());

        JMenu counter = new JMenu(Lang.get("menu_fsm_create_counter"));
        create.add(counter);
        int[] counterValues = new int[]{4, 5, 6, 7, 8, 10, 16};
        for (int n : counterValues) {
            counter.add(new ToolTipAction(Lang.get("menu_fsm_create_counter_N", n)) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if (ClosingWindowListener.checkForSave(FSMFrame.this, FSMFrame.this)) {
                        setFSM(FSMDemos.counter(n).circle().setModified(false));
                        setFilename(null);
                    }
                }
            });
        }

        if (Main.isExperimentalMode()) {
            create.add(new ToolTipAction(Lang.get("menu_fsm_oneBitPerState")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    java.util.List<State> states = fsm.getStates();
                    if (states.size() < 32) {
                        int mask = 1;
                        for (State s : states) {
                            s.setNumber(mask);
                            mask *= 2;
                        }
                        fsmComponent.repaint();
                    }
                }
            }.setToolTip(Lang.get("menu_fsm_oneBitPerState_tt")).createJMenuItem());
            create.add(new ToolTipAction(Lang.get("menu_fsm_optimize_state_numbers")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        new OptimizerDialog(FSMFrame.this).setVisible(true);
                    } catch (FiniteStateMachineException | FormatterException | ExpressionException e) {
                        new ErrorMessage(Lang.get("menu_fsm_optimize_state_numbers_err")).addCause(e).show(FSMFrame.this);
                    } finally {
                        fsmComponent.repaint();
                    }
                }
            }.setToolTip(Lang.get("menu_fsm_optimize_state_numbers_tt")).createJMenuItem());
        }
    }

    /**
     * Sets a base file name which is used to determine a target directory
     * if no other name is available.
     *
     * @param filename the filename
     * @return this for chained calls
     */
    public FSMFrame setBaseFileName(File filename) {
        baseFilename = filename;
        return this;
    }

    /**
     * Sets the fsm name used in the circuit probe label which has opened
     * this window.
     *
     * @param probeLabelName the fsm name contained in the circuit
     * @return this for chained calls
     */
    public FSMFrame setProbeLabelName(String probeLabelName) {
        this.probeLabelName = probeLabelName;
        return this;
    }

    private void createHelpMenu(JMenuBar bar, JToolBar toolBar) {
        JMenu helpMenu = new JMenu(Lang.get("menu_help"));

        ToolTipAction viewHelp = new ToolTipAction(Lang.get("menu_help"), ICON_HELP) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ShowStringDialog(
                        FSMFrame.this,
                        Lang.get("msg_fsmHelpTitle"),
                        Lang.get("msg_fsmHelp"), true)
                        .setVisible(true);

            }
        }.setToolTip(Lang.get("menu_fsm_Help_tt"));

        helpMenu.add(viewHelp.createJMenuItem());
        helpMenu.add(InfoDialog.getInstance().createMenuItem(this, Lang.get("message")));
        bar.add(helpMenu);
        toolBar.add(viewHelp);
    }


    private class StateListener implements GlobalValues.GlobalValueListener {
        @Override
        public void valueCreated(String name, ObservableValue value, Model model) {
            if (name.equals(getStateSignalName())) {
                value.addObserverToValue(() -> setActiveState(value.getValue()));
                setActiveState(value.getValue());
                model.addObserver(event -> {
                            if (event == ModelEvent.CLOSED)
                                setActiveState(-1);
                        }, ModelEventType.CLOSED
                );
            }
        }
    }

    private String getStateSignalName() {
        if (filename != null)
            return filename.getName();
        else
            return "state";
    }

    private void setActiveState(long value) {
        if (fsm.setActiveStateTransition((int) value))
            SwingUtilities.invokeLater(fsmComponent::repaint);
    }

    /**
     * @return the current fsm
     */
    public FSM getFSM() {
        return fsm;
    }

    private class ExportAction extends ToolTipAction {
        private final String name;
        private final String suffix;
        private final ExportFactory exportFactory;

        ExportAction(String name, String suffix, ExportFactory exportFactory) {
            super(name);
            this.name = name;
            this.suffix = suffix;
            this.exportFactory = exportFactory;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new MyFileChooser();
            if (filename != null)
                fc.setSelectedFile(SaveAsHelper.checkSuffix(filename, suffix));

            ElementAttributes settings = Settings.getInstance().getAttributes();
            File exportDir = settings.getFile("exportDirectory");
            if (exportDir != null)
                fc.setCurrentDirectory(exportDir);

            fc.addChoosableFileFilter(new FileNameExtensionFilter(name, suffix));
            new SaveAsHelper(FSMFrame.this, fc, suffix).checkOverwrite(
                    file -> {
                        settings.setFile("exportDirectory", file.getParentFile());
                        try (OutputStream out = new FileOutputStream(file)) {
                            try (Graphic gr = exportFactory.create(out)) {
                                GraphicMinMax minMax = new GraphicMinMax(gr);
                                fsm.drawTo(minMax);

                                gr.setBoundingBox(minMax.getMin(), minMax.getMax());
                                fsm.drawTo(gr);
                            }
                        }
                    }
            );
        }

    }

    /**
     * A simple test method
     *
     * @param args the programs arguments
     */
    public static void main(String[] args) {
        ElementLibrary library = new ElementLibrary();
        new ShapeFactory(library);

        File f = null;
        if (args.length == 1)
            f = new File(args[0]);

        new FSMFrame(null, library, f).setVisible(true);
    }

}
