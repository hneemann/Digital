/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.linemerger;

import de.neemann.digital.draw.graphics.*;

/**
 * Filters out all the lines.
 * All other drawing elements are delegated to the given class.
 */
public class GraphicSkipLines extends Graphic {

    private final Graphic delegate;

    /**
     * Creates a new instance
     *
     * @param delegate the delegate
     */
    public GraphicSkipLines(Graphic delegate) {
        this.delegate = delegate;
    }

    @Override
    public void drawLine(VectorInterface p1, VectorInterface p2, Style style) {
        // lines are skipped
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        delegate.drawPolygon(p, style);
    }

    @Override
    public void drawCircle(VectorInterface p1, VectorInterface p2, Style style) {
        delegate.drawCircle(p1, p2, style);
    }

    @Override
    public void drawText(VectorInterface p1, VectorInterface p2, VectorInterface p3, String text, Orientation orientation, Style style) {
        delegate.drawText(p1, p2, p3, text, orientation, style);
    }

    @Override
    public void openGroup() {
        delegate.openGroup();
    }

    @Override
    public void closeGroup() {
        delegate.closeGroup();
    }

    @Override
    public boolean isFlagSet(Flag flag) {
        return delegate.isFlagSet(flag);
    }
}
