package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.gui.components.OrderMerger;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Dialog which shows the data to plot.
 *
 * @author hneemann
 */
public class DataSetDialog extends JDialog implements ModelStateObserver {
    private static final int MAX_SAMPLE_SIZE = 1000;
    private final ArrayList<Model.Signal> signals;
    private final DataSetComponent dsc;
    private DataSet dataSet;
    private DataSetObserver dataSetObserver;

    /**
     * Creates a new instance
     *
     * @param owner    the parent frame
     * @param model    the model used to collect the data
     * @param type     the event type which triggers a new DataSample
     * @param ordering the ordering of the measurement values
     */
    public DataSetDialog(Frame owner, Model model, ModelEvent type, List<String> ordering) {
        super(owner, createTitle(type), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        signals = model.getSignalsCopy();
        new OrderMerger<String, Model.Signal>(ordering) {
            @Override
            public boolean equals(Model.Signal a, String b) {
                return a.getName().equals(b);
            }
        }.order(signals);

        dataSet = new DataSet(signals, MAX_SAMPLE_SIZE);

        dataSetObserver = new DataSetObserver(type, dataSet);

        dsc = new DataSetComponent(dataSet);
        JScrollPane scrollPane = new JScrollPane(dsc);
        getContentPane().add(scrollPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                model.addObserver(DataSetDialog.this);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                model.removeObserver(DataSetDialog.this);
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
                if (fileChooser.showSaveDialog(DataSetDialog.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().endsWith(".csv"))
                        file = new File(file.getParentFile(), file.getName() + ".csv");
                    try {
                        dataSet.saveCSV(file);
                    } catch (IOException e1) {
                        new ErrorMessage(Lang.get("msg_errorSavingData")).addCause(e1).show(DataSetDialog.this);
                    }
                }
            }
        }.setToolTip(Lang.get("menu_saveData_tt")).createJMenuItem());
        setJMenuBar(bar);

        pack();
        setLocationRelativeTo(owner);
    }

    private static String createTitle(ModelEvent type) {
        switch (type) {
            case MICROSTEP:
                return Lang.get("win_measures_microstep");
            case STEP:
                return Lang.get("win_measures_fullstep");
            default:
                return Lang.get("win_measures");
        }
    }


    @Override
    public void handleEvent(ModelEvent event) {
        dataSetObserver.handleEvent(event);
        dsc.revalidate();
        dsc.repaint();
    }
}
