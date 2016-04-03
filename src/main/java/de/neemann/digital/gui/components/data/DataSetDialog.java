package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class DataSetDialog extends JDialog implements ModelStateObserver {
    private final ModelEvent.Event type;
    private final ArrayList<Model.Signal> signals;
    private final DataSetComponent dsc;
    private DataSample manualSample;
    private int maintime;
    private DataSet dataSet;

    public DataSetDialog(Frame owner, Model model, ModelEvent.Event type) {
        super(owner, Lang.get("win_measures"), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        this.type = type;

        signals = model.getSignals();
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
        dsc.repaint();
    }
}
