/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorInterface;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import java.awt.Color;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Instances of this class are used to generate a simple rectangular custom
 * shape from the supplied list of inputs and outputs and attributes from a
 * circuit; positions are derived from the layout of the inputs and outputs in
 * the actual circuit. Pins can appear anywhere in the custom shape. Their
 * positions are a compressed version of the inputs and outputs in the actual
 * circuit. Vertical or horizontal alignment of I/Os is preserved for the pins
 * in the shape but distances between pins are reduced to a uniform minimum.
 *
 * @author Stephen Parry (sgparry@mainscreen.com)
 */
public class SimpleCustomShapeGenerator {

    /**
     * List of Inputs and Outputs supplied to the generator.
     */
    private final ArrayList<VisualElement> inputsAndOutputs;
    /**
     * The original (unmodified) attributes supplied to the generator.
     */
    private final ElementAttributes orginalAttr;
    /**
     * The label supplied to the generator.
     */
    private final String labelText;

    /**
     * Creates the generator based on the supplied parameters
     *
     * @param inputsAndOutputs List of Inputs and Outputs to construct the shape
     * from.
     * @param orginalAttr The attributes of the circuit; the generation will not
     * alter these directly; it determine the color of the new shape from these,
     * copy them and store the new shape there before returning them.
     * @param labelText Text to put in the shapes label.
     */
    public SimpleCustomShapeGenerator(
            ArrayList<VisualElement> inputsAndOutputs,
            ElementAttributes orginalAttr,
            String labelText) {
        this.inputsAndOutputs = inputsAndOutputs;
        this.orginalAttr = orginalAttr;
        this.labelText = labelText;
    }

    /**
     * Internal helper class that implements the map between circuit In/Output
     * coordinates and shape pins.
     */
    private static class CoordinateMap {

        /**
         * The actual mapping between X coords, indexed by circuit X coordinate.
         */
        private final TreeMap<Integer, Integer> xMap;

        /**
         * The actual mapping between Y coords, indexed by circuit Y coordinate.
         */
        private final TreeMap<Integer, Integer> yMap;

        /**
         * Minimum X coord of the inputs and outputs in the circuit.
         */
        private int minInOutX;
        /**
         * Maximum X coord of the inputs and outputs in the circuit.
         */
        private int maxInOutX;
        /**
         * Minimum Y coord of the inputs and outputs in the circuit.
         */
        private int minInOutY;
        /**
         * Maximum Y coord of the inputs and outputs in the circuit.
         */
        private int maxInOutY;

        /**
         * Flags indicating which corners of the circuit are occupied by inputs
         * or outputs and which edges have inputs or outputs other than at the
         * the corners. These are used later to decide whether to expand the
         * shape in either direction to try and avoid having pins at the
         * corners.
         */
        /**
         * Will the North East corner of the shape be occupied by a pin?.
         */
        private boolean neCornerOccupied;
        /**
         * Will the North West corner of the shape be occupied by a pin?.
         */
        private boolean nwCornerOccupied;
        /**
         * Will the South East corner of the shape be occupied by a pin?.
         */
        private boolean seCornerOccupied;
        /**
         * Will the South West corner of the shape be occupied by a pin?.
         */
        private boolean swCornerOccupied;
        /**
         * Will the North edge of the shape be occupied by a pin?.
         */
        private boolean northEdgeOccupied;
        /**
         * Will the East edge of the shape be occupied by a pin?.
         */
        private boolean eastEdgeOccupied;
        /**
         * Will the South edge of the shape be occupied by a pin?.
         */
        private boolean southEdgeOccupied;
        /**
         * Will the West edge of the shape be occupied by a pin?.
         */
        private boolean westEdgeOccupied;

        /**
         * Flags indicating whether the shape should be expanded in X or Y to
         * try and avoid having any pins at corners.
         */
        /**
         * Should the shape be expanded in X to avoid a corner pin?.
         */
        private boolean expandY;
        /**
         * Should the shape be expanded in Y to avoid a corner pin?.
         */
        private boolean expandX;

        /**
         * The maximum coordinates of the pins in the shape; used to generate
         * the actual rectangle.
         */
        /**
         * The maximum X coordinate of pins in the shape
         */
        private int maxPinX;
        /**
         * The maximum Y coordinate of pins in the shape
         */
        private int maxPinY;

        /**
         * Record an input or output's x coordinate in the circuit for later
         * mapping and assess it's horizontal impact on the boundaries.
         *
         * @param x The x coordinate of the input or output.
         * @param y The y coordinate of the input or output.
         */
        private void analyzeX(int x, int y) {
            if (x < minInOutX) {
                // New leftmost edge
                nwCornerOccupied = (y <= minInOutY);
                swCornerOccupied = (y >= maxInOutY);
                westEdgeOccupied = y > minInOutY && y < maxInOutY;
                minInOutX = x;
            } else if (x == minInOutX) {
                // On leftmost edge
                nwCornerOccupied = nwCornerOccupied || (y <= minInOutY);
                swCornerOccupied = swCornerOccupied || (y >= maxInOutY);
                westEdgeOccupied = westEdgeOccupied
                        || (y > minInOutY && y < maxInOutY);
            }
            if (x > maxInOutX) {
                // New rightmost edge
                neCornerOccupied = (y <= minInOutY);
                seCornerOccupied = (y >= maxInOutY);
                eastEdgeOccupied = y > minInOutY && y < maxInOutY;
                maxInOutX = x;
            } else if (x == maxInOutX) {
                // On rightmost edge
                neCornerOccupied = nwCornerOccupied || (y <= minInOutY);
                seCornerOccupied = swCornerOccupied || (y >= maxInOutY);
                eastEdgeOccupied = eastEdgeOccupied
                        || (y > minInOutY && y < maxInOutY);
            }
            // Record the x cordinate for later mapping.
            xMap.put(x, 0);
        }

        /**
         * Record an input or output's y coordinate in the circuit for later
         * mapping and assess it's vertical impact on the boundaries.
         *
         * @param x The x coordinate of the input or output.
         * @param y The y coordinate of the input or output.
         */
        private void analyzeY(int x, int y) {
            if (y < minInOutY) {
                // New topmost edge
                nwCornerOccupied = (x <= minInOutX);
                neCornerOccupied = (x >= maxInOutX);
                northEdgeOccupied = x > minInOutX && x < maxInOutX;
                minInOutY = y;
            } else if (y == minInOutY) {
                // On topmost edge
                nwCornerOccupied = nwCornerOccupied || (x <= minInOutX);
                neCornerOccupied = neCornerOccupied || (x >= maxInOutX);
                northEdgeOccupied = northEdgeOccupied
                        || (x > minInOutX && x < maxInOutX);
            }
            if (y > maxInOutY) {
                // New bottommost edge
                swCornerOccupied = (x <= minInOutX);
                seCornerOccupied = (x >= maxInOutX);
                southEdgeOccupied = x > minInOutX && x < maxInOutX;
                maxInOutY = y;
            } else if (y == maxInOutY) {
                // On bottommost edge
                swCornerOccupied = swCornerOccupied || (x <= minInOutX);
                seCornerOccupied = seCornerOccupied || (x >= maxInOutX);
                southEdgeOccupied = southEdgeOccupied
                        || (x > minInOutX && x < maxInOutX);
            }
            // Record the y cordinate for later mapping.
            yMap.put(y, 0);
        }

        /**
         * Determines whether we need to expand the shape in X or Y in order to
         * avoid having pins at any corners.
         *
         * @precondition: analyzeX and analyzeY must have been called for every
         * input and output.
         */
        private void calculateExpansion() {
            boolean cornerOccupied =
                    (neCornerOccupied || seCornerOccupied
                    || nwCornerOccupied || swCornerOccupied);
            // If yes, try extending, first in Y then in X.
            expandY = cornerOccupied
                    && !(northEdgeOccupied || southEdgeOccupied);
            expandX = !expandY && cornerOccupied
                    && !(westEdgeOccupied || eastEdgeOccupied);
        }

        /**
         * Determines the final coord values in one axis for pins in the shape.
         *
         * @precondition: analyzeX and analyzeY must have been called for every
         * input and output and to determine map and calculateExpansion called
         * to determine expand.
         * @param map source of circuit coordinate values and where the pin
         * coordinate values will be written.
         * @param expand whether or not the circuit should be expanded in this
         * coordinate to avoid corner pins.
         * @return the maximum coordinate determined.
         */
        static private int calculateMapping(
                TreeMap<Integer, Integer> map,
                boolean expand) {

            // If we are extending in X, leave a margin, otherwise start at 0.
            int coord = expand ? SIZE : 0;

            // Step through all the values, mapping to a uniform minimally
            // spaced grid.
            for (Integer k : map.keySet()) {
                map.put(k, coord);
                coord += SIZE;
            }
            return coord - SIZE;
        }

        /**
         * For a given circuit input / output coord, return the corresponding
         * shape pin coord.
         *
         * @param in The circuit I/O coord.
         * @return The shape pin coord.
         */
        public Vector getMappedCoordOf(VectorInterface in) {
            return new Vector(xMap.get(in.getX()), yMap.get(in.getY()));
        }

        /**
         * Gives the coordinate of the top left corner of the shape (rectangle)
         *
         * @return the coordinate of the corner.
         */
        public VectorInterface getRectP1() {
            return new Vector(expandX ? SIZE2 : 0, expandY ? SIZE2 : 0);
        }

        /**
         * Gives the coordinate of the bottom right corner of the shape
         * (rectangle)
         *
         * @return the coordinate of the corner.
         */
        public VectorInterface getRectP2() {
            return new Vector(maxPinX + (expandX ? SIZE2 : 0),
                    maxPinY + (expandY ? SIZE2 : 0));
        }

        /**
         * Constructor for the Map.
         *
         * @param inputsAndOutputs The list of Inputs and Outputs to construct
         * the map from.
         */
        CoordinateMap(ArrayList<VisualElement> inputsAndOutputs) {
            xMap = new TreeMap<>();
            yMap = new TreeMap<>();

            minInOutX = Integer.MAX_VALUE;
            maxInOutX = Integer.MIN_VALUE;
            minInOutY = Integer.MAX_VALUE;
            maxInOutY = Integer.MIN_VALUE;

            neCornerOccupied = false;
            nwCornerOccupied = false;
            seCornerOccupied = false;
            swCornerOccupied = false;
            northEdgeOccupied = false;
            eastEdgeOccupied = false;
            southEdgeOccupied = false;
            westEdgeOccupied = false;

            if (inputsAndOutputs.size() > 0) {
                // Process each input or output
                for (VisualElement inOut : inputsAndOutputs) {
                    // Analyze each in/output's impact on the boundaries
                    // and record for later mapping.
                    int x = inOut.getPos().getX();
                    int y = inOut.getPos().getY();
                    analyzeX(x, y);
                    analyzeY(x, y);
                }
                // Do we need / can we to expand to avoid corner pins?
                calculateExpansion();

                // Calculate the actual mapping in each axis.
                maxPinX = calculateMapping(xMap, expandX);
                maxPinY = calculateMapping(yMap, expandY);
            }
        }
    }

    /**
     * Generate the actual custom shape.
     *
     * @param map The map of pin coordinates used to generate the shape.
     * @return The shape.
     */
    private CustomShapeDescription generate(CoordinateMap map) {
        CustomShapeDescription csd = new CustomShapeDescription();

        // For each input or output, add a pin at the mapped position.
        for (VisualElement ve : inputsAndOutputs) {
            Vector pos = ve.getPos();
            csd.addPin(ve.getElementAttributes().getLabel(),
                    map.getMappedCoordOf(pos), true);
        }

        // Get the coords of two of the rectangle vertices:
        VectorInterface rectP1 = map.getRectP1();
        VectorInterface rectP2 = map.getRectP2();

        int adjX = 0;
        int adjY = 0;

        // Ensure the rectangle is at least SIZE x SIZE
        if (rectP2.getX() - rectP1.getX() < SIZE) {
            adjX = SIZE;
        }
        if (rectP2.getY() - rectP1.getY() < SIZE) {
            adjY = SIZE;
        }
        rectP2 = rectP2.add(new Vector(adjX, adjY));

        // Determine the other two vertices:
        Vector rectP3 = new Vector(rectP1.getX(), rectP2.getY());
        Vector rectP4 = new Vector(rectP2.getX(), rectP1.getY());

        // Create a the list of the vertices:
        ArrayList<VectorInterface> rectPoints = new ArrayList<>();
        rectPoints.add(rectP1);
        rectPoints.add(rectP3);
        rectPoints.add(rectP2);
        rectPoints.add(rectP4);

        // Determine the color, based on the shape attributes:
        Color bg;
        if (orginalAttr.contains(Keys.BACKGROUND_COLOR)) {
            bg = orginalAttr.get(Keys.BACKGROUND_COLOR);
        } else {
            bg = Keys.BACKGROUND_COLOR.getDefault();
        }

        // Add the rectangle - opaque, bordered in black and filled in the
        // chosen color:
        csd.addPolygon(
                new de.neemann.digital.draw.graphics.Polygon(rectPoints, true),
                Style.NORMAL.getThickness(), bg, true);
        csd.addPolygon(
                new de.neemann.digital.draw.graphics.Polygon(rectPoints, true),
                Style.NORMAL.getThickness(), Color.BLACK, false);

        // Return the final shape:
        return csd;
    }

    /**
     * Gets the new attributes object with the custom shape definition added.
     * @return The attributes
     * @throws PinException one or more of the in/outputs had no label.
     */
    public ElementAttributes getNewAttributes() throws PinException {
        // Create a new copy of the attributes.
        ElementAttributes newAttr = new ElementAttributes(orginalAttr);

        // Create a map from circuit input and output x and y cordinates to
        // custom shape pin coordinates.
        CoordinateMap map = new CoordinateMap(inputsAndOutputs);

        // Create the actual shape:
        CustomShapeDescription csd = generate(map);

        // Update the new attributes and return them.
        newAttr.set(Keys.CUSTOM_SHAPE, csd);
        return newAttr;
    }
}
