/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

/**
 * A simple fuse.
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
