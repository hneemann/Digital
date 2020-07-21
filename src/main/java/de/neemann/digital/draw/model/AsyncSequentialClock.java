/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelEventType;
import de.neemann.digital.core.ModelStateObserverTyped;
import de.neemann.digital.core.wiring.AsyncSeq;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The real time clock used to clock a circuit in async mode.
 */
public class AsyncSequentialClock implements ModelStateObserverTyped {
    private final Model model;
    private final ScheduledThreadPoolExecutor executor;
    private final int frequency;
    private RealTimeRunner runner;

    /**
     * Creates a new real time clock
     *
     * @param model    the model
     * @param asyncSeq the data used to configure the clock
     * @param executor the executor used to schedule the update
     */
    public AsyncSequentialClock(Model model, AsyncSeq asyncSeq, ScheduledThreadPoolExecutor executor) {
        this.model = model;
        this.executor = executor;
        int f = asyncSeq.getFrequency();
        if (f < 1) f = 1;
        this.frequency = f;
    }

    @Override
    public void handleEvent(ModelEvent event) {
        switch (event.getType()) {
            case STARTED:
                int delayMuS = 1000000 / frequency;
                if (delayMuS < 100)
                    delayMuS = 100;
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
     * runs with defined rate
     */
    private class RealTimeRunner {

        private final ScheduledFuture<?> timer;

        RealTimeRunner(int delay) {
            timer = executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    model.doMicroStep(false);
                }
            }, delay, delay, TimeUnit.MICROSECONDS);
        }

        public void stop() {
            if (timer != null)
                timer.cancel(false);
        }
    }

}
