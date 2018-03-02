/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import java.util.ArrayList;

/**
 * A simple observable
 */
public class Observable {
    private final ArrayList<Observer> observers;

    /**
     * Creates a new instance
     */
    public Observable() {
        observers = new ArrayList<>();
    }

    /**
     * Adds an observer to this observable.
     *
     * @param observer the observer to add
     * @return observer the observer to add
     */
    public Observer addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer))
            observers.add(observer);
        return observer;
    }

    /**
     * @return the numbers of observers
     */
    public int observerCount() {
        return observers.size();
    }

    /**
     * Removes an observer from this observable.
     *
     * @param observer the observer to use
     */
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Removes all observers from the given class
     *
     * @param observerClass the class of observers to remove
     */
    public void removeObserver(Class<? extends Observer> observerClass) {
        observers.removeIf(observer -> observer.getClass() == observerClass);
    }

    /**
     * Fires a has changed event to all observers
     */
    public void fireHasChanged() {
        for (Observer o : observers)
            o.hasChanged();
    }


    /**
     * Returns true if the given observer observes this observable
     *
     * @param observer the observer
     * @return true if the given observer observes this observable
     */
    public boolean hasObserver(Observer observer) {
        return observers.contains(observer);
    }

    /**
     * @return tje list of observers
     */
    public ArrayList<Observer> getObservers() {
        return observers;
    }

}
