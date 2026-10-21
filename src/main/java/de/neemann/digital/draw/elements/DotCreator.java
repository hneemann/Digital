/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Is used to create the dots to visualize the connections between wires.
 * Each {@link Wire} has two flags to decide which end of the wire needs to be marked with a dot.
 */
public class DotCreator {

    private final ArrayList<Dot> dots;
    private final ArrayList<Wire> wires;
    private HashMap<Vector, Dot> map;

    /**
     * Creates a new instance
     *
     * @param wires the wires
     */
    public DotCreator(ArrayList<Wire> wires) {
        this.wires = wires;
        map = new HashMap<>();
        for (Wire w : wires) {
            inc(w.p1, w);
            inc(w.p2, w);
        }

        dots = new ArrayList<>();
        for (Map.Entry<Vector, Dot> e : map.entrySet())
            if (e.getValue().wires.size() > 2)
                dots.add(e.getValue());
        map = null;
    }

    private void inc(Vector vector, Wire w) {
        Dot c = map.get(vector);
        if (c == null) {
            c = new Dot(vector);
            map.put(vector, c);
        }
        c.add(w);
    }

    /**
     * Applies the dots to the wires
     */
    public void applyDots() {
        for (Wire w : wires)
            w.noDot();
        for (Dot d : dots)
            d.setDot();
    }

    /**
     * Draws each connection dot using the styles of the wires entering it.
     *
     * @param graphic the graphic instance used
     * @param highLighted the currently highlighted drawables
     */
    public void drawTo(Graphic graphic, Collection<?> highLighted) {
        for (Dot dot : dots)
            if (!dot.isHighlighted(highLighted))
                dot.drawTo(graphic);
    }

    /**
     * A single dot
     */
    private static class Dot {
        private final Vector vector;
        private final ArrayList<Wire> wires = new ArrayList<>();

        Dot(Vector vector) {
            this.vector = vector;
        }

        private void add(Wire wire) {
            wires.add(wire);
        }

        private boolean isHighlighted(Collection<?> highLighted) {
            for (Wire wire : wires)
                if (highLighted.contains(wire))
                    return true;
            return false;
        }

        private void setDot() {
            for (Wire wire : wires)
                wire.setDot(vector);
        }

        private void drawTo(Graphic graphic) {
            ArrayList<Direction> directions = new ArrayList<>();
            for (Wire wire : wires) {
                Vector other = wire.p1.equals(vector) ? wire.p2 : wire.p1;
                double angle = Math.atan2(other.y - vector.y, other.x - vector.x);
                addDirection(directions, new Direction(angle, wire));
            }
            Collections.sort(directions, Comparator.comparingDouble(d -> d.angle));

            Style firstOuterStyle = directions.get(0).wire.getCurrentDrawStyle();
            drawFilledCircle(graphic, firstOuterStyle, firstOuterStyle.getThickness() + 1);
            for (int i = 0; i < directions.size(); i++) {
                Direction direction = directions.get(i);
                Direction previous = directions.get((i + directions.size() - 1) % directions.size());
                Direction next = directions.get((i + 1) % directions.size());
                double previousGap = normalizeAngle(direction.angle - previous.angle);
                double nextGap = normalizeAngle(next.angle - direction.angle);
                double start = directions.size() == 1 ? direction.angle - Math.PI
                        : direction.angle - previousGap / 2;
                double end = directions.size() == 1 ? direction.angle + Math.PI
                        : direction.angle + nextGap / 2;

                Style outerStyle = direction.wire.getCurrentDrawStyle();
                drawSector(graphic, vector, outerStyle.getThickness() + 1, start, end,
                        fillStyle(outerStyle));
            }

            Style firstCoreStyle = directions.get(0).wire.getCoreDrawStyle();
            if (firstCoreStyle != null)
                drawFilledCircle(graphic, firstCoreStyle, firstCoreStyle.getThickness() + 1);
            for (int i = 0; i < directions.size(); i++) {
                Direction direction = directions.get(i);
                Direction previous = directions.get((i + directions.size() - 1) % directions.size());
                Direction next = directions.get((i + 1) % directions.size());
                double previousGap = normalizeAngle(direction.angle - previous.angle);
                double nextGap = normalizeAngle(next.angle - direction.angle);
                double start = directions.size() == 1 ? direction.angle - Math.PI
                        : direction.angle - previousGap / 2;
                double end = directions.size() == 1 ? direction.angle + Math.PI
                        : direction.angle + nextGap / 2;
                Style coreStyle = direction.wire.getCoreDrawStyle();
                if (coreStyle != null)
                    drawSector(graphic, vector, coreStyle.getThickness() + 1, start, end,
                            fillStyle(coreStyle));
            }
        }

        private static void addDirection(ArrayList<Direction> directions, Direction direction) {
            for (Direction existing : directions) {
                if (Math.abs(normalizeAngle(direction.angle - existing.angle)) < 0.001
                        || Math.abs(normalizeAngle(existing.angle - direction.angle)) < 0.001) {
                    existing.wire = direction.wire;
                    return;
                }
            }
            directions.add(direction);
        }

        private static double normalizeAngle(double angle) {
            while (angle < 0)
                angle += Math.PI * 2;
            while (angle >= Math.PI * 2)
                angle -= Math.PI * 2;
            return angle;
        }

        private static Style fillStyle(Style style) {
            return Style.WIRE.deriveStyle(0, true, style.getColor());
        }

        private void drawFilledCircle(Graphic graphic, Style style, int radius) {
            Vector size = new Vector(radius, radius);
            graphic.drawCircle(vector.sub(size), vector.add(size), fillStyle(style));
        }

        private static void drawSector(Graphic graphic, Vector center, int radius, double start, double end, Style style) {
            if (radius <= 0 || end <= start)
                return;
            Polygon sector = new Polygon(true).add(center);
            int steps = Math.max(2, (int) Math.ceil((end - start) * radius / 2));
            for (int i = 0; i <= steps; i++) {
                double angle = start + (end - start) * i / steps;
                sector.add(center.x + (int) Math.round(Math.cos(angle) * radius),
                        center.y + (int) Math.round(Math.sin(angle) * radius));
            }
            graphic.drawPolygon(sector, style);
        }

        private static final class Direction {
            private final double angle;
            private Wire wire;

            private Direction(double angle, Wire wire) {
                this.angle = normalizeAngle(angle);
                this.wire = wire;
            }
        }
    }

}
