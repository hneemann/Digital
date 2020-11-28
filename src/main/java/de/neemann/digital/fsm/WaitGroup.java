/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

/**
 * WaitGroup to wait for multiple threads to finish
 */
public class WaitGroup {
    private final Runnable finish;
    private int counter;

    /**
     * Creates a new instance.
     */
    public WaitGroup() {
        this(null);
    }

    /**
     * Creates a new instance.
     *
     * @param finish is executed if the last thread has called the done method.
     */
    public WaitGroup(Runnable finish) {
        this.finish = finish;
    }

    /**
     * Adds a running thread
     */
    synchronized void add() {
        counter++;
    }

    /**
     * Adds multiple threads
     *
     * @param n the number of threads to add
     */
    synchronized public void add(int n) {
        counter += n;
    }

    /**
     * Called from the thread when it finishes.
     */
    synchronized public void done() {
        counter--;
        notify();
        if (counter < 0)
            throw new RuntimeException("illegal waitgroup state");
        if (counter == 0 && finish != null)
            finish.run();
    }

    /**
     * Waits for all threads to be finished.
     *
     * @throws InterruptedException InterruptedException
     */
    synchronized public void waitFor() throws InterruptedException {
        while (counter > 0)
            wait();
    }
}
