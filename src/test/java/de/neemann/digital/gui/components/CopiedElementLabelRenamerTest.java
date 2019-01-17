/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Movable;
import de.neemann.digital.draw.elements.VisualElement;
import junit.framework.TestCase;

import java.util.ArrayList;

public class CopiedElementLabelRenamerTest extends TestCase {

    private CopiedElementLabelRenamer.LabelInstance getLI(String elementName, String label) {
        return CopiedElementLabelRenamer.LabelInstance.create(new VisualElement(elementName).setAttribute(Keys.LABEL, label));
    }

    public void testLabelClass() {
        assertNull(getLI("In", "val"));
        assertNull(getLI("In", ""));

        CopiedElementLabelRenamer.LabelInstance li = getLI("In", "val 1");
        assertEquals("val ", li.getLabelClass().getLabel());
        assertEquals("In", li.getLabelClass().getElementName());
        assertEquals(1, li.getNumber());


        li = getLI("Out", "O0");
        assertEquals("O", li.getLabelClass().getLabel());
        assertEquals("Out", li.getLabelClass().getElementName());
        assertEquals(0, li.getNumber());

        li = getLI("Out", "O_10");
        assertEquals("O_", li.getLabelClass().getLabel());
        assertEquals("Out", li.getLabelClass().getElementName());
        assertEquals(10, li.getNumber());
    }

    public void testRenameSimple() {
        final VisualElement in = new VisualElement("In").setAttribute(Keys.LABEL, "I_1");
        Circuit c = new Circuit().add(in);
        ArrayList<Movable> list = new ArrayList<>();
        list.add(new VisualElement(in));
        ArrayList<Movable> newList = new CopiedElementLabelRenamer(c, list).rename();
        assertEquals(1, newList.size());
        assertTrue(newList.get(0) instanceof VisualElement);
        assertEquals("I_2", ((VisualElement) newList.get(0)).getElementAttributes().getLabel());
    }

    public void testRenameTwo() {
        final VisualElement in1 = new VisualElement("In").setAttribute(Keys.LABEL, "I_1");
        final VisualElement in2 = new VisualElement("In").setAttribute(Keys.LABEL, "I_2");
        Circuit c = new Circuit().add(in1).add(in2);
        ArrayList<Movable> list = new ArrayList<>();
        list.add(new VisualElement(in2));
        list.add(new VisualElement(in1));
        ArrayList<Movable> newList = new CopiedElementLabelRenamer(c, list).rename();
        assertEquals("I_4", ((VisualElement) newList.get(0)).getElementAttributes().getLabel());
        assertEquals("I_3", ((VisualElement) newList.get(1)).getElementAttributes().getLabel());
    }

    public void testRenameWithPinNumber() {
        final VisualElement in = new VisualElement("In").setAttribute(Keys.LABEL, "I_1").setAttribute(Keys.PINNUMBER,"3");
        Circuit c = new Circuit().add(in);
        ArrayList<Movable> list = new ArrayList<>();
        list.add(new VisualElement(in));
        ArrayList<Movable> newList = new CopiedElementLabelRenamer(c, list).rename();
        assertEquals("I_1", ((VisualElement) newList.get(0)).getElementAttributes().getLabel());
    }

    public void testRenameIgnore() {
        final VisualElement in = new VisualElement("In").setAttribute(Keys.LABEL, "In");
        Circuit c = new Circuit();
        ArrayList<Movable> list = new ArrayList<>();
        list.add(new VisualElement(in));
        ArrayList<Movable> newList = new CopiedElementLabelRenamer(c, list).rename();
        assertEquals("In", ((VisualElement) newList.get(0)).getElementAttributes().getLabel());
    }

    public void testRenameNotInCircuit() {
        final VisualElement in = new VisualElement("In").setAttribute(Keys.LABEL, "In_1");
        Circuit c = new Circuit();
        ArrayList<Movable> list = new ArrayList<>();
        list.add(new VisualElement(in));
        ArrayList<Movable> newList = new CopiedElementLabelRenamer(c, list).rename();
        assertEquals("In_1", ((VisualElement) newList.get(0)).getElementAttributes().getLabel());
    }

    public void testRenameNotInCircuit2() {
        Circuit c = new Circuit().add(new VisualElement("In").setAttribute(Keys.LABEL, "In_1"));
        ArrayList<Movable> list = new ArrayList<>();
        list.add(new VisualElement(new VisualElement("In").setAttribute(Keys.LABEL, "In_2")));
        ArrayList<Movable> newList = new CopiedElementLabelRenamer(c, list).rename();
        assertEquals("In_2", ((VisualElement) newList.get(0)).getElementAttributes().getLabel());
    }

}