/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.shapes.custom.svg.SvgException;
import de.neemann.digital.draw.shapes.custom.svg.SvgImporter;
import junit.framework.TestCase;

import java.io.IOException;

import static de.neemann.digital.draw.graphics.Vector.vec;
import static de.neemann.digital.draw.shapes.custom.SvgImporterTest.in;

public class CustomShapeDescriptionTest extends TestCase {

    private CustomShapeDescription custom;

    public void setUp() throws IOException, SvgException {
        custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <path d=\"M 0,0 l 0,100 l 100,0 l 0,-100\" />\n" +
                        "  <circle id=\"pin:in\" cx=\"0\" cy=\"00\" r=\"6\" />\n" +
                        "  <circle id=\"pin:out\" cx=\"40\" cy=\"0\" r=\"6\" />\n" +
                        "</svg>")).create();
    }

    public void testCheckCompatibilityOk() throws PinException {
        Circuit circuit = new Circuit()
                .add(new VisualElement(In.DESCRIPTION.getName()).setPos(vec(0, 0)).setAttribute(Keys.LABEL, "in"))
                .add(new VisualElement(Out.DESCRIPTION.getName()).setPos(vec(20, 0)).setAttribute(Keys.LABEL, "out"))
                .add(new Wire(vec(0, 0), vec(20, 0)));

        custom.checkCompatibility(circuit);
    }

    public void testCheckCompatibilityClock() throws PinException {
        Circuit circuit = new Circuit()
                .add(new VisualElement(Clock.DESCRIPTION.getName()).setPos(vec(0, 0)).setAttribute(Keys.LABEL, "in"))
                .add(new VisualElement(Out.DESCRIPTION.getName()).setPos(vec(20, 0)).setAttribute(Keys.LABEL, "out"))
                .add(new Wire(vec(0, 0), vec(20, 0)));

        custom.checkCompatibility(circuit);
    }

    public void testCheckCompatibilityInPinMissing() {
        Circuit circuit = new Circuit()
                .add(new VisualElement(In.DESCRIPTION.getName()).setPos(vec(0, 0)).setAttribute(Keys.LABEL, "in"))
                .add(new VisualElement(In.DESCRIPTION.getName()).setPos(vec(0, 20)).setAttribute(Keys.LABEL, "in2"))
                .add(new VisualElement(Out.DESCRIPTION.getName()).setPos(vec(20, 0)).setAttribute(Keys.LABEL, "out"));

        try {
            custom.checkCompatibility(circuit);
            fail();
        } catch (PinException e) {
            assertTrue(true);
        }
    }

    public void testCheckCompatibilityOutPinMissing() {
        Circuit circuit = new Circuit()
                .add(new VisualElement(In.DESCRIPTION.getName()).setPos(vec(0, 0)).setAttribute(Keys.LABEL, "in"))
                .add(new VisualElement(Out.DESCRIPTION.getName()).setPos(vec(20, 0)).setAttribute(Keys.LABEL, "out"))
                .add(new VisualElement(Out.DESCRIPTION.getName()).setPos(vec(20, 20)).setAttribute(Keys.LABEL, "out2"));

        try {
            custom.checkCompatibility(circuit);
            fail();
        } catch (PinException e) {
            assertTrue(true);
        }
    }

    public void testCheckCompatibilityOutPinToMuch() {
        Circuit circuit = new Circuit()
                .add(new VisualElement(In.DESCRIPTION.getName()).setPos(vec(0, 0)).setAttribute(Keys.LABEL, "in"));

        try {
            custom.checkCompatibility(circuit);
            fail();
        } catch (PinException e) {
            assertTrue(true);
        }
    }
    public void testCheckCompatibilityInPinToMuch() {
        Circuit circuit = new Circuit()
                .add(new VisualElement(Out.DESCRIPTION.getName()).setPos(vec(0, 0)).setAttribute(Keys.LABEL, "out"));

        try {
            custom.checkCompatibility(circuit);
            fail();
        } catch (PinException e) {
            assertTrue(true);
        }
    }
}