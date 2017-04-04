package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.Signal;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.components.OrderMerger;
import de.neemann.digital.gui.sync.Sync;
import de.neemann.digital.lang.Lang;
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
    private DataSet dataSet;
    private DataSetObserver dataSetObserver;

    /**
     * Creates a new instance
     *
     * @param owner     the parent frame
     * @param model     the model used to collect the data
     * @param microStep true     the event type which triggers a new DataSample
     * @param ordering  the ordering of the measurement values
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

        dataSet = new DataSet(signals, MAX_SAMPLE_SIZE);

        dataSetObserver = new DataSetObserver(microStep, dataSet);

        dsc = new DataSetComponent(dataSet);
        scrollPane = new JScrollPane(dsc);
        getContentPane().add(scrollPane);

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
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Comma Separated Values", "csv"));
                new SaveAsHelper(DataSetDialog.this, fileChooser, "csv")
                        .checkOverwrite(file -> dataSet.saveCSV(file));
            }
        }.setToolTip(Lang.get("menu_saveData_tt")).createJMenuItem());
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
            JScrollBar bar = scrollPane.getHorizontalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}
