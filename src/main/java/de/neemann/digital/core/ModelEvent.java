package de.neemann.digital.core;

import de.neemann.digital.core.wiring.Clock;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ModelEvent {


    public static final ModelEvent STEP = new ModelEvent(Event.STEP);

    public enum Event {STARTED, STOPPED, FETCHCLOCK, STEP}

    private final Event event;
    private ArrayList<Clock> clocks;

    public ModelEvent(Event event) {
        this.event = event;
    }

    public Event getType() {
        return event;
    }

    public void registerClock(Clock clock) {
        if (clocks == null)
            clocks = new ArrayList<Clock>();
        clocks.add(clock);
    }

    public ArrayList<Clock> getClocks() {
        return clocks;
    }
}
