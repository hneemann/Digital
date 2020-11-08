/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;


import java.util.Arrays;
import java.util.LinkedList;

/**
 * Used to create permutations
 */
public final class Permute {

    private Permute() {
    }

    /**
     * Creates all permutations of the numbers 0-(size-1).
     *
     * @param size     the size
     * @param listener listener to provide the permutations to
     * @throws PermListenerException PermListenerException
     */
    static void permute(int size, PermListener listener) throws PermListenerException {
        permute(size, size, listener);
    }

    /**
     * Creates all permutations of size numbers aut of the given range.
     *
     * @param size     the size
     * @param range    the range
     * @param listener listener to provide the permutations to
     * @throws PermListenerException PermListenerException
     */
    static void permute(int size, int range, PermListener listener) throws PermListenerException {
        int[] perms = new int[range];
        for (int i = 0; i < range; i++)
            perms[i] = i;
        permute(perms, 0, size, listener);
    }

    private static void permute(int[] perms, int fixed, int size, PermListener listener) throws PermListenerException {
        if (fixed == size) {
            listener.perm(perms);
            return;
        }

        permute(perms, fixed + 1, size, listener);
        for (int i = fixed + 1; i < perms.length; i++) {
            swap(perms, fixed, i);
            permute(perms, fixed + 1, size, listener);
            swap(perms, fixed, i);
        }
    }

    private static void swap(int[] perms, int n0, int n1) {
        int t = perms[n0];
        perms[n0] = perms[n1];
        perms[n1] = t;
    }

    /**
     * Interface to provide the permutations
     */
    public interface PermListener {
        /**
         * Called for all permutations
         *
         * @param perm the permutation
         * @throws PermListenerException PermListenerException
         */
        void perm(int[] perm) throws PermListenerException;
    }

    /**
     * Used to use only a part of the permutations
     */
    public static class Divider implements PermListener {

        private final PermListener parent;
        private final int div;
        private int count;

        /**
         * Creates a new devider
         *
         * @param parent the parent PermListener
         * @param div    the divider
         */
        public Divider(PermListener parent, int div) {
            this.parent = parent;
            this.div = div;
        }

        @Override
        public void perm(int[] perm) throws PermListenerException {
            count++;
            if (count >= div) {
                count = 0;
                parent.perm(perm);
            }
        }
    }

    /**
     * Exception cause by the perm listener
     */
    public static class PermListenerException extends Exception {
        /**
         * Creates a new instance
         *
         * @param message message
         */
        public PermListenerException(String message) {
            super(message);
        }

        /**
         * Creates a new instance
         *
         * @param cause cause
         */
        public PermListenerException(Exception cause) {
            super(cause);
        }
    }

    /**
     * Used to pull permutations
     */
    public static final class PermPull {
        private static final int MAXSIZE = 50;
        private final LinkedList<int[]> queue;
        private boolean running = true;

        /**
         * Creates a new instance
         *
         * @param size the size of the permutation
         */
        public PermPull(int size) {
            this(size, size);
        }

        /**
         * Creates a new instance
         *
         * @param size  the size of the permutation
         * @param range the range
         */
        public PermPull(int size, int range) {
            queue = new LinkedList<>();

            Thread thread = new Thread(() -> {
                try {
                    permute(size, range, perm -> {
                        synchronized (queue) {
                            try {

                                while (queue.size() >= MAXSIZE) {
                                    queue.wait();
                                }

                                queue.add(Arrays.copyOf(perm, perm.length));

                                queue.notify();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (!running)
                                throw new PermListenerException("terminated");
                        }

                    });
                } catch (PermListenerException e) {
                    e.printStackTrace();
                }
                synchronized (queue) {
                    running = false;
                    queue.notifyAll();
                }
            });

            thread.start();
        }

        /**
         * Stop creating new permutations
         */
        public void stop() {
            synchronized (queue) {
                running = false;
                queue.notifyAll();
            }
        }

        /**
         * Get the next permutation
         *
         * @return the next permutation
         */
        public int[] next() {
            synchronized (queue) {

                try {
                    while (queue.isEmpty() && running)
                        queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                queue.notifyAll();
                if (!queue.isEmpty())
                    return queue.remove(0);
                else
                    return null;
            }
        }

        /**
         * Wait until all permutations are read
         */
        public void waitFor() {
            synchronized (queue) {
                try {
                    while (running)
                        queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
