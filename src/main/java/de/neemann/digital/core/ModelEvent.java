package de.neemann.digital.core;

/**
 * @author hneemann
 */
public class ModelEvent {

    public static final ModelEvent STEP = new ModelEvent(Event.STEP);
    public static final ModelEvent MICROSTEP = new ModelEvent(Event.MICROSTEP);
    public static final ModelEvent STARTED = new ModelEvent(Event.STARTED);
    public static final ModelEvent BREAK = new ModelEvent(Event.BREAK);
    public static final ModelEvent STOPPED = new ModelEvent(Event.STOPPED);
    public static final ModelEvent MANUALCHANGE = new ModelEvent(Event.MANUALCHANGE);

    public enum Event {STARTED, STOPPED, STEP, BREAK, MANUALCHANGE, MICROSTEP}

    private final Event event;

    private ModelEvent(Event event) {
        this.event = event;
    }

    public Event getType() {
        return event;
    }

}
