/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.testing.TestingDataException;
import junit.framework.TestCase;

import java.io.IOException;

public class ModelInitializerTest extends TestCase {

    public void test_program() throws IOException, ParserException, TestingDataException {
        ModelInitializer mi = new Parser("A B Y\n" +
                "program(1,2,3,4)\n" +
                "1 1 1").parse().getModelInitializer();

        Model m = new Model();
        RAMSinglePort ram = m.add(new RAMSinglePort(new ElementAttributes().set(Keys.IS_PROGRAM_MEMORY, true)));

        mi.init(m);

        DataField program = ram.getMemory();
        assertNotNull(program);
        assertEquals(4, program.trim());
        assertEquals(1, program.getDataWord(0));
        assertEquals(2, program.getDataWord(1));
        assertEquals(3, program.getDataWord(2));
        assertEquals(4, program.getDataWord(3));
        assertEquals(0, program.getDataWord(4));
    }

    public void test_signal() throws IOException, ParserException, TestingDataException {
        ModelInitializer mi = new Parser("A B Y\n" +
                "init s1=5;\n" +
                "init s2=-1;\n" +
                "1 1 1").parse().getModelInitializer();

        Model m = new Model();
        ObservableValue s1 = new ObservableValue("s", 8);
        ObservableValue s2 = new ObservableValue("s", 8);
        m.addSignal(new Signal("s1", s1, (value, highZ) -> s1.setValue(value)));
        m.addSignal(new Signal("s2", s2, (value, highZ) -> s2.setValue(value)));

        mi.init(m);

        assertEquals(5, s1.getValue());
        assertEquals(-1, s2.getValueSigned());
    }

    public void test_ram() throws IOException, ParserException, TestingDataException {
        ModelInitializer mi = new Parser("A B Y\n" +
                "memory myRam(0)=1;\n" +
                "memory myRam(1)=2;\n" +
                "1 1 1").parse().getModelInitializer();

        Model m = new Model();
        RAMSinglePort ram = m.add(new RAMSinglePort(new ElementAttributes().set(Keys.LABEL, "myRam")));

        mi.init(m);

        DataField ramData = ram.getMemory();
        assertNotNull(ram);
        assertEquals(2, ramData.trim());
        assertEquals(1, ramData.getDataWord(0));
        assertEquals(2, ramData.getDataWord(1));


        m = new Model();
        m.add(new RAMSinglePort(new ElementAttributes().set(Keys.LABEL, "wrongName")));
        try {
            mi.init(m);
            fail();
        } catch (TestingDataException e) {
            assertTrue(e.getMessage().contains("myRam"));
        }
    }

}