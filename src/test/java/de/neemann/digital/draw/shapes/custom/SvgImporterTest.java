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

    public void testScale() throws IOException, SvgException, PinException {
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

    public void testArc() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 0,0 L 40,0 A 31,80,30,0,0,100,0 L 140,0\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 40,0 Q 37.161327,16.337067 40.99908,25.612656 Q 44.83683,34.888245 54.32269,34.616966 Q 63.80854,34.345688 76.40078,24.600235 Q 88.27738,15.408623 100,0 L 140,0")
                .check();
    }

    public void testArc2() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 0,0 L 40,0 A 31,80,30,1,0,100,0 L 140,0\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 40,0 Q 27.571037,16.337067 18.5496,35.35811 Q 9.528162,54.37915 6.331539,70.987495 Q 3.1349144,87.59584 6.6196365,97.3413 Q 10.104355,107.086754 19.336689,107.358025 Q 28.569027,107.6293 41.075184,98.35372 Q 53.581345,89.078125 66.0103,72.74106 Q 78.43926,56.403996 87.46069,37.38295 Q 96.48214,18.36191 99.67876,1.7535629 Q 99.84891,0.86956406 100,0 L 140,0")
                .check();
    }

    public void testArc3() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 0,0 L 40,0 A 31,80,30,0,1,100,0 L 140,0\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 40,0 Q 52.42896,-16.33707 64.93511,-25.612656 Q 77.44127,-34.88825 86.67361,-34.616966 Q 95.905945,-34.345688 99.39067,-24.600235 Q 102.67735,-15.408623 100,0 L 140,0")
                .check();
    }

    public void testArc4() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 0,0 L 40,0 A 31,80,30,1,1,100,0 L 140,0\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 40,0 Q 42.83867,-16.337069 51.593147,-35.35811 Q 60.347626,-54.379158 72.67216,-70.987495 Q 84.99669,-87.59584 97.58891,-97.3413 Q 110.18115,-107.086754 119.66701,-107.35804 Q 129.15286,-107.62932 132.99062,-98.353714 Q 136.82837,-89.07814 133.98969,-72.74107 Q 131.15103,-56.403996 122.396545,-37.382957 Q 113.64207,-18.361908 101.317535,-1.7535667 Q 100.661545,-0.86956406 100,0 L 140,0")
                .check();
    }

    public void testInvalidArcRadii() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 0,0 L 40,0 A 10,20,0,1,1,100,0 L 140,0\"/>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 40,0 Q 40,-16.076952 44.019238,-30 Q 48.038475,-43.92305 55,-51.961525 Q 61.961525,-60 70,-60 Q 78.038475,-60 85,-51.961525 Q 91.961525,-43.92305 95.98076,-30 Q 100,-16.076952 100,-1.469576E-14 L 140,0")
                .check();
    }

    public void testInkscape1() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"m 40,-40 h 20 c 11.08,0 20,8.92 20,20 V 20 C 80,31.08 71.08,40 60,40 H 40 C 28.92,40 20,31.08 20,20 v -40 c 0,-11.08 8.92,-20 20,-20 z\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 40,-40 L 60,-40 C 71.08,-40 80,-31.08 80,-20 L 80,20 C 80,31.08 71.08,40 60,40 L 40,40 C 28.92,40 20,31.08 20,20 L 20,-20 C 20,-31.08 28.92,-40 40,-40 Z")
                .check();
    }

    public void testInkscape2() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 80,0 A 40,40 0 0 1 58.083689,35.678848 40,40 0 0 1 16.350991,32.26026 L 40,0 Z\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 80,0 Q 80,10.717968 74.641014,20 Q 69.282036,29.282032 60,34.641018 Q 59.05599,35.186043 58.08369,35.67885 Q 48.52357,40.52436 37.82151,39.940636 Q 27.119452,39.35691 18.143057,33.500362 Q 17.230127,32.904728 16.35099,32.26026 L 40,0 Z")
                .check();
    }

    public void testInkscape3() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "  d=\"M 4.71136,11.590742 A 15,47.5 0 0 1 -3.5072565,53.959374 15,47.5 0 0 1 -19.157018,49.899801 l 8.868378,-38.309059 z\"" +
                        "  transform=\"rotate(-41.594188)\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 11.217981,5.540678 Q 19.667194,15.0592 25.481504,24.63657 Q 31.29581,34.213936 32.91727,41.2839 Q 33.082184,42.00294 33.19799,42.68264 Q 34.336685,49.36582 30.875134,51.51164 Q 27.413582,53.657448 20.279312,50.690926 Q 19.55373,50.389217 18.799128,50.03573 L -1.9073486E-6,15.498432 Z")
                .check();
    }


    //*****************************************************************************************************


    private InputStream in(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private static class CSDChecker {
        private final CustomShapeDescription csd;
        private final ArrayList<Checker> checker;
        private final ArrayList<TestPin> pins;

        private CSDChecker(CustomShapeDescription csd) {
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

        private CSDChecker checkPolygon(String s) throws PolygonParser.ParserException {
            checker.add(new CheckPolygon(new PolygonParser(s).create()));
            return this;
        }

        private CSDChecker checkPolygon(String s, boolean evenOdd) throws PolygonParser.ParserException {
            checker.add(new CheckPolygon(new PolygonParser(s).create().setEvenOdd(evenOdd)));
            return this;
        }

        private CSDChecker checkPin(int x, int y, String name, boolean showLabel) {
            pins.add(new TestPin(x, y, name, showLabel));
            return this;
        }

        private CSDChecker checkLine(int x1, int y1, int x2, int y2) {
            checker.add(new Checker() {
                @Override
                public void check(Drawable d) {
                    assertTrue("element is no line", d instanceof CustomShapeDescription.LineHolder);
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

            private TestPin(int x, int y, String name, boolean showLabel) {
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

        private CheckPolygon(Polygon should) {
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