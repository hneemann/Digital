/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Parser;
import de.neemann.digital.hdl.hgs.ParserException;
import junit.framework.TestCase;

import java.io.IOException;

public class ElementAttributesTest extends TestCase {

    /**
     * Ensures that the ElementAttributes is accessible from within the template engine
     */
    public void testElementAttibutes() throws IOException, ParserException, HGSEvalException {
        ElementAttributes attr = new ElementAttributes().set(Keys.BITS, 5);
        final Context c = new Context().declareVar("elem", attr);
        new Parser("bits=<?=elem.Bits?>;").parse().execute(c);
        assertEquals("bits=5;", c.toString());
    }

    /**
     * Ensures that the DataField is accessible from within the template engine
     */
    public void testDataField() throws IOException, ParserException, HGSEvalException {
        DataField d = new DataField(5);
        d.setData(0, 1);
        d.setData(1, 7);
        d.setData(2, 4);
        d.setData(3, 8);
        d.setData(4, 2);
        Context c = new Context().declareVar("d", d);
        new Parser("(<? for(i:=0;i<sizeOf(d);i++) { if (i>0) print(\"-\"); print(d[i]);} ?>)").parse().execute(c);
        assertEquals("(1-7-4-8-2)", c.toString());
    }

}