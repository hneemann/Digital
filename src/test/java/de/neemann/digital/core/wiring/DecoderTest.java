/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

/**
 */
public class DecoderTest extends TestCase {

    public void testDecoder() throws Exception {
        Model model = new Model();
        ObservableValue sel = new ObservableValue("sel", 2);
        Decoder decoder = model.add(new Decoder(
                new ElementAttributes()
                        .set(Keys.SELECTOR_BITS, 2)));
        decoder.setInputs(sel.asList());


        TestExecuter te = new TestExecuter(model).setInputs(sel).setOutputs(decoder.getOutputs());
        te.check(0, 1, 0, 0, 0);
        te.check(1, 0, 1, 0, 0);
        te.check(2, 0, 0, 1, 0);
        te.check(3, 0, 0, 0, 1);
    }

}
