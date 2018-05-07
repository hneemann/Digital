/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.io.File;
import java.io.IOException;

import de.neemann.digital.draw.graphics.svg.ImportSVG;
import de.neemann.digital.draw.graphics.svg.NoParsableSVGException;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import junit.framework.TestCase;

/**
 * @author felix Examples from: http://www.selfsvg.info/?section=3.5
 */
public class ImportSVGTest extends TestCase {
    private CustomShapeDescription svg = new CustomShapeDescription();

    public void testSVGImport() throws IOException {
        try {
            ImportSVG importer = new ImportSVG(new File("src/test/resources/svg/test.svg"));
            assertEquals(4, importer.getFragments().size());
            svg = svg.addPin("I", new Vector(0, 0),true);
            svg = svg.addPin("O", new Vector(100, 0),true);
            importer = new ImportSVG(new File("src/test/resources/svg/test.svg"));
            assertEquals(4, importer.getFragments().size());
            assertEquals(0, importer.getPseudoPins().size());
            importer = new ImportSVG(new File("src/test/resources/svg/test-with-pin.svg"));
            assertEquals(5, importer.getFragments().size());
            assertEquals(1, importer.getPseudoPins().size());
        } catch (NoParsableSVGException e) {
            fail("The SVG is not parsed correctly");
        }
    }
}
