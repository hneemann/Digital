package de.neemann.digital.draw.shapes.custom;

import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.PolygonParser;
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
                        "       cx=\"10\"\n" +
                        "       cy=\"20\"\n" +
                        "       r=\"6.0923076\" />\n" +
                        "  <circle\n" +
                        "       id=\"pin+:test2\"\n" +
                        "       cx=\"10\"\n" +
                        "       cy=\"30\"\n" +
                        "       r=\"6.0923076\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 0,100 L 100,100 L 100,0")
                .checkPin(10, 20, "test", false)
                .checkPin(10, 30, "test2", true)
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
            assertEquals("wrong polygon shape", should.toString(), polygon.toString());
            assertEquals("wrong evanOdd mode", should.getEvenOdd(), polygon.getEvenOdd());
        }
    }

}