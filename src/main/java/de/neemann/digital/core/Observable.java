package de.neemann.digital.core;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simple observable
 *
 * @author hneemann
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
     */
    public void addObserver(Observer observer) {
        if (observer != null)
            observers.add(observer);
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
        Iterator<Observer> it = observers.iterator();
        while (it.hasNext()) {
            if (it.next().getClass() == observerClass)
                it.remove();
        }
    }

    /**
     * Fires a has changed event to all observers
     */
    public void hasChanged() {
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
}
