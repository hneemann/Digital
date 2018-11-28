/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.fsm.FSM;
import de.neemann.digital.fsm.FSMDemos;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.Settings;
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
public class FSMFrame extends JFrame implements ClosingWindowListener.ConfirmSave, FSM.ModifiedListener {
    private static final Preferences PREFS = Preferences.userRoot().node("dig").node("fsm");
    private static final String PREF_FOLDER = "folder";
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
    private File baseFilename;
    private boolean lastModified;

    /**
     * Creates a new instance
     *
     * @param parent   the parents frame
     * @param givenFsm the fsm to visualize
     * @param library  the library used to show the table
     */
    public FSMFrame(JFrame parent, FSM givenFsm, ElementLibrary library) {
        super(Lang.get("fsm_title"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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

        addWindowListener(new ClosingWindowListener(this, this));
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
        createCreateMenu(bar, library);


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

        JMenu export = new JMenu(Lang.get("menu_export"));
        export.add(new ExportAction(Lang.get("menu_exportSVG"), "svg", GraphicSVGIndex::new));
        export.add(new ExportAction(Lang.get("menu_exportSVGLaTex"), "svg", GraphicSVGLaTeX::new));


        JMenu file = new JMenu(Lang.get("menu_file"));
        bar.add(file);
        file.add(newFile.createJMenuItem());
        file.add(open.createJMenuItem());
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

    private void createCreateMenu(JMenuBar bar, ElementLibrary library) {
        JMenu create = new JMenu(Lang.get("menu_fsm_create"));
        bar.add(create);
        create.add(new ToolTipAction(Lang.get("menu_fsm_create_table")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    new TableDialog(FSMFrame.this, fsm.createTruthTable(), library, filename).setVisible(true);
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
        FSM fsm = FSMDemos.rotDecoder();

        ElementLibrary library = new ElementLibrary();
        new ShapeFactory(library);

        new FSMFrame(null, fsm.circle().setModified(false), library).setVisible(true);
    }

}
