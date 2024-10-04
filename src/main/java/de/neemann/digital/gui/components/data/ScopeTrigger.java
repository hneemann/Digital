/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.data.Value;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.OrderMerger;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.parser.TestRow;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.neemann.digital.core.element.PinInfo.input;
import static de.neemann.digital.gui.components.data.GraphDialog.createColumnsInfo;

/**
 * The ScopeElement
 */
public class ScopeTrigger extends Node implements Element {

    /**
     * Trigger mode
     */
    public enum Trigger {
        /**
         * rising edge
         */
        rising,
        /**
         * falling edge
         */
        falling,
        /**
         * both edges
         */
        both
    }

    /**
     * The ScopeElement description
     */
    public static final ElementTypeDescription DESCRIPTION =
            new ElementTypeDescription(ScopeTrigger.class, input("T").setClock())
                    .addAttribute(Keys.LABEL)
                    .addAttribute(Keys.TRIGGER)
                    .addAttribute(Keys.MAX_STEP_COUNT);

    private final int maxSize;
    private final String label;
    private final Trigger trigger;
    private ObservableValue clockValue;
    private boolean lastClock;
    private ValueTable logData;
    private ArrayList<Signal> signals;
    private Model model;
    private GraphDialog graphDialog;
    private boolean wasTrigger;
    private ScopeModelStateObserver scopeModelStateObserver;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public ScopeTrigger(ElementAttributes attr) {
        label = attr.getLabel();
        maxSize = attr.get(Keys.MAX_STEP_COUNT);
        trigger = attr.get(Keys.TRIGGER);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        clockValue = inputs.get(0).checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockValue.getBool();
        if (clock != lastClock) {
            switch (trigger) {
                case rising:
                    wasTrigger = !lastClock & clock;
                    break;
                case falling:
                    wasTrigger = lastClock & !clock;
                    break;
                default:
                    wasTrigger = true;
            }
        }
        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void init(Model model) throws NodeException {
        this.model = model;
        scopeModelStateObserver = new ScopeModelStateObserver();
        model.addObserver(scopeModelStateObserver, ModelEventType.STEP);
    }

    private ValueTable createLogData() {
        signals = model.getSignalsCopy();
        signals.removeIf(signal -> !signal.isShowInGraph());

        JFrame m = model.getWindowPosManager().getMainFrame();
        if (m instanceof Main) {
            List<String> ordering = ((Main) m).getCircuitComponent().getCircuit().getMeasurementOrdering();
            new OrderMerger<String, Signal>(ordering) {
                @Override
                public boolean equals(Signal a, String b) {
                    return a.getName().equals(b);
                }
            }.order(signals);
        }

        ArrayList<String> names = new ArrayList<>(signals.size());
        for (Signal signal : signals) names.add(signal.getName());
        return new ValueTable(names).setMaxSize(maxSize);
    }

    private final AtomicBoolean openPending = new AtomicBoolean();

    private final class ScopeModelStateObserver implements ModelStateObserver {
        @Override
        public void handleEvent(ModelEvent event) {
            if (wasTrigger && event.getType() == ModelEventType.STEP) {

                if (logData == null)
                    logData = createLogData();

                Value[] sample = new Value[logData.getColumns()];
                for (int i = 0; i < logData.getColumns(); i++)
                    sample[i] = new Value(signals.get(i).getValue());

                logData.add(new TestRow(sample));
                wasTrigger = false;

                if (graphDialog == null || !graphDialog.isVisible()) {
                    if (openPending.compareAndSet(false, true)) {
                        SwingUtilities.invokeLater(() -> {
                            String title = label;
                            if (title.isEmpty())
                                title = Lang.get("elem_ScopeTrigger_short");
                            GraphDialog gd = new GraphDialog(model.getWindowPosManager().getMainFrame(), title, logData, model, false)
                                    .setColumnInfo(createColumnsInfo(signals));

                            gd.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowClosed(WindowEvent e) {
                                    model.removeObserver(scopeModelStateObserver);
                                }
                            });
                            gd.setVisible(true);
                            model.getWindowPosManager().register("Scope_" + label, gd);

                            graphDialog = gd;
                            openPending.set(false);
                        });
                    }
                }
            }
        }
    }
}
