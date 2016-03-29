package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class ModelEvent {

    public static final ModelEvent STEP = new ModelEvent(Event.STEP);
    public static final ModelEvent STARTED = new ModelEvent(Event.STARTED);
    public static final ModelEvent STOPPED = new ModelEvent(Event.STOPPED);

    public enum Event {STARTED, STOPPED, STEP}

    private final Event event;

    private ModelEvent(Event event) {
        this.event = event;
    }

    public Event getType() {
        return event;
    }

}
