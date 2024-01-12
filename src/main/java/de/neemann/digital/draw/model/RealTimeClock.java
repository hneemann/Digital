/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.*;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.gui.StatusInterface;
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

    private final Model model;
    private final ScheduledThreadPoolExecutor executor;
    private final StatusInterface status;
    private final int frequency;
    private final ObservableValue output;
    private Runner runner;

    /**
     * Creates a new real time clock
     *
     * @param model    the model
     * @param clock    the clock element which is modify
     * @param executor the executor used to schedule the update
     * @param status   allows sending messages to the status line
     */
    public RealTimeClock(Model model, Clock clock, ScheduledThreadPoolExecutor executor, StatusInterface status) {
        this.model = model;
        this.executor = executor;
        this.status = status;
        int f = clock.getFrequency();
        if (f < 1) f = 1;
        this.frequency = f;
        this.output = clock.getClockOutput();
        model.addObserver(this);
    }

    @Override
    public void handleEvent(ModelEvent event) {
        switch (event.getType()) {
            case STARTED:
                int delayMuS = 500000 / frequency;
                if (delayMuS < 1)
                    runner = new ThreadRunner();
                else
                    runner = new RealTimeRunner(delayMuS);
                break;
            case CLOSED:
                if (runner != null)
                    runner.stop();
                break;
        }
    }

    @Override
    public ModelEventType[] getEvents() {
        return new ModelEventType[]{ModelEventType.STARTED, ModelEventType.CLOSED};
    }

    /**
     * @return true if a thread runner is used
     */
    public boolean isThreadRunner() {
        int delayMuS = 500000 / frequency;
        return delayMuS < 1;
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
            FrequencyCalculator frequencyCalculator;
            if (frequency > 2000 && status != null)
                frequencyCalculator = new FrequencyCalculator(status, frequency);
            else
                frequencyCalculator = null;
            timer = executor.scheduleAtFixedRate(() -> {
                model.modify(() -> output.setValue(1 - output.getValue()));
                if (frequencyCalculator != null)
                    frequencyCalculator.calc();
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
                FrequencyCalculator frequencyCalculator = null;
                if (status != null)
                    frequencyCalculator = new FrequencyCalculator(status, frequency);
                while (!Thread.interrupted()) {
                    model.modify(() -> output.setValue(1 - output.getValue()));
                    if (frequencyCalculator != null)
                        frequencyCalculator.calc();
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
        private final StatusInterface status;
        private final long minCounter;
        private long checkCounter;
        private int counter;
        private long time;

        private FrequencyCalculator(StatusInterface status, int frequency) {
            this.status = status;
            time = System.currentTimeMillis();
            counter = 0;
            minCounter = Math.min(frequency, 50000);
            checkCounter = minCounter;
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
                    checkCounter = minCounter;
                } else {
                    checkCounter += minCounter;
                }
            }
        }
    }
}
