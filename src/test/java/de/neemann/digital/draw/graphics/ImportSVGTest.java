package de.neemann.digital.draw.graphics;

import java.util.ArrayList;

import de.neemann.digital.draw.graphics.svg.ImportSVG;
import de.neemann.digital.draw.graphics.svg.NoParsableSVGException;
import de.neemann.digital.draw.graphics.svg.SVG;
import de.neemann.digital.draw.graphics.svg.SVGDrawable;
import de.neemann.digital.draw.graphics.svg.SVGFragment;
import junit.framework.TestCase;

/**
 * @author felix Examples from: http://www.selfsvg.info/?section=3.5
 */
public class ImportSVGTest extends TestCase {
    private String rect = "<rect x =\"10\" y =\"10\" width =\"100\" height =\"50\" rx =\"15\" ry =\"15\" />";
    private String circle = "<circle cx =\"40\" cy =\"40\" r =\"20\" />";
    private String ellipse = "<ellipse cx=\"50\" cy=\"30\" rx=\"40\" ry=\"20\" />";
    private String line = "<line x1=\"5\" y1=\"5\" x2=\"200\" y2=\"100\" style=\"stroke:black;stroke-width:3;\" />";
    private String polyline = "<polyline points=\"10,10 20,40 140,50 55,62 12,70 120,80\" style=\"stroke:black;stroke-width:3;fill:none\" />";
    private String polygon = "<polygon points=\"10,10 20,40 140,50 55,62 12,70 120,80\" style=\"stroke:black;stroke-width:3;fill:none\" />";
    private String path = "<path d=\"M60 10 L110 80 L10 80 Z\" style=\"stroke:black;stroke-width:3;fill:none;\" />";
    private String text = "<text x=\"20\" y=\"20\">Test</text>";
    private SVG svg = new SVG("<svg>" + rect + circle + "<g>" + ellipse + line + "</g><a>" + polyline + polygon + path
            + text + "</a></svg>");

    public void testSVGImport() {
        try {
            ImportSVG importer = new ImportSVG(svg, null, null);
            assertEquals(4, importer.getFragments().size());
            svg = svg.addPin(true, "I", new Vector(0, 0));
            svg = svg.addPin(false, "O", new Vector(100, 0));
            importer = new ImportSVG(svg, null, null);
            assertEquals(6, importer.getFragments().size());
            assertEquals(2, importer.getPseudoPins().size());
            svg = svg.deletePin(new Vector(100, 0));
            importer = new ImportSVG(svg, null, null);
            assertEquals(5, importer.getFragments().size());
            assertEquals(1, importer.getPseudoPins().size());
            ArrayList<SVGDrawable> drawables = new ArrayList<SVGDrawable>();
            for (SVGFragment f : importer.getFragments()) {
                if (f != null)
                    for (SVGDrawable d : f.getDrawables())
                        drawables.add(d);
            }
            /**
             * 8 Drawables for the 8 SVG Elements, plus 1 PseudoPin
             */
            assertEquals(9, drawables.size());
            svg = svg.transformPin(new Vector(0, 0), new Vector(100, 0), "I", true);
            importer = new ImportSVG(svg, null, null);
            assertEquals(5, importer.getFragments().size());
            assertEquals(1, importer.getPseudoPins().size());
            svg = svg.deletePin(new Vector(100, 0));
            importer = new ImportSVG(svg, null, null);
            assertEquals(4, importer.getFragments().size());
            assertEquals(0, importer.getPseudoPins().size());
        } catch (NoParsableSVGException e) {
            fail("The SVG is not parsed correctly");
        }
    }
}
