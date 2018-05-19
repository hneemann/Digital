/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.*;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.gui.ErrorStopper;
import de.neemann.digital.gui.GuiModelObserver;
import de.neemann.digital.gui.StatusInterface;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The real time clock which is used to fire the models clocks with real time signals
 * If the maximum frequency is selected a dedicated thread is started which runs the model.
 * So you get the highest speed but no real time. The model runs as fast as possible.
 */
public class RealTimeClock implements ModelStateObserverTyped {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeClock.class);
    private static final int THREAD_RUNNER_DELAY = 100;

    private final Model model;
    private final ScheduledThreadPoolExecutor executor;
    private final ErrorStopper stopper;
    private final StatusInterface status;
    private final int frequency;
    private final ObservableValue output;
    private Runner runner;

    /**
     * Creates a new real time clock
     *
     * @param model     the model
     * @param clock     the clock element which is modify
     * @param executor  the executor used to schedule the update
     * @param stopper   used to stop the model if an error is detected
     * @param status    allows sending messages to the status line
     */
    public RealTimeClock(Model model, Clock clock, ScheduledThreadPoolExecutor executor, ErrorStopper stopper, StatusInterface status) {
        this.model = model;
        this.executor = executor;
        this.stopper = stopper;
        this.status = status;
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
                    model.access(() -> output.removeObserver(GuiModelObserver.class));

                int delayMuS = 500000 / frequency;
                if (delayMuS < THREAD_RUNNER_DELAY)
                    runner = new ThreadRunner();
                else
                    runner = new RealTimeRunner(delayMuS);
                break;
            case STOPPED:
                if (runner != null)
                    runner.stop();
                break;
        }
    }

    @Override
    public ModelEvent[] getEvents() {
        return new ModelEvent[]{ModelEvent.STARTED, ModelEvent.STOPPED};
    }

    /**
     * @return true if a thread runner is used
     */
    public boolean isThreadRunner() {
        int delayMuS = 500000 / frequency;
        return delayMuS < THREAD_RUNNER_DELAY;
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
                        model.accessNEx(() -> {
                            output.setValue(1 - output.getValue());
                            model.doStep();
                        });
                    } catch (NodeException | RuntimeException e) {
                        stopper.showErrorAndStopModel(Lang.get("msg_clockError"), e);
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
     * runs as fast as possible!
     */
    private class ThreadRunner implements Runner {

        private final Thread thread;

        ThreadRunner() {
            thread = new Thread(() -> {
                LOGGER.debug("thread start");
                FrequencyCalculator frequency = new FrequencyCalculator(status);
                try {
                    while (!Thread.interrupted()) {
                        model.accessNEx(() -> {
                            output.setValue(1 - output.getValue());
                            model.doStep();
                        });
                        frequency.calc();
                    }
                } catch (NodeException | RuntimeException e) {
                    stopper.showErrorAndStopModel(Lang.get("msg_clockError"), e);
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        @Override
        public void stop() {
            thread.interrupt();
        }
    }

    private static final class FrequencyCalculator {
        private static final long MIN_COUNTER = 50000;
        private final StatusInterface status;
        private long checkCounter;
        private int counter;
        private long time;

        private FrequencyCalculator(StatusInterface status) {
            this.status = status;
            time = System.currentTimeMillis();
            counter = 0;
            checkCounter = MIN_COUNTER;
        }

        private void calc() {
            counter++;
            if (counter == checkCounter) {
                long t = System.currentTimeMillis();
                if (t - time > 2000) {
                    final long l = counter / (t - time) / 2;
                    status.setStatus(l + " kHz");
                    time = t;
                    counter = 0;
                    checkCounter = MIN_COUNTER;
                } else {
                    checkCounter += MIN_COUNTER;
                }
            }
        }
    }
}
