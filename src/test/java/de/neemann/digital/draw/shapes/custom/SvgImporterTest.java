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
                .checkPolygon("M 0,-20 L 0,80 L 100,80 L 100,-20")
                .checkPin(20, 0, "test", false)
                .checkPin(0, 20, "test2", true)
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
                        "  <polygon fill=\"none\" points=\"0,0 0,100 100,100 100,0\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 0,100 L 100,100 L 100,0 Z")
                .check();
    }

    public void testPolygonTranslated() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "  <polygon fill=\"none\" transform=\"translate(10,20)\" points=\"0,0 0,100 100,100 100,0\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 10,20 L 10,120 L 110,120 L 110,20 Z")
                .check();
    }

    public void testPolygonTranslated2() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        " <g transform=\"translate(10,20)\">\n" +
                        "  <polygon fill=\"none\" points=\"0,0 0,100 100,100 100,0\" />\n" +
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
                        "  <polygon fill=\"none\" transform=\"translate(10,0)\" points=\"0,0 0,100 100,100 100,0\" />\n" +
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

    public void testCircle2() throws IOException, SvgException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"matrix(2,0,0,2,5,10)\">\n" +
                        "<circle fill=\"none\" stroke=\"black\" \n" +
                        "cx=\"30\" cy=\"30\" r=\"30\" />\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkCircle(65, 70, 60, 60)
                .check();
    }

    public void testCircle3() throws IOException, SvgException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"rotate(10)\">\n" +
                        "<circle fill=\"none\" stroke=\"black\" \n" +
                        "cx=\"30\" cy=\"30\" r=\"30\" />\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkCircle(24, 35, 30, 30)
                .check();
    }

    public void testCircle4() throws IOException, SvgException, PinException, PolygonParser.ParserException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"rotate(10)\">\n" +
                        "<ellipse fill=\"none\" stroke=\"black\" \n" +
                        "cx=\"30\" cy=\"30\" rx=\"30\" ry=\"40\" />\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M -5.2094455,29.544231 C -9.045575,51.300007 1.072031,71.26889 17.388859,74.14598 C 33.70569,77.02308 50.04289,61.7189 53.879017,39.963123 C 57.71515,18.207352 47.59754,-1.7615356 31.280714,-4.6386323 C 14.963885,-7.51573 -1.3733156,7.78846 -5.2094455,29.544231 Z")
                .check();
    }

    public void testCircle5() throws IOException, SvgException, PinException, PolygonParser.ParserException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"scale(1.5,2)\">\n" +
                        "<ellipse fill=\"none\" stroke=\"black\" \n" +
                        "cx=\"30\" cy=\"30\" rx=\"30\" ry=\"40\" />\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkCircle(45, 60, 45, 80)
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
                .checkPolygon("M 0,0 L 40,0 Q 37.201767,16.104305 40.89812,25.364246 Q 44.594475,34.624187 53.821163,34.624184 Q 63.047855,34.624187 75.39799,25.364246 Q 87.748116,16.104305 100,0 L 140,0")
                .check();
    }

    public void testArc2() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 0,0 L 40,0 A 31,80,30,1,0,100,0 L 140,0\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 40,0 Q 27.482368,16.453613 18.429781,35.611202 Q 9.377195,54.76879 6.2476597,71.42859 Q 3.118122,88.08838 6.761387,97.72682 Q 10.404648,107.36524 19.83147,107.36525 Q 29.25829,107.36525 41.90905,97.726814 Q 54.559814,88.08838 66.999504,71.42859 Q 79.4392,54.76879 88.29014,35.611202 Q 97.141075,16.453615 100,0 L 140,0")
                .check();
    }

    public void testArc3() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 0,0 L 40,0 A 31,80,30,0,1,100,0 L 140,0\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 40,0 Q 52.251877,-16.104307 64.602005,-25.364244 Q 76.95214,-34.624187 86.17883,-34.62419 Q 95.405525,-34.624187 99.10187,-25.364246 Q 102.798225,-16.104305 100,0 L 140,0")
                .check();
    }

    public void testArc4() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "      d=\"M 0,0 L 40,0 A 31,80,30,1,1,100,0 L 140,0\"/> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 L 40,0 Q 42.858925,-16.453617 51.70986,-35.611206 Q 60.5608,-54.7688 73.00049,-71.42859 Q 85.440186,-88.08838 98.09094,-97.726814 Q 110.741714,-107.36525 120.16853,-107.36526 Q 129.59535,-107.36525 133.23862,-97.726814 Q 136.88188,-88.08838 133.75233,-71.42859 Q 130.6228,-54.76879 121.57021,-35.61121 Q 112.517624,-16.453617 100,0 L 140,0")
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
                .checkPolygon("M 80,0 Q 80,11.304157 74.083336,20.936241 Q 68.16667,30.568327 58.08369,35.67885 Q 48.00071,40.78937 36.734287,39.866467 Q 25.467867,38.943565 16.35099,32.26026 L 40,0 Z")
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
                .checkPolygon("M 11.217981,5.540678 Q 20.1293,15.579788 26.06316,25.606865 Q 31.99702,35.633938 33.19799,42.68264 Q 34.398964,49.731346 30.511753,51.716427 Q 26.624548,53.701515 18.799128,50.03573 L -1.9073486E-6,15.498432 Z")
                .check();
    }

    public void testInkscape4() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "  d=\"M 10,10 V 40 H 40 V 10 Z m 10, 10 h 10 v 10 H 20 v -10 z\"" +
                        "  /> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 10,10 L 10,40 L 40,40 L 40,10 Z M 20,20 L 30,20 L 30,30 L 20,30 L 20,20 Z")
                .check();
    }


    public void testInkscape5() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 100\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<path fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "  d=\"M 0,0 A 50,50,0,0,0,50,50\"" +
                        "  /> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,0 Q 7.1054274E-15,13.39746 6.69873,25 Q 13.39746,36.60254 25,43.30127 Q 36.60254,50 50,50")
                .check();
    }

    public void testInkscape6() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 200\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<rect fill=\"none\" stroke=\"black\" stroke-width=\"3\" transform=\"rotate(45,100,100)\"\n" +
                        "  x=\"50\" y=\"50\" width=\"100\" height=\"100\"" +
                        "  /> \n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 100,29.289322 L 170.71068,100 L 100,170.71068 L 29.289322,100 Z")
                .check();
    }

    public void testInkscape7() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 200\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<g transform=\"rotate(45, 100, 100)\">\n" +
                        "<g transform=\"translate(10,10)\">\n" +
                        "<rect fill=\"none\" stroke=\"black\" stroke-width=\"3\"\n" +
                        "  x=\"50\" y=\"50\" width=\"100\" height=\"100\"  /> \n" +
                        "</g>\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 100,43.431458 L 170.71068,114.142136 L 100,184.85281 L 29.289322,114.142136 Z")
                .check();
    }

    public void testInkscape8() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg viewBox=\"0 0 200 200\" xmlns=\"http://www.w3.org/2000/svg\">\n" +
                        "<rect fill=\"none\" stroke=\"yellow\" stroke-width=\"5\" transform=\"rotate(45, 100, 100) translate(10,10)\"\n" +
                        "  x=\"50\" y=\"50\" width=\"100\" height=\"100\" />\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 100,43.431458 L 170.71068,114.142136 L 100,184.85281 L 29.289322,114.142136 Z")
                .check();
    }

    public void testInkscape9() throws IOException, SvgException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg\n" +
                        "   viewBox=\"-80 -80 240 200\">\n" +
                        "  <text\n" +
                        "     xml:space=\"preserve\"\n" +
                        "     style=\"font-style:normal;font-weight:normal;font-size:15px;line-height:25px;font-family:Sans;letter-spacing:0px;word-spacing:0px;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1\"\n" +
                        "     x=\"3.55761\"\n" +
                        "     y=\"53.782589\"\n" +
                        "     id=\"text4523\"><tspan\n" +
                        "       sodipodi:role=\"line\"\n" +
                        "       id=\"tspan4521\"\n" +
                        "       x=\"3.55761\"\n" +
                        "       y=\"53.782589\">FA</tspan></text>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkText(4, 54, 15, "FA")
                .check();
    }

    // github #273
    public void testInkscape10() throws IOException, SvgException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<svg\n" +
                        "   viewBox=\"-80 -80 240 200\">\n" +
                        "  <text\n" +
                        "     xml:space=\"preserve\"\n" +
                        "       style=\"-inkscape-font-specification:Arial;text-align:start; font-size : 15px ;word-spacing:0px;text-anchor:start;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.73224092px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1  \"\n" +
                        "     x=\"3.55761\"\n" +
                        "     y=\"53.782589\"\n" +
                        "     id=\"text4523\"><tspan\n" +
                        "       sodipodi:role=\"line\"\n" +
                        "       id=\"tspan4521\"\n" +
                        "       x=\"3.55761\"\n" +
                        "       y=\"53.782589\">FA</tspan></text>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkText(4, 54, 15, "FA")
                .check();
    }

    public void testIllustrator1() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<!-- Generator: Adobe Illustrator 22.1.0, SVG Export Plug-In . SVG Version: 6.00 Build 0)  -->\n" +
                        "<svg version=\"1.1\"\n" +
                        "\t id=\"Ebene_1\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\" xmlns:svg=\"http://www.w3.org/2000/svg\"\n" +
                        "\t xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 220 180\"\n" +
                        "\t style=\"enable-background:new 0 0 220 180;\" xml:space=\"preserve\">\n" +
                        "<style type=\"text/css\">\n" +
                        "\t.st0{fill:#FFFFB4;fill-opacity:0.7843;stroke:#000000;stroke-width:3;}\n" +
                        "\t.st1{font-family:'ArialMT';}\n" +
                        "\t.st2{font-size:24px;}\n" +
                        "\t.st3{fill:#0000B2;}\n" +
                        "\t.st4{fill:#808080;}\n" +
                        "\t.st5{font-size:18px;}\n" +
                        "\t.st6{fill:#B20000;}\n" +
                        "</style>\n" +
                        "<sodipodi:namedview  showgrid=\"true\">\n" +
                        "\t<inkscape:grid  empspacing=\"4\" spacingx=\"5\" spacingy=\"5\" type=\"xygrid\"></inkscape:grid>\n" +
                        "</sodipodi:namedview>\n" +
                        "<path class=\"st0\" d=\"M80,70c0,0,15,0,30,0c40,0,40,60,5,60c-15,0-35,0-35,0V70z\"/>\n" +
                        "<text id=\"label\" transform=\"matrix(1 0 0 1 81.8621 60)\" class=\"st1 st2\">Label</text>\n" +
                        "<g>\n" +
                        "\t<circle id=\"pin:A\" class=\"st3\" cx=\"80\" cy=\"80\" r=\"3\"/>\n" +
                        "\t<text transform=\"matrix(1 0 0 1 84 86)\" class=\"st4 st1 st5\">A</text>\n" +
                        "</g>\n" +
                        "<g>\n" +
                        "\t<circle id=\"pin:B\" class=\"st3\" cx=\"79.5\" cy=\"118.8\" r=\"3\"/>\n" +
                        "\t<text transform=\"matrix(1 0 0 1 83.5 124.8469)\" class=\"st4 st1 st5\">B</text>\n" +
                        "</g>\n" +
                        "<g>\n" +
                        "\t<circle id=\"pin:Y\" class=\"st6\" cx=\"140\" cy=\"99\" r=\"3\"/>\n" +
                        "\t<text transform=\"matrix(1 0 0 1 124.6611 105)\" class=\"st4 st1 st5\">Y</text>\n" +
                        "</g>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 0,-10 C 0,-10 15,-10 30,-10 C 70,-10 70,50 35,50 C 20,50 0,50 0,50 L 0,-10 Z")
                .checkPolygon("M 0,-10 C 0,-10 15,-10 30,-10 C 70,-10 70,50 35,50 C 20,50 0,50 0,50 L 0,-10 Z")
                .checkText(4, 6, 18, "A")
                .checkText(4, 45, 18, "B")
                .checkText(45, 25, 18, "Y")
                .checkPin(0, 0, "A", false)
                .checkPin(0, 40, "B", false)
                .checkPin(60, 20, "Y", false)
                .check();
    }

    public void testScaling() throws IOException, SvgException, PolygonParser.ParserException, PinException {
        CustomShapeDescription custom = new SvgImporter(
                in("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<svg version=\"1.1\">\n" +
                        "<rect stroke=\"black\" stroke-width=\"3\" fill=\"none\" x=\"1cm\" y=\"1cm\" width=\"8cm\" height=\"8cm\"/>\n" +
                        "</svg>")).create();

        new CSDChecker(custom)
                .checkPolygon("M 37.795277,37.795277 L 340.1575,37.795277 L 340.1575,340.1575 L 37.795277,340.1575 Z")
                .check();
    }

    //*****************************************************************************************************


    static InputStream in(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream inDebug(String s) {
        System.out.println(s);
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
                assertEquals("wrong pin x coordinate in " + tp.name, tp.x, p.getPos().x);
                assertEquals("wrong pin y coordinate " + tp.name, tp.y, p.getPos().y);
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

        private CSDChecker checkLine(float x1, float y1, float x2, float y2) {
            checker.add(d -> {
                assertTrue("element is no line", d instanceof CustomShapeDescription.LineHolder);
                CustomShapeDescription.LineHolder l = (CustomShapeDescription.LineHolder) d;
                assertEquals(x1, l.getP1().getXFloat(), 1e-4);
                assertEquals(y1, l.getP1().getYFloat(), 1e-4);
                assertEquals(x2, l.getP2().getXFloat(), 1e-4);
                assertEquals(y2, l.getP2().getYFloat(), 1e-4);
            });
            return this;
        }

        private CSDChecker checkCircle(float cx, float cy, float rx, float ry) {
            checker.add(d -> {
                assertTrue("element is no circle", d instanceof CustomShapeDescription.CircleHolder);
                CustomShapeDescription.CircleHolder c = (CustomShapeDescription.CircleHolder) d;

                VectorInterface isCenter = c.getP1().add(c.getP2()).div(2);
                VectorInterface isRad = c.getP2().sub(c.getP1()).div(2);

                String message = "\ncx=" + isCenter.getXFloat() + "; cy=" + isCenter.getYFloat() + "; rx=" + isRad.getXFloat() + "; ry=" + isRad.getYFloat() + "\n";

                assertEquals(message + "cx", cx, isCenter.getXFloat(), 1e-4);
                assertEquals(message + "cy", cy, isCenter.getYFloat(), 1e-4);
                assertEquals(message + "rx", rx, isRad.getXFloat(), 1e-4);
                assertEquals(message + "ry", ry, isRad.getYFloat(), 1e-4);
            });
            return this;
        }

        private CSDChecker checkText(int x, int y, int fontSize, String text) {
            checker.add(d -> {
                assertTrue("Text expected, found " + d.getClass().getSimpleName(), d instanceof CustomShapeDescription.TextHolder);
                CustomShapeDescription.TextHolder t = (CustomShapeDescription.TextHolder) d;
                assertEquals(text + " x", x, t.getPos().x);
                assertEquals(text + " y", y, t.getPos().y);
                assertEquals(text + " font size", fontSize, t.getFontSize());
                assertEquals(text + " text", text, t.getText());
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

            String message = "\nshould: " + should + "\nwas   : " + polygon + "\n";

            assertEquals(message + "not the correct polygon size", shouldPoints.size(), isPoints.size());
            for (int i = 0; i < shouldPoints.size(); i++) {
                VectorInterface sh = shouldPoints.get(i);
                VectorInterface is = isPoints.get(i);
                assertEquals(message + "x coordinate " + i, sh.getXFloat(), is.getXFloat(), 1e-4);
                assertEquals(message + "y coordinate " + i, sh.getYFloat(), is.getYFloat(), 1e-4);
            }
        }
    }

}