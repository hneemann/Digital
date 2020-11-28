/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.modification.ModifyAttribute;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestExecutor;
import de.neemann.digital.testing.TestingDataException;
import de.neemann.digital.testing.parser.ParserException;
import de.neemann.digital.undo.Modification;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CircuitModifierTest extends TestCase {

    private Modification<Circuit> mod;

    public void testEEPROMModification() throws IOException, ElementNotFoundException, PinException, NodeException, TestingDataException, ParserException {
        // load circuit
        File file = new File(Resources.getRoot(), "dig/test/eeprom.dig");
        ElementLibrary library = new ElementLibrary();
        ShapeFactory shapeFactory = new ShapeFactory(library);
        Circuit c = Circuit.loadCircuit(file, shapeFactory);

        // pic test case
        List<VisualElement> l = c.getElements(v -> v.equalsDescription(TestCaseElement.DESCRIPTION));
        assertEquals(1, l.size());
        TestCaseDescription testCase = l.get(0).getElementAttributes().get(Keys.TESTDATA);

        // create the model and configure it like it was running in the gui
        // fetch a modification in mod
        CircuitModifierPostClosed cmpc = new CircuitModifierPostClosed(modification -> mod = modification);
        ModelCreator mc = new ModelCreator(c, library);
        Model model = mc.createModel(false);
        mc.connectToGui(cmpc);
        model.addObserver(cmpc);

        // executes a test case which writes to the eeprom
        new TestExecutor(testCase, model).execute();

        // there must by a modification
        assertNotNull(mod);

        // check if it is the correct modification
        ModifyAttribute<DataField> ma = (ModifyAttribute<DataField>) mod;
        DataField data = ma.getValue();
        for (int i = 0; i < 30; i++)
            assertEquals(i, data.getDataWord(i));
    }
}