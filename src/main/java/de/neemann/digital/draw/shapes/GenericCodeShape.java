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
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Simple text
 */
public class GenericCodeShape implements Shape {
    private static final Style STYLE = Style.NORMAL.deriveFontStyle(Style.NORMAL.getFontSize(), true);

    private final ArrayList<String> text;

    /**
     * Create a new instance
     *
     * @param attr    attributes
     * @param inputs  the inputs
     * @param outputs the outputs
     */
    public GenericCodeShape(ElementAttributes attr, PinDescriptions inputs, PinDescriptions outputs) {
        String gen = attr.get(Keys.GENERIC);
        text = new ArrayList<>();
        if (gen.isEmpty())
            text.add(Lang.get("elem_GenericCode"));
        else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < gen.length(); i++) {
                char c = gen.charAt(i);
                switch (c) {
                    case ' ':
                        sb.append("\u00A0");
                        break;
                    case '_':
                        sb.append("\\_");
                        break;
                    case '^':
                        sb.append("\\^");
                        break;
                    case '\n':
                        text.add(sb.toString());
                        sb.setLength(0);
                    default:
                        sb.append(c);
                }
            }
            if (sb.length() > 0)
                text.add(sb.toString());
        }
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
        Vector pos = new Vector(0, 0);
        final int dy = (STYLE.getFontSize() * 20) / 16;
        for (String s : text) {
            graphic.drawText(pos, s, Orientation.LEFTCENTER, STYLE);
            pos = pos.add(0, dy);
        }
    }
}
