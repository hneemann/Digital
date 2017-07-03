package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.Signal;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.components.OrderMerger;
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
public class DataSetDialog extends JDialog implements ModelStateObserver {
    private static final int MAX_SAMPLE_SIZE = 1000;
    private final DataSetComponent dsc;
    private final JScrollPane scrollPane;
    private final Sync modelSync;
    private ValueTable logData;
    private DataSetObserver dataSetObserver;

    private static final Icon ICON_EXPAND = IconCreator.create("View-zoom-fit.png");
    private static final Icon ICON_ZOOM_IN = IconCreator.create("View-zoom-in.png");
    private static final Icon ICON_ZOOM_OUT = IconCreator.create("View-zoom-out.png");

    /**
     * Creates a new instance
     *
     * @param owner     the parent frame
     * @param model     the model used to collect the data
     * @param microStep true     the event type which triggers a new DataSample
     * @param ordering  the ordering of the measurement values
     * @param modelSync used to access the running model
     */
    public DataSetDialog(Frame owner, Model model, boolean microStep, List<String> ordering, Sync modelSync) {
        super(owner, createTitle(microStep), false);
        this.modelSync = modelSync;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        ArrayList<Signal> signals = model.getSignalsCopy();
        new OrderMerger<String, Signal>(ordering) {
            @Override
            public boolean equals(Signal a, String b) {
                return a.getName().equals(b);
            }
        }.order(signals);


        dataSetObserver = new DataSetObserver(microStep, signals, MAX_SAMPLE_SIZE);
        logData = dataSetObserver.getLogData();

        dsc = new DataSetComponent(logData);
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                modelSync.access(() -> model.addObserver(DataSetDialog.this));
            }

            @Override
            public void windowClosed(WindowEvent e) {
                modelSync.access(() -> model.removeObserver(DataSetDialog.this));
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
                new SaveAsHelper(DataSetDialog.this, fileChooser, "csv")
                        .checkOverwrite(file -> logData.saveCSV(file));
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

    private static String createTitle(boolean microStep) {
        if (microStep)
            return Lang.get("win_measures_microstep");
        else
            return Lang.get("win_measures_fullstep");
    }


    @Override
    public void handleEvent(ModelEvent event) {
        modelSync.access(() -> {
            dataSetObserver.handleEvent(event);
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
