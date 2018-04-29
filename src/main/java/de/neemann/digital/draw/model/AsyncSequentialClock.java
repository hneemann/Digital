/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserverTyped;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.wiring.AsyncSeq;
import de.neemann.digital.gui.ErrorStopper;
import de.neemann.digital.lang.Lang;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The real time clock used to clock a circuit in async mode.
 */
public class AsyncSequentialClock implements ModelStateObserverTyped {
    private final Model model;
    private final ScheduledThreadPoolExecutor executor;
    private final ErrorStopper stopper;
    private final int frequency;
    private RealTimeRunner runner;

    /**
     * Creates a new real time clock
     *
     * @param model     the model
     * @param asyncSeq  the infos used to cofigure the clock
     * @param executor  the executor used to schedule the update
     * @param stopper   used to stop the model if an error is detected
     */
    public AsyncSequentialClock(Model model, AsyncSeq asyncSeq, ScheduledThreadPoolExecutor executor, ErrorStopper stopper) {
        this.model = model;
        this.executor = executor;
        this.stopper = stopper;
        int f = asyncSeq.getFrequency();
        if (f < 1) f = 1;
        this.frequency = f;
    }

    @Override
    public void handleEvent(ModelEvent event) {
        switch (event) {
            case STARTED:
                int delayMuS = 1000000 / frequency;
                if (delayMuS < 100)
                    delayMuS = 100;
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
     * runs with defined rate
     */
    private class RealTimeRunner {

        private final ScheduledFuture<?> timer;

        RealTimeRunner(int delay) {
            timer = executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        model.accessNEx(() -> model.doMicroStep(false));
                    } catch (NodeException | RuntimeException e) {
                        stopper.showErrorAndStopModel(Lang.get("msg_clockError"), e);
                        timer.cancel(false);
                    }
                }
            }, delay, delay, TimeUnit.MICROSECONDS);
        }

        public void stop() {
            if (timer != null)
                timer.cancel(false);
        }
    }

}
