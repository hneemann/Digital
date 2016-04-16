package de.neemann.digital.draw.model;

import de.neemann.digital.core.*;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.gui.ErrorStopper;
import de.neemann.digital.gui.GuiModelObserver;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The real time clock which is used to fire the models clocks with realtime signals
 * @author hneemann
 */
public class RealTimeClock implements ModelStateObserver {
    private final Model model;
    private final ScheduledThreadPoolExecutor executor;
    private final ErrorStopper stopper;
    private final int frequency;
    private final ObservableValue output;
    private ScheduledFuture<?> timer;

    /**
     * Creates a new real time clock
     *
     * @param model    the model
     * @param clock    the clock element which is modify
     * @param executor the executor used to schedule the update
     */
    public RealTimeClock(Model model, Clock clock, ScheduledThreadPoolExecutor executor, ErrorStopper stopper) {
        this.model = model;
        this.executor = executor;
        this.stopper = stopper;
        int f = clock.getFrequency();
        if (f < 1) f = 1;
        this.frequency = f;
        this.output = clock.getClockOutput();
    }

    @Override
    public void handleEvent(ModelEvent event) {
        switch (event) {
            case STARTED:
                if (frequency > 50)  // if frequency is high it is not necessary to update the GUI at every clock
                    output.removeObserver(GuiModelObserver.class);

                int delay = 500 / frequency;
                if (delay < 1) delay = 1;

                timer = executor.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
                    output.setValue(1 - output.getValue());
                    try {
                        model.doStep();
                    } catch (NodeException e1) {
                        stopper.showErrorAndStopModel(Lang.get("msg_clockError"), e1);
                        timer.cancel(false);
                    }
                }), delay, delay, TimeUnit.MILLISECONDS);

                break;
            case STOPPED:
                if (timer != null)
                    timer.cancel(false);
                break;
        }
    }
}
