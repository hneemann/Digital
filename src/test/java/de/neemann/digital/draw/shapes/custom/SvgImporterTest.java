/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom;

import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.PolygonParser;
import de.neemann.digital.draw.graphics.VectorInterface;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.custom.svg.SvgException;
import de.neemann.digital.draw.shapes.custom.svg.SvgImporter;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SvgImporterTest extends TestCase {

    public void testPin() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <path d=\"M 0,0 l 0,100 l 100,0 l 0,-100\" />\n" +
                        "  <circle\n" +
                        "       id=\"pin:test\"\n" +
                        "       cx=\"22\"\n" +
                        "       cy=\"19\"\n" +
                        "       r=\"6.0923076\" />\n" +
                        "  <circle\n" +
                        "       id=\"pin+:test2\"\n" +
                        "       cx=\"0\"\n" +
                        "       cy=\"45\"\n" +
                        "       r=\"6.0923076\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 0,100 L 100,100 L 100,0")
                .checkPin(20, 20, "test", false)
                .checkPin(0, 40, "test2", true)
                .check();
    }

    public void testPath() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <path d=\"M 0,0 l 0,100 l 100,0 l 0,-100\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 0,100 L 100,100 L 100,0")
                .check();
    }

    public void testPathEvenOdd() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <path fill-rule=\"evenOdd\" d=\"M 0,0 l 0,100 l 100,0 l 0,-100\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 0,100 L 100,100 L 100,0", true)
                .check();
    }

    public void testPathEvenOdd2() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <path style=\"fill-rule:evenOdd\" d=\"M 0,0 l 0,100 l 100,0 l 0,-100\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 0,100 L 100,100 L 100,0", true)
                .check();
    }

    public void testPolyline() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <polyline points=\"0,0 0,100 100,100 100,0\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 0,100 L 100,100 L 100,0")
                .check();
    }

    public void testPolygon() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <polygon points=\"0,0 0,100 100,100 100,0\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 0,100 L 100,100 L 100,0 Z")
                .check();
    }

    public void testPolygonTranslated() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <polygon transform=\"translate(10,20)\" points=\"0,0 0,100 100,100 100,0\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 10,20 L 10,120 L 110,120 L 110,20 Z")
                .check();
    }

    public void testPolygonTranslated2() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        " <g transform=\"translate(10,20)\">\n" +
                        "  <polygon points=\"0,0 0,100 100,100 100,0\" />\n" +
                        " </g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 10,20 L 10,120 L 110,120 L 110,20 Z")
                .check();
    }

    public void testPolygonTranslated3() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        " <g transform=\"translate(0,20)\">\n" +
                        "  <polygon transform=\"translate(10,0)\" points=\"0,0 0,100 100,100 100,0\" />\n" +
                        " </g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 10,20 L 10,120 L 110,120 L 110,20 Z")
                .check();
    }

    public void testPolygonRotate() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"rotate(30)\">\n" +
                        "<path fill=\"none\" stroke=\"black\"\n" +
                        "d=\"M 0,0 L 0,100 L 100,100 L 100,0 Z\"/>\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L -50,86.60254 L 36.60254,136.60254 L 86.60254,50 Z")
                .check();
    }

    public void testPolygonRotate2() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"rotate(45, 50, 50)\">\n" +
                        "<path fill=\"none\" stroke=\"black\"\n" +
                        "d=\"M 0,0 L 0,100 L 100,100 L 100,0 Z\"/>\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 50,-20.710678 L -20.710678,50 L 50,120.71068 L 120.71068,50 Z")
                .check();
    }

    public void testPolygonMatrix() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"matrix(0.8,0.2,0.1,0.9,5,10)\">\n" +
                        "<path fill=\"none\" stroke=\"black\"\n" +
                        "d=\"M 0,0 L 0,100 L 100,100 L 100,0 Z\"/>\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 5,10 L 15,100 L 95,120 L 85,30 Z")
                .check();
    }

    public void testRect() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<rect fill=\"none\" stroke=\"black\" \n" +
                        "x=\"10\" y=\"20\" width=\"70\" height=\"80\"/>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 10,20 L 80,20 L 80,100 L 10,100 Z")
                .check();
    }

    public void testRectRound() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<rect fill=\"none\" stroke=\"black\" \n" +
                        "x=\"10\" y=\"20\" rx=\"10\" ry=\"20\" width=\"70\" height=\"80\"/>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 70,20 C 75.52285,20 80,28.954304 80,40 L 80,80 C 80,91.04569 75.52285,100 70,100 L 20,100 C 14.477152,100 10,91.04569 10,80 L 10,40 C 10,28.954304 14.477152,20 20,20 Z")
                .check();
    }

    public void testCircle() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"matrix(0.8,0.2,0.1,0.9,5,10)\">\n" +
                        "<circle fill=\"none\" stroke=\"black\" \n" +
                        "cx=\"50\" cy=\"60\" r=\"30\" />\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 27,68 C 28.656855,82.91169 40.745167,97.686295 54,101 C 67.25484,104.313705 76.65685,94.91169 75,80 C 73.34315,65.08831 61.254833,50.31371 48,47 C 34.745167,43.68629 25.343145,53.08831 27,68 Z")
                .check();
    }

    public void testScale() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"scale(2,3)\">\n" +
                        "<line fill=\"none\" stroke=\"black\" \n" +
                        "x1=\"10\" y1=\"20\" x2=\"80\" y2=\"70\" />\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkLine(20, 60, 160, 210)
                .check();
    }


    //*****************************************************************************************************


    private InputStream in(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    public static class CSDChecker {
        private final CustomShapeDescription csd;
        private final ArrayList<Checker> checker;
        private final ArrayList<TestPin> pins;

        public CSDChecker(CustomShapeDescription csd) {
            this.csd = csd;
            this.checker = new ArrayList<>();
            this.pins = new ArrayList<>();
        }

        public void check() throws PinException {
            checkShape();
            checkPins();
        }

        private void checkPins() throws PinException {
            assertEquals("wrong number of pins", pins.size(), csd.getPinCount());
            for (TestPin tp : pins) {
                CustomShapeDescription.Pin p = csd.getPin(tp.name);
                assertEquals("wrong pin x coordinate", tp.x, p.getPos().x);
                assertEquals("wrong pin y coordinate", tp.y, p.getPos().y);
                assertEquals("wrong pin label", tp.showLabel, p.isShowLabel());
            }
        }

        private void checkShape() {
            int i = 0;
            for (Drawable d : csd) {
                if (i >= checker.size())
                    fail("to much elements found in the csd");
                checker.get(i).check(d);
                i++;
            }
            if (i != checker.size())
                fail("not enough elements found in the csd");
        }

        public CSDChecker checkPolygon(String s) throws PolygonParser.ParserException {
            checker.add(new CheckPolygon(new PolygonParser(s).create()));
            return this;
        }

        public CSDChecker checkPolygon(String s, boolean evenOdd) throws PolygonParser.ParserException {
            checker.add(new CheckPolygon(new PolygonParser(s).create().setEvenOdd(evenOdd)));
            return this;
        }

        public CSDChecker checkPin(int x, int y, String name, boolean showLabel) {
            pins.add(new TestPin(x, y, name, showLabel));
            return this;
        }

        public CSDChecker checkLine(int x1, int y1, int x2, int y2) {
            checker.add(new Checker() {
                @Override
                public void check(Drawable d) {
                    assertTrue(d instanceof CustomShapeDescription.LineHolder);
                    CustomShapeDescription.LineHolder l = (CustomShapeDescription.LineHolder) d;
                    assertEquals(x1, l.getP1().x);
                    assertEquals(y1, l.getP1().y);
                    assertEquals(x2, l.getP2().x);
                    assertEquals(y2, l.getP2().y);
                }
            });
            return this;
        }

        private static class TestPin {
            private final int x;
            private final int y;
            private final String name;
            private final boolean showLabel;

            public TestPin(int x, int y, String name, boolean showLabel) {
                this.x = x;
                this.y = y;
                this.name = name;
                this.showLabel = showLabel;
            }
        }
    }

    private interface Checker {
        void check(Drawable d);
    }

    private static class CheckPolygon implements Checker {

        private final Polygon should;

        public CheckPolygon(Polygon should) {
            this.should = should;
        }

        @Override
        public void check(Drawable d) {
            assertTrue("no polygon found", d instanceof CustomShapeDescription.PolygonHolder);
            final Polygon polygon = ((CustomShapeDescription.PolygonHolder) d).getPolygon();
            assertEquals("wrong evanOdd mode", should.getEvenOdd(), polygon.getEvenOdd());

            ArrayList<VectorInterface> shouldPoints = new ArrayList<>();
            should.traverse(shouldPoints::add);
            ArrayList<VectorInterface> isPoints = new ArrayList<>();
            polygon.traverse(isPoints::add);

            //System.out.println(polygon);

            assertEquals("not the correct polygon size", shouldPoints.size(), isPoints.size());
            for (int i = 0; i < shouldPoints.size(); i++) {
                VectorInterface sh = shouldPoints.get(i);
                VectorInterface is = isPoints.get(i);
                assertEquals("x coordinate " + i, sh.getXFloat(), is.getXFloat(), 1e-4);
                assertEquals("y coordinate " + i, sh.getYFloat(), is.getYFloat(), 1e-4);
            }
        }
    }

}