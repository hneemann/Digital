/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.lang.Lang;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * Helper to import a SVG file.
 */
public class SvgImporter {
    private final Document svg;

    /**
     * Create a new importer instance
     *
     * @param in the svg file to import
     * @throws IOException IOException
     */
    public SvgImporter(InputStream in) throws IOException {
        try {
            svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        } catch (Exception e) {
            throw new IOException(Lang.get("err_parsingSVG"), e);
        }
    }

    /**
     * Create a new importer instance
     *
     * @param svgFile the svg file to import
     * @throws IOException IOException
     */
    public SvgImporter(File svgFile) throws IOException {
        if (!svgFile.exists())
            throw new FileNotFoundException(svgFile.getPath());
        try {
            svg = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgFile);
        } catch (Exception e) {
            throw new IOException(Lang.get("err_parsingSVG"), e);
        }
    }

    /**
     * Parses the svg file.
     *
     * @return the custom shape description
     * @throws SvgException SvgException
     */
    public CustomShapeDescription create() throws SvgException {
        NodeList gList = svg.getElementsByTagName("svg").item(0).getChildNodes();
        Context c = new Context();
        try {
            CustomShapeDescription.Builder builder = new CustomShapeDescription.Builder();
            create(builder, gList, c);

            CustomShapeDescription csd = builder.build();
            if (csd.getPinCount() > 0) {
                float xMin = Float.MAX_VALUE;
                float yMin = Float.MAX_VALUE;
                for (CustomShapeDescription.Pin p : csd.getPins()) {
                    if (p.getPos().x < xMin) xMin = p.getPos().x;
                    if (p.getPos().y < yMin) yMin = p.getPos().y;
                }
                csd.transform(new TransformTranslate(-xMin, -yMin));
            }

            return csd;
        } catch (RuntimeException e) {
            throw new SvgException(Lang.get("err_parsingSVG"), e);
        }
    }

    private void create(CustomShapeDescription.Builder csd, NodeList gList, Context c) throws SvgException {
        for (int i = 0; i < gList.getLength(); i++) {
            final Node node = gList.item(i);
            if (node instanceof Element) {
                final Element element = (Element) node;
                if (element.getNodeName().equals("style"))
                    c.addClasses(element.getTextContent());
                else
                    create(csd, element, c);
            }
        }
    }

    private void create(CustomShapeDescription.Builder csd, Element element, Context parent) throws SvgException {
        Context c = new Context(parent, element);
        switch (element.getNodeName()) {
            case "a":
            case "g":
                create(csd, element.getChildNodes(), c);
                break;
            case "line":
                csd.addLine(
                        c.v(c.getLength(element.getAttribute("x1")), c.getLength(element.getAttribute("y1"))).round(),
                        c.v(c.getLength(element.getAttribute("x2")), c.getLength(element.getAttribute("y2"))).round(),
                        c.getThickness(), c.getStroke());
                break;
            case "rect":
                drawRect(csd, element, c);
                break;
            case "path":
                try {
                    final Polygon d = new PolygonParser(element.getAttribute("d")).create();
                    if (d != null) {
                        d.setEvenOdd(c.isFillRuleEvenOdd());
                        drawTransformedPolygon(csd, c, d);
                    }
                } catch (PolygonParser.ParserException e) {
                    throw new SvgException("invalid path", e);
                }
                break;
            case "polygon":
                try {
                    drawTransformedPolygon(csd, c, new PolygonParser(element.getAttribute("points")).parsePolygon());
                } catch (PolygonParser.ParserException e) {
                    throw new SvgException("invalid points", e);
                }
                break;
            case "polyline":
                try {
                    drawTransformedPolygon(csd, c, new PolygonParser(element.getAttribute("points")).parsePolyline());
                } catch (PolygonParser.ParserException e) {
                    throw new SvgException("invalid points", e);
                }
                break;
            case "circle":
            case "ellipse":
                drawCircle(csd, element, c);
                break;
            case "text":
                drawText(csd, c, element);
                break;
        }
    }

    private void drawTransformedPolygon(CustomShapeDescription.Builder csd, Context c, Polygon polygon) {
        if (polygon != null)
            drawPolygon(csd, c, polygon.transform(c.getTransform()));
    }

    private void drawPolygon(CustomShapeDescription.Builder csd, Context c, Polygon polygon) {
        if (c.getFilled() != null && polygon.isClosed())
            csd.addPolygon(polygon, c.getThickness(), c.getFilled(), true);
        if (c.getStroke() != null)
            csd.addPolygon(polygon, c.getThickness(), c.getStroke(), false);
    }

    private void drawRect(CustomShapeDescription.Builder csd, Element element, Context c) {
        VectorInterface size = new VectorFloat(c.getLength(element.getAttribute("width")), c.getLength(element.getAttribute("height")));
        VectorInterface pos = new VectorFloat(c.getLength(element.getAttribute("x")), c.getLength(element.getAttribute("y")));
        String rxStr = element.getAttribute("rx");
        String ryStr = element.getAttribute("ry");

        VectorInterface rad;
        if (rxStr.isEmpty() && ryStr.isEmpty())
            rad = new Vector(0, 0);
        else if (rxStr.isEmpty())
            rad = new VectorFloat(c.getLength(ryStr), c.getLength(ryStr));
        else if (ryStr.isEmpty())
            rad = new VectorFloat(c.getLength(rxStr), c.getLength(rxStr));
        else
            rad = new VectorFloat(c.getLength(rxStr), c.getLength(ryStr));

        final Polygon polygon;

        float x = pos.getXFloat();
        float y = pos.getYFloat();
        float width = size.getXFloat();
        float height = size.getYFloat();
        if (rad.getXFloat() * rad.getYFloat() != 0) {
            float rx = rad.getXFloat();
            float ry = rad.getYFloat();
            float w = size.getXFloat() - 2 * rx;
            float h = size.getYFloat() - 2 * ry;

            double f = 4 * (Math.sqrt(2) - 1) / 3;
            float cx = (float) (f * rx);
            float cy = (float) (f * ry);

            polygon = new Polygon(true)
                    .add(c.v(x + rx + w, y))
                    .add(c.v(x + rx + w + cx, y), c.v(x + width, y + ry - cy), c.v(x + width, y + ry))
                    .add(c.v(x + width, y + ry + h))
                    .add(c.v(x + width, y + ry + h + cy), c.v(x + rx + w + cx, y + height), c.v(x + rx + w, y + height))
                    .add(c.v(x + rx, y + height))
                    .add(c.v(x + rx - cx, y + height), c.v(x, y + ry + h + cy), c.v(x, y + ry + h))
                    .add(c.v(x, y + ry))
                    .add(c.v(x, y + ry - cy), c.v(x + rx - cx, y), c.v(x + rx, y));
        } else
            polygon = new Polygon(true)
                    .add(c.v(x, y))
                    .add(c.v(x + width, y))
                    .add(c.v(x + width, y + height))
                    .add(c.v(x, y + height));

        drawPolygon(csd, c, polygon);
    }

    private void drawCircle(CustomShapeDescription.Builder csd, Element element, Context c) {
        if (element.hasAttribute("id")) {
            VectorInterface pos = c.v(c.getLength(element.getAttribute("cx")), c.getLength(element.getAttribute("cy")));
            String id = element.getAttribute("id");
            if (id.startsWith("pin:")) {
                csd.addPin(id.substring(4).trim(), toGrid(pos), false);
                return;
            } else if (id.startsWith("pin+:")) {
                csd.addPin(id.substring(5).trim(), toGrid(pos), true);
                return;
            }
        }

        VectorFloat r = null;
        if (element.hasAttribute("r")) {
            final String rad = element.getAttribute("r");
            r = new VectorFloat(c.getLength(rad), c.getLength(rad));
        }
        if (element.hasAttribute("rx")) {
            r = new VectorFloat(c.getLength(element.getAttribute("rx")), c.getLength(element.getAttribute("ry")));
        }
        if (r != null) {
            VectorFloat pos = new VectorFloat(c.getLength(element.getAttribute("cx")), c.getLength(element.getAttribute("cy")));
            float rx = r.getXFloat();
            float ry = r.getYFloat();

            final TransformMatrix matrix = c.getTransform().getMatrix();
            if (matrix.noRotation() || (rx == ry && matrix.isUniform())) {
                // simple circle
                rx = matrix.transformDirection(new VectorFloat(rx, 0)).len();
                ry = matrix.transformDirection(new VectorFloat(0, ry)).len();

                VectorInterface center = pos.transform(c.getTransform());
                VectorFloat rad = new VectorFloat(rx, ry);
                Vector p1 = center.sub(rad).round();
                Vector p2 = center.add(rad).round();

                if (c.getStroke() != null)
                    csd.addCircle(p1, p2, c.getThickness(), c.getStroke(), false);
                if (c.getFilled() != null)
                    csd.addCircle(p1, p2, c.getThickness(), c.getFilled(), true);
            } else {
                // bezier curves
                double f = 4 * (Math.sqrt(2) - 1) / 3;
                float cx = (float) (f * rx);
                float cy = (float) (f * ry);
                float x = pos.getXFloat();
                float y = pos.getYFloat();

                Polygon poly = new Polygon(true)
                        .add(c.v(x - rx, y))
                        .add(c.v(x - rx, y + cy), c.v(x - cx, y + ry), c.v(x, y + ry))
                        .add(c.v(x + cx, y + ry), c.v(x + rx, y + cy), c.v(x + rx, y))
                        .add(c.v(x + rx, y - cy), c.v(x + cx, y - ry), c.v(x, y - ry))
                        .add(c.v(x - cx, y - ry), c.v(x - rx, y - cy), c.v(x - rx, y));

                drawPolygon(csd, c, poly);
            }

        }
    }

    private Vector toGrid(VectorInterface pos) {
        return new Vector(Math.round(pos.getXFloat() / SIZE) * SIZE, Math.round(pos.getYFloat() / SIZE) * SIZE);
    }

    private void drawText(CustomShapeDescription.Builder csd, Context c, Element element) throws SvgException {
        VectorFloat p = new VectorFloat(c.getLength(element.getAttribute("x")), c.getLength(element.getAttribute("y")));
        VectorInterface pos0 = p.transform(c.getTransform());
        VectorInterface pos1 = p.add(new VectorFloat(1, 0)).transform(c.getTransform());

        if (element.getAttribute("id").equals("label"))
            csd.setLabel(pos0.round(), pos1.round(), c.getTextOrientation(), Math.round(c.getFontSize()), c.getFilled());
        else
            drawTextElement(csd, c, element, pos0, pos1);
    }

    private void drawTextElement(CustomShapeDescription.Builder csd, Context c, Element element, VectorInterface pos0, VectorInterface pos1) throws SvgException {
        NodeList nodes = element.getElementsByTagName("*");
        if (nodes.getLength() == 0) {
            String text = element.getTextContent();
            csd.addText(pos0.round(), pos1.round(), text, c.getTextOrientation(), Math.round(c.getFontSize()), c.getFilled());
        } else {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    drawTextElement(csd, new Context(c, el), el, pos0, pos1);
                }
            }
        }
    }

}
