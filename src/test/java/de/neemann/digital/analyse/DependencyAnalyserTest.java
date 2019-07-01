/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class DependencyAnalyserTest extends TestCase {

    private static final int[] VAL = new int[]{1, 1, 1, 2, 6, 5, 5, 3, 8, 7, 7, 5, 10, 9, 9, 7, 2, 14, 14, 14, 14, 14, 14, 14, 2, 2};

    public void testAnalyzer() throws Exception {
        Model model = new ToBreakRunner("dig/backtrack/Plexer.dig").getModel();
        ModelAnalyser m = new ModelAnalyser(model);
        DependencyAnalyser da = new DependencyAnalyser(m);

        assertEquals(17, m.getInputs().size());
        assertEquals(26, m.getOutputs().size());
        for (int i = 0; i < m.getOutputs().size(); i++)
            assertEquals("" + i, VAL[i], da.getInputs(m.getOutputs().get(i)).size());
    }

/*
ExpressionCreator - p0n+1 reduced from 17 to 1 variables ([Count])
ExpressionCreator - p1n+1 reduced from 17 to 1 variables ([p0n])
ExpressionCreator - Q_1n+1 reduced from 17 to 1 variables ([Q_0n])
ExpressionCreator - Q_0n+1 reduced from 17 to 2 variables ([Q_1n, Q_0n])
ExpressionCreator - C0Q_3n+1 reduced from 17 to 6 variables ([p0n, p1n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n])
ExpressionCreator - C0Q_2n+1 reduced from 17 to 5 variables ([p0n, p1n, C0Q_2n, C0Q_1n, C0Q_0n])
ExpressionCreator - C0Q_1n+1 reduced from 17 to 5 variables ([p0n, p1n, C0Q_3n, C0Q_1n, C0Q_0n])
ExpressionCreator - C0Q_0n+1 reduced from 17 to 3 variables ([p0n, p1n, C0Q_0n])
ExpressionCreator - C1Q_3n+1 reduced from 17 to 8 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n])
ExpressionCreator - C1Q_2n+1 reduced from 17 to 7 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_2n, C1Q_1n, C1Q_0n])
ExpressionCreator - C1Q_1n+1 reduced from 17 to 7 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_1n, C1Q_0n])
ExpressionCreator - C1Q_0n+1 reduced from 17 to 5 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_0n])
ExpressionCreator - C2Q_3n+1 reduced from 17 to 10 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - C2Q_2n+1 reduced from 17 to 9 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_0n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - C2Q_1n+1 reduced from 17 to 9 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_0n, C2Q_3n, C2Q_1n, C2Q_0n])
ExpressionCreator - C2Q_0n+1 reduced from 17 to 7 variables ([p0n, p1n, C0Q_3n, C0Q_0n, C1Q_3n, C1Q_0n, C2Q_0n])
ExpressionCreator - s2 reduced from 17 to 2 variables ([Q_1n, Q_0n])
ExpressionCreator - d0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - c0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - b0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - a0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - e0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - f0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - g0 reduced from 17 to 14 variables ([Q_1n, Q_0n, C0Q_3n, C0Q_2n, C0Q_1n, C0Q_0n, C1Q_3n, C1Q_2n, C1Q_1n, C1Q_0n, C2Q_3n, C2Q_2n, C2Q_1n, C2Q_0n])
ExpressionCreator - s1 reduced from 17 to 2 variables ([Q_1n, Q_0n])
ExpressionCreator - s0 reduced from 17 to 2 variables ([Q_1n, Q_0n])
 */


    public void testInputInvert() throws Exception {
        Model model = new ToBreakRunner("dig/backtrack/InputInvert.dig").getModel();
        ModelAnalyser m = new ModelAnalyser(model);
        DependencyAnalyser da = new DependencyAnalyser(m);

        assertEquals(1, m.getOutputs().size());
        Signal out = m.getOutputs().get(0);

        ArrayList<Signal> inputs = da.getInputs(out);
        assertEquals(2, inputs.size());
    }

    public void testSplitter() throws Exception {
        Model model = new ToBreakRunner("dig/backtrack/Splitter.dig").getModel();
        ModelAnalyser m = new ModelAnalyser(model);
        DependencyAnalyser da = new DependencyAnalyser(m);
        assertEquals(2, m.getOutputs().size());
        assertEquals(1, da.getInputs(m.getOutputs().get(0)).size());
        assertEquals(1, da.getInputs(m.getOutputs().get(1)).size());
    }

    public void testBacktrackCompleteness() throws Exception {
        File f = new File(Resources.getRoot(), "dig/backtrack/AllComponents.dig");

        final ElementLibrary library = new ElementLibrary();
        library.setRootFilePath(f.getParentFile());
        ShapeFactory shapeFactory = new ShapeFactory(library);
        Circuit circuit = Circuit.loadCircuit(f, shapeFactory);

        // create a set of all components used in the circuit
        Set<String> set = new HashSet<>();
        for (VisualElement e : circuit.getElements())
            set.add(e.getElementName());

        // ensure all available components are included in test circuit
        for (ElementLibrary.ElementContainer c : library) {
            if (!set.contains(c.getDescription().getName())) {
                // nodes with state are allowed to be missing
                Element n = c.getDescription().createElement(new ElementAttributes());
                boolean ok = (n instanceof Node) && ((Node) n).hasState();
                assertTrue("component " + c.getDescription().getName() + " is missing in test/resources/dig/backtrack/AllComponents.dig!", ok);
            }
        }

        // check if backtracking is ok at all components!
        Model model = new ModelCreator(circuit, new SubstituteLibrary(library)).createModel(false);
        ModelAnalyser m = new ModelAnalyser(model);
        new DependencyAnalyser(m);
    }

}
