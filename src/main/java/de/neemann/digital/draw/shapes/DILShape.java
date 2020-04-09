/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * Shape of a DIL package
 */
public class DILShape implements Shape {
    static final float CIRC = (float) (4 * (Math.sqrt(2) - 1) / 3);
    private static final int SPACING = 2;
    private static final int RAD = SPACING * SIZE / 4;
    private static final int BEZ = Math.round(RAD * CIRC);

    private final int pinCount;
    private final Pins pins;
    private final int width;
    private final String shortName;
    private final String label;
    private final ShapePinMap map;

    /**
     * Creates a new dil shape
     *
     * @param shortName shortname
     * @param inputs    inputs
     * @param outputs   outputs
     * @param label     label
     * @param attr      attributes of the embedded circuit
     */
    public DILShape(String shortName, PinDescriptions inputs, PinDescriptions outputs, String label, ElementAttributes attr) {
        this.shortName = shortName;
        this.label = label;
        this.width = attr.get(Keys.WIDTH)+1;
        map = new ShapePinMap(this.width, attr.get(Keys.PINCOUNT));
        map.addAll(inputs);
        map.addAll(outputs);
        this.pinCount = map.getPinCount();
        pins = map.createPins();

    }


    @Override
    public Pins getPins() {
        return pins;
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int dp = 2 * SIZE;
        int pin = dp / 4;
        int x = width * SIZE;
        int h = (pinCount / 2) * dp - SIZE;

        for (int i = 0; i < pinCount / 2; i++) {
            int y = i * dp;
            graphic.drawPolygon(
                    new Polygon(false)
                            .add(pin, y - pin)
                            .add(2, y - pin)
                            .add(2, y + pin)
                            .add(pin, y + pin), Style.NORMAL);
            graphic.drawText(new Vector(pin + SIZE2 / 2, y), map.getText(i + 1), Orientation.LEFTCENTER, Style.SHAPE_PIN);
            graphic.drawPolygon(
                    new Polygon(false)
                            .add(x - pin, y - pin)
                            .add(x - 2, y - pin)
                            .add(x - 2, y + pin)
                            .add(x - pin, y + pin), Style.NORMAL);
            graphic.drawText(new Vector(x - pin - SIZE2 / 2, y), map.getText(pinCount - i), Orientation.RIGHTCENTER, Style.SHAPE_PIN);
        }

        graphic.drawPolygon(
                new Polygon(true)
                        .add(pin, -SIZE)
                        .add(x / 2 - RAD, -SIZE)
                        .add(new Vector(x / 2 - RAD, -SIZE + BEZ), new Vector(x / 2 - BEZ, -SIZE + RAD), new Vector(x / 2, -SIZE + RAD))
                        .add(new Vector(x / 2 + BEZ, -SIZE + RAD), new Vector(x / 2 + RAD, -SIZE + BEZ), new Vector(x / 2 + RAD, -SIZE))
                        .add(x - pin, -SIZE)
                        .add(x - pin, h)
                        .add(pin, h), Style.NORMAL);
        graphic.drawText(new Vector(x / 2, SIZE2), new Vector(x / 2, SIZE * 2), shortName, Orientation.LEFTCENTER, Style.NORMAL_TEXT);

        if (label != null && label.length() > 0)
            graphic.drawText(new Vector(x / 2, h + SIZE2), label, Orientation.CENTERTOP, Style.NORMAL_TEXT);
    }

    private static final class ShapePinMap {
        private final int width;
        private final HashMap<Integer, PinDescription> map;
        private ArrayList<PinDescription> notAssigned;
        private int pinCount;

        private ShapePinMap(int width, int minPinCount) {
            this.width = width;
            this.pinCount = minPinCount;
            map = new HashMap<>();
            notAssigned = new ArrayList<>();
        }

        private void addAll(PinDescriptions pinDescriptions) {
            for (PinDescription p : pinDescriptions)
                add(p);
        }

        private void add(PinDescription p) {
            int num = 0;
            try {
                String str = p.getPinNumber();
                if (str != null)
                    num = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                // keep zero
            }

            if (num == 0 || map.containsKey(num))
                notAssigned.add(p);
            else {
                map.put(num, p);
                if (num > pinCount)
                    pinCount = num;
            }
        }

        private int getPinCount() {
            if (notAssigned != null)
                assignNotAssigned();
            return pinCount;
        }

        private void assignNotAssigned() {
            int num = 1;
            for (PinDescription p : notAssigned) {
                while (map.containsKey(num)) num++;
                map.put(num, p);
            }
            if (num > pinCount)
                pinCount = num;
            notAssigned = null;

            if ((pinCount & 1) == 1)
                pinCount++;
        }

        private Vector getPinPos(int pinNumber) {
            if (pinNumber <= pinCount / 2)
                return new Vector(0, (pinNumber - 1) * SIZE * SPACING);
            else
                return new Vector(SIZE * width, (pinCount - pinNumber) * SIZE * SPACING);
        }

        private Pins createPins() {
            if (notAssigned != null)
                assignNotAssigned();
            Pins pins = new Pins();
            for (Map.Entry<Integer, PinDescription> e : map.entrySet())
                pins.add(new Pin(getPinPos(e.getKey()), e.getValue()));
            return pins;
        }

        private String getText(int i) {
            PinDescription p = map.get(i);
            if (p != null)
                return p.getName();
            else
                return "";
        }
    }

}
