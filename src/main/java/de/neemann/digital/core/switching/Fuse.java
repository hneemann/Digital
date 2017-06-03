package de.neemann.digital.core.switching;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

/**
 * A simple fuse.
 * Created by hneemann on 03.06.17.
 */
public class Fuse extends Switch {

    /**
     * The fuse description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Fuse.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BLOWN);

    /**
     * Create a new fuse
     *
     * @param attr the attributes
     */
    public Fuse(ElementAttributes attr) {
        super(attr, !attr.get(Keys.BLOWN), "out1", "out2");
    }
}
