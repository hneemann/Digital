/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * Simple rectangle
 */
public class RectShape implements ShapeSpecificMatch {
    private final String label;
    private final int width;
    private final int height;
    private final boolean inside;
    private final boolean bottom;
    private final boolean right;
    private final int fontSize;

    /**
     * Create a new instance
     *
     * @param attr    attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public RectShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        final String l = attr.get(Keys.LABEL);
        if (l.isEmpty())
            label = Lang.get("elem_Text");
        else if (l.equals("-"))
            label = "";
        else
            label = Lang.evalMultilingualContent(l);

        width = attr.get(Keys.RECT_WIDTH);
        height = attr.get(Keys.RECT_HEIGHT);
        inside = attr.get(Keys.RECT_INSIDE);
        bottom = attr.get(Keys.RECT_BOTTOM);
        right = attr.get(Keys.RECT_RIGHT);
        fontSize = attr.get(Keys.FONT_SIZE);
    }

    @Override
    public Pins getPins() {
        return new Pins();
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {

        int ofs = -3;
        Orientation orientation = right ? Orientation.RIGHTBOTTOM : Orientation.LEFTBOTTOM;
        if (inside ^ bottom) {
            ofs = -ofs;
            orientation = right ? Orientation.RIGHTTOP : Orientation.LEFTTOP;
        }

        Vector pos = new Vector(2, ofs);
        if (right)
            pos = pos.add(width * SIZE - 4, 0);
        if (bottom)
            pos = pos.add(0, height * SIZE);

        Style style = Style.NORMAL.deriveFontStyle(fontSize, true);
        if (!label.isEmpty())
            graphic.drawText(pos, label, orientation, style);

        graphic.drawPolygon(new Polygon(true)
                .add(0, 0)
                .add(width * SIZE, 0)
                .add(width * SIZE, height * SIZE)
                .add(0, height * SIZE), Style.DASH);
    }

    @Override
    public boolean matches(Vector pos) {
        return (pos.x > -SIZE2 && pos.x < SIZE2 && pos.y > -SIZE2 && pos.y < height * SIZE + SIZE2)
                || (pos.x > width * SIZE - SIZE2 && pos.x < width * SIZE + SIZE2 && pos.y > -SIZE2 && pos.y < height * SIZE + SIZE2)
                || (pos.y > -SIZE2 && pos.y < SIZE2 && pos.x > -SIZE2 && pos.x < width * SIZE + SIZE2)
                || (pos.y > height * SIZE - SIZE2 && pos.y < height * SIZE + SIZE2 && pos.x > -SIZE2 && pos.x < width * SIZE + SIZE2);
    }

}
