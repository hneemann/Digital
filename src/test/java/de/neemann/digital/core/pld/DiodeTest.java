/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.pld;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.data.Value;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import junit.framework.TestCase;

import java.io.IOException;

/**
 */
public class DiodeTest extends TestCase {

    /**
     * Two anti parallel unidirectional diodes are able to hold each other either in low or in
     * high state, depending on which diode is processed first.
     * The current simulation model which is build up on inputs which are modifying the outputs
     * is not suited to handle bidirectional passive diodes.
     * The solution implemented for switches - form a common net if switch is closed - is also
     * not able to handle diodes.
     * <p>
     * To make this possible the simulation core needs a significant improvement.
     */

    private static final Value[] values = new Value[]{new Value(0), new Value(1), Value.getHighZ()};

    public void testAntiParallelDiodes() throws IOException, ElementNotFoundException, PinException, NodeException {
        /*
        final ElementLibrary library = new ElementLibrary(true);
        Circuit c = Circuit.loadCircuit(new File(Resources.getRoot(),"dig/DiodeTest.dig"), new ShapeFactory(library));
        Model m = new ModelCreator(c, library).createModel(false);

        ObservableValue a3 = m.getInput("A3");
        ObservableValue y3 = m.getOutput("Y3");

        for (Value vFinal : values) {
            for (Value vInitial : values) {
                vInitial.copyTo(a3);
                assertTrue("set initial"+vInitial, vInitial.isEqualTo(new Value(y3)));
                // switch from initial to v
                System.out.println("test from "+vInitial+" to "+vFinal);
                vFinal.copyTo(a3);
                assertTrue("from "+vInitial+" to "+vFinal, vFinal.isEqualTo(new Value(y3)));
            }
        }
         */
    }

}
