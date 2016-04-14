package de.neemann.digital.core.element;

/**
 * A simple pin
 *
 * @author hneemann
 */
public class PinInfo implements PinDescription {


    /**
     * Helper to create an input
     *
     * @param name the pins name
     * @return the new input
     */
    public static PinInfo input(String name) {
        return new PinInfo(name, null, Direction.input);
    }

    /**
     * Helper to create an input
     *
     * @param name        the pins name
     * @param description the pins description
     * @return the new input
     */
    public static PinInfo input(String name, String description) {
        return new PinInfo(name, description, Direction.input);
    }

    /**
     * Helper to create an input
     *
     * @param name the pins name
     * @return the new output
     */
    public static PinInfo output(String name) {
        return new PinInfo(name, null, Direction.output);
    }

    /**
     * Helper to create an input
     *
     * @param name        the pins name
     * @param description the pins description
     * @return the new output
     */
    public static PinInfo output(String name, String description) {
        return new PinInfo(name, description, Direction.output);
    }

    private final String description;
    private final String name;
    private Direction direction;

    /**
     * Creates a copy of the given {@link PinDescription}
     *
     * @param description the description to copy
     */
    public PinInfo(PinDescription description) {
        this(description.getName(), description.getDescription(), description.getDirection());
    }

    /**
     * Creates a new pin
     *
     * @param name        the pins name
     * @param description the pins description
     * @param direction   the pins direction
     */
    public PinInfo(String name, String description, Direction direction) {
        this.description = description;
        this.name = name;
        this.direction = direction;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        if (description == null)
            return name;
        else
            return description;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return name;
    }
}
