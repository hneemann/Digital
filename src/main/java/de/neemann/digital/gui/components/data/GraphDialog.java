package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.Signal;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.components.OrderMerger;
import de.neemann.digital.gui.sync.NoSync;
import de.neemann.digital.gui.sync.Sync;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.IconCreator;
import de.neemann.gui.MyFileChooser;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The Dialog which shows the data to plot.
 *
 * @author hneemann
 */
public class GraphDialog extends JDialog implements ModelStateObserver {
    private static final int MAX_SAMPLE_SIZE = 1000;
    private final GraphComponent dsc;
    private final JScrollPane scrollPane;
    private final Sync modelSync;
    private ValueTableObserver valueTableObserver;

    private static final Icon ICON_EXPAND = IconCreator.create("View-zoom-fit.png");
    private static final Icon ICON_ZOOM_IN = IconCreator.create("View-zoom-in.png");
    private static final Icon ICON_ZOOM_OUT = IconCreator.create("View-zoom-out.png");


    /**
     * Creates a instance prepared for "live logging"
     *
     * @param owner     the parent frame
     * @param model     the model
     * @param microStep stepping mode
     * @param ordering  the ordering to use
     * @param modelSync the lock to access the model
     * @return the created instance
     */
    public static GraphDialog createLiveDialog(Frame owner, Model model, boolean microStep, List<String> ordering, Sync modelSync) {
        String title;
        if (microStep)
            title = Lang.get("win_measures_microstep");
        else
            title = Lang.get("win_measures_fullstep");

        ArrayList<Signal> signals = model.getSignalsCopy();
        new OrderMerger<String, Signal>(ordering) {
            @Override
            public boolean equals(Signal a, String b) {
                return a.getName().equals(b);
            }
        }.order(signals);

        ValueTableObserver valueTableObserver = new ValueTableObserver(microStep, signals, MAX_SAMPLE_SIZE);
        ValueTable logData = valueTableObserver.getLogData();

        return new GraphDialog(owner, title, model, logData, valueTableObserver, modelSync);
    }

    /**
     * Creates a new instance
     *
     * @param owner   the parent frame
     * @param title   the frame title
     * @param logData the data to visualize
     */
    public GraphDialog(Frame owner, String title, ValueTable logData) {
        this(owner, title, null, logData, null, NoSync.INST);
    }

    /**
     * Creates a new instance
     *
     * @param owner     the parent frame
     * @param title     the frame title
     * @param model     the model used to collect the data
     * @param logData   the data to visualize
     * @param modelSync used to access the running model
     */
    private GraphDialog(Frame owner, String title, Model model, ValueTable logData, ValueTableObserver valueTableObserver, Sync modelSync) {
        super(owner, title, false);
        this.valueTableObserver = valueTableObserver;
        this.modelSync = modelSync;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        dsc = new GraphComponent(logData, modelSync);
        scrollPane = new JScrollPane(dsc);
        getContentPane().add(scrollPane);
        dsc.setScrollPane(scrollPane);

        JToolBar toolBar = new JToolBar();
        ToolTipAction maximize = new ToolTipAction(Lang.get("menu_maximize"), ICON_EXPAND) {
            @Override
            public void actionPerformed(ActionEvent e) {
                dsc.fitData(scrollPane.getWidth() - scrollPane.getInsets().left - scrollPane.getInsets().right);
            }
        }.setAccelerator("F1");
        ToolTipAction zoomIn = new ToolTipAction(Lang.get("menu_zoomIn"), ICON_ZOOM_IN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rectangle r = scrollPane.getViewport().getViewRect();
                dsc.scale(1.25f, r.x + r.width / 2);
            }
        }.setAccelerator("control PLUS");
        ToolTipAction zoomOut = new ToolTipAction(Lang.get("menu_zoomOut"), ICON_ZOOM_OUT) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Rectangle r = scrollPane.getViewport().getViewRect();
                dsc.scale(0.8f, r.x + r.width / 2);
            }
        }.setAccelerator("control MINUS");

        toolBar.add(zoomIn.createJButtonNoText());
        toolBar.add(zoomOut.createJButtonNoText());
        toolBar.add(maximize.createJButtonNoText());

        getContentPane().add(toolBar, BorderLayout.NORTH);
        pack();

        if (model != null)
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    modelSync.access(() -> model.addObserver(GraphDialog.this));
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    modelSync.access(() -> model.removeObserver(GraphDialog.this));
                }
            });

        scrollPane.getViewport().setPreferredSize(dsc.getPreferredSize());

        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu(Lang.get("menu_file"));
        bar.add(file);
        file.add(new ToolTipAction(Lang.get("menu_saveData")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new MyFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Comma Separated Values", "csv"));
                new SaveAsHelper(GraphDialog.this, fileChooser, "csv")
                        .checkOverwrite(logData::saveCSV);
            }
        }.setToolTip(Lang.get("menu_saveData_tt")).createJMenuItem());

        JMenu view = new JMenu(Lang.get("menu_view"));
        bar.add(view);
        view.add(maximize.createJMenuItem());
        view.add(zoomOut.createJMenuItem());
        view.add(zoomIn.createJMenuItem());

        setJMenuBar(bar);
        pack();
        setLocationRelativeTo(owner);
    }

    @Override
    public void handleEvent(ModelEvent event) {
        modelSync.access(() -> {
            valueTableObserver.handleEvent(event);
        });
        SwingUtilities.invokeLater(() -> {
            dsc.revalidate();
            dsc.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = scrollPane.getHorizontalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        });
    }
}
