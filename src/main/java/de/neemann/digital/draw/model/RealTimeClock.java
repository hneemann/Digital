package de.neemann.digital.draw.model;

import de.neemann.digital.core.*;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.gui.ErrorStopper;
import de.neemann.digital.gui.GuiModelObserver;
import de.neemann.digital.gui.sync.Sync;
import de.neemann.digital.lang.Lang;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The real time clock which is used to fire the models clocks with realtime signals
 *
 * @author hneemann
 */
public class RealTimeClock implements ModelStateObserver {
    private final Model model;
    private final ScheduledThreadPoolExecutor executor;
    private final ErrorStopper stopper;
    private final Sync modelSync;
    private final int frequency;
    private final ObservableValue output;
    private Runner runner;

    /**
     * Creates a new real time clock
     *
     * @param model    the model
     * @param clock    the clock element which is modify
     * @param executor the executor used to schedule the update
     */
    public RealTimeClock(Model model, Clock clock, ScheduledThreadPoolExecutor executor, ErrorStopper stopper, Sync modelSync) {
        this.model = model;
        this.executor = executor;
        this.stopper = stopper;
        this.modelSync = modelSync;
        int f = clock.getFrequency();
        if (f < 1) f = 1;
        this.frequency = f;
        this.output = clock.getClockOutput();
    }

    @Override
    public void handleEvent(ModelEvent event) {
        switch (event) {
            case STARTED:
                if (frequency > 50)  // if frequency is high it is not necessary to update the GUI at every clock change
                    output.removeObserver(GuiModelObserver.class);

                int delay = 500000 / frequency;
                if (delay < 10)
                    runner = new ThreadRunner();
                else
                    runner = new RealTimeRunner(delay);
                break;
            case STOPPED:
                if (runner != null)
                    runner.stop();
                break;
        }
    }

    interface Runner {
        void stop();
    }

    /**
     * runs with defined rate
     */
    private class RealTimeRunner implements Runner {

        private final ScheduledFuture<?> timer;

        RealTimeRunner(int delay) {
            timer = executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        modelSync.accessNEx(() -> {
                            output.setValue(1 - output.getValue());
                            model.doStep();
                        });
                    } catch (NodeException e1) {
                        stopper.showErrorAndStopModel(Lang.get("msg_clockError"), e1);
                        timer.cancel(false);
                    }
                }
            }, delay, delay, TimeUnit.MICROSECONDS);
        }

        @Override
        public void stop() {
            if (timer != null)
                timer.cancel(false);
        }
    }

    /**
     * runs at fast as possible!
     */
    private class ThreadRunner implements Runner {

        private final Thread thread;

        ThreadRunner() {
            thread = new Thread() {
                @Override
                public void run() {
                    System.out.println("thread start");
                    try {
                        while (!interrupted()) {
                            modelSync.accessNEx(() -> {
                                output.setValue(1 - output.getValue());
                                model.doStep();
                            });
                        }
                    } catch (NodeException e1) {
                        stopper.showErrorAndStopModel(Lang.get("msg_clockError"), e1);
                    }
                    System.out.println("thread end");
                }
            };
            thread.setDaemon(true);
            thread.start();
        }

        @Override
        public void stop() {
            thread.interrupt();
        }
    }
}
