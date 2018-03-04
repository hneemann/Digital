/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Style;

/**
 * Interface implemented by the elements which can draw itself to a {@link Graphic} instance.
 */
public interface Drawable {
    /**
     * Draws an element depending on its state.
     * If implementing a shape, it is not allowed to access the model! Override the
     * {@link ObservableValueReader#readObservableValues()} method to access the model!
     *
     * @param graphic   interface to draw to
     * @param highLight null means no highlighting at all. If highlight is not null, highlight is active.
     *                  The given style should be used to highlight the drawing.
     */
    void drawTo(Graphic graphic, Style highLight);
}
