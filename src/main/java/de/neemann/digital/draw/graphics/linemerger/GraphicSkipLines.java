package de.neemann.digital.draw.graphics.linemerger;

import de.neemann.digital.draw.graphics.*;

/**
 * Filters out all the lines.
 * All other drawing elements are delegated to the given class.
 *
 * @author hneemann
 */
public class GraphicSkipLines implements Graphic {

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
    public void drawLine(Vector p1, Vector p2, Style style) {
        // lines are skipped
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        delegate.drawPolygon(p, style);
    }

    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        delegate.drawCircle(p1, p2, style);
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        delegate.drawText(p1, p2, text, orientation, style);
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
    public boolean isFlagSet(String name) {
        return delegate.isFlagSet(name);
    }
}
