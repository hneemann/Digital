/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * Simple sync interface.
 * Is used to access a running model. Every access to a running model needs to be synchronized.
 * Also a access to a {@link ObservableValues} which is part of the model needs to be synchronized.
 * The {@link Model} implements this interface, so you can use the model itself to synchronize the
 * access to the model.
 * Dialogs which can appear either if a model is running or even if no model exists can use this interface
 * to synchronize a possible model access. It there is no model, pass the the {@link SyncAccess#NOSYNC} instance
 * to the dialog, if there is a model simply pass the model to the dialog.
 * The most obvious example is the {@link de.neemann.digital.gui.components.CircuitComponent}. If a model is
 * running it uses the model to synchronize modifications. If the the simulation is stopped and there is no model
 * it uses the {@link SyncAccess#NOSYNC} instance instead.
 */
public interface SyncAccess {

    /**
     * Does no synchronization at all.
     * Used if there are modifications which use sometimes a running model and sometimes do not.
     * If there is no running model you can use this instance. But use this instance only if you are
     * sure that there is no running model!
     */
    SyncAccess NOSYNC = new SyncAccess() {
        @Override
        public <A extends Runnable> A modify(A run) {
            run.run();
            return run;
        }

        @Override
        public <A extends Runnable> A read(A run) {
            run.run();
            return run;
        }
    };

    /**
     * Calls the given runnable.
     * The runnable is allowed to modify the model.
     *
     * @param run the runnable to execute
     * @param <A> the type oth the runnable
     * @return the given runnable. Used for chained calls
     */
    <A extends Runnable> A modify(A run);

    /**
     * Calls the given runnable
     * The runnable is NOT allowed to modify the model.
     *
     * @param run the runnable to execute
     * @param <A> the type oth the runnable
     * @return the given runnable. Used for chained calls
     */
    <A extends Runnable> A read(A run);

}
