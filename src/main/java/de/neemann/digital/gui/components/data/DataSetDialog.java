package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.gui.components.OrderMerger;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
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
    private final ModelEvent.Event type;
    private final ArrayList<Model.Signal> signals;
    private final DataSetComponent dsc;
    private DataSample manualSample;
    private int maintime;
    private DataSet dataSet;

    /**
     * Creates a new instance
     *
     * @param owner    the parent frame
     * @param model    the model used to collect the data
     * @param type     the event type which triggers a new DataSample
     * @param ordering
     */
    public DataSetDialog(Frame owner, Model model, ModelEvent.Event type, List<String> ordering) {
        super(owner, Lang.get("win_measures"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        this.type = type;

        signals = model.getSignalsCopy();
        new OrderMerger<String, Model.Signal>(ordering) {
            @Override
            public boolean equals(Model.Signal a, String b) {
                return a.getName().equals(b);
            }
        }.order(signals);

        dataSet = new DataSet(signals);

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

        pack();
        setLocationRelativeTo(owner);
    }


    @Override
    public void handleEvent(ModelEvent event) {
        if (event.getType() == ModelEvent.Event.MANUALCHANGE) {
            if (manualSample == null)
                manualSample = new DataSample(maintime, signals.size());
            manualSample.fillWith(signals);
        }

        if (event.getType() == type) {
            if (manualSample != null) {
                dataSet.add(manualSample);
                manualSample = null;
                maintime++;
            }
            dataSet.add(new DataSample(maintime, signals.size()).fillWith(signals));
            maintime++;
        }
        dsc.revalidate();
        dsc.repaint();
    }
}
