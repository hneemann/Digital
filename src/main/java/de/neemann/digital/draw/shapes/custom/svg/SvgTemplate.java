/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * Used to create a SVG template
 */
public class SvgTemplate implements Closeable {
    private final Writer w;
    private final PinDescription[] inputs;
    private final ObservableValues outputs;
    private final int width;
    private final int height;

    /**
     * Creates a new instance
     *
     * @param file    the file to create
     * @param circuit the circuit
     * @throws Exception Exception
     */
    public SvgTemplate(File file, Circuit circuit) throws Exception {
        this(new FileOutputStream(file), circuit);
    }

    /**
     * Creates a new instance
     *
     * @param outputStream the stream to write to
     * @param circuit      the circuit
     * @throws Exception Exception
     */
    public SvgTemplate(OutputStream outputStream, Circuit circuit) throws Exception {
        width = circuit.getAttributes().get(Keys.WIDTH) * SIZE;
        inputs = circuit.getInputNames();
        outputs = circuit.getOutputNames();
        height = Math.max(inputs.length, outputs.size()) * SIZE;
        int border = SIZE * 4;


        w = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<svg\n"
                + "   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"
                + "   xmlns:cc=\"http://creativecommons.org/ns#\"\n"
                + "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
                + "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n"
                + "   xmlns=\"http://www.w3.org/2000/svg\"\n"
                + "   xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\"\n"
                + "   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"\n"
                + "   viewBox=\"-" + border + " -" + border + " " + (width + border * 2) + " " + (height - SIZE + border * 2) + "\"\n"
                + "   version=\"1.1\">\n"
                + "  <sodipodi:namedview showgrid=\"true\">\n"
                + "    <inkscape:grid\n"
                + "       type=\"xygrid\"\n"
                + "       empspacing=\"4\"\n"
                + "       spacingx=\"5\"\n"
                + "       spacingy=\"5\" />\n"
                + "  </sodipodi:namedview>\n");
    }

    /**
     * Creates the template
     *
     * @throws Exception Exception
     */
    public void create() throws Exception {
        w.write("  <rect stroke=\"black\" stroke-width=\"4\""
                + " fill=\"" + getColor(Keys.BACKGROUND_COLOR.getDefault()) + "\""
                + " fill-opacity=\"" + (Keys.BACKGROUND_COLOR.getDefault().getAlpha() / 255f) + "\""
                + " x=\"0\" y=\"-10\" width=\"" + width + "\" height=\"" + height + "\"/>\n");

        Style style = Style.NORMAL;
        w.write("    <text id=\"label\" fill=\"" + getColor(style) + "\" font-size=\""
                + style.getFontSize() + "\" text-anchor=\"middle\" x=\"" + width / 2 + "\" y=\"" + (-SIZE) + "\">Label</text>\n");

        style = Style.SHAPE_PIN;
        final int yOffs = style.getFontSize() / 3;

        int y = 0;
        for (PinDescription i : inputs) {
            w.write("  <g>\n");
            w.write("    <circle fill=\"" + getColor(Style.WIRE) + "\" id=\"pin:" + i.getName() + "\" cx=\"0\" cy=\"" + y + "\" r=\"3\"/>\n");
            Vector labelPos = new Vector(4, y + yOffs);
            w.write("    <text fill=\"" + getColor(style) + "\" font-size=\"" + style.getFontSize() + "\" x=\"" + labelPos.getX() + "\" y=\"" + labelPos.getY() + "\">" + i.getName() + "</text>\n");
            w.write("  </g>\n");
            y += 20;
        }
        y = 0;
        for (PinDescription o : outputs) {
            w.write("  <g>\n");
            w.write("    <circle fill=\"" + getColor(Style.WIRE_OUT) + "\" id=\"pin:" + o.getName() + "\" cx=\"" + width + "\" cy=\"" + y + "\" r=\"3\"/>\n");
            Vector labelPos = new Vector(width - 4, y + yOffs);
            w.write("    <text fill=\"" + getColor(style) + "\" font-size=\"" + style.getFontSize() + "\" text-anchor=\"end\" x=\"" + labelPos.getX() + "\" y=\"" + labelPos.getY() + "\">" + o.getName() + "</text>\n");
            w.write("  </g>\n");
            y += 20;
        }
    }

    private String getColor(Style style) {
        return getColor(style.getColor());
    }

    private String getColor(Color color) {
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    @Override
    public void close() throws IOException {
        w.write("</svg>\n");
        w.close();
    }
}
