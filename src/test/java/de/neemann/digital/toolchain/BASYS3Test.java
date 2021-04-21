/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.toolchain;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Parser;
import de.neemann.digital.hdl.hgs.ParserException;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class BASYS3Test extends TestCase {

    public void testMMCME2_BASEParams() throws IOException, ParserException, HGSEvalException {
        Configuration c = Configuration.load(new File(Resources.getRoot(), "../../main/dig/hdl/BASYS3.config"));
        FileToCreate clock = c.getFileById("MMCME2_BASE", null);

        String content = clock.getContent();
        for (int f = 4688; f < 500000; f += 77) {
            Context context = new Context((File) null).disableLogging()
                    .declareVar("model",
                            new ElementAttributes()
                                    .set(new Key<>("frequency", 10), f * 1000))
                    .declareVar("F_IN", 100.0)
                    .declareVar("hdl", "vhdl");
            Parser p = new Parser(content);
            p.parse().execute(context);

            double f_vco = ((Number) context.getVar("F_VCO")).doubleValue();
            assertTrue(f_vco <= 1200 && f_vco >= 600);
            double f_out = ((Number) context.getVar("F_OUT")).doubleValue();
            final double errPercent = Math.abs(f_out - f / 1000.0) / f_out * 100;

            assertTrue("" + f + "kHz, " + errPercent + "%", errPercent < 0.1);
        }

    }
}
