/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ProgramMemory;
import de.neemann.digital.core.memory.RAMInterface;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestingDataException;

import java.util.ArrayList;
import java.util.List;

/**
 * Is prepared by the test data parser and then used to initialize the
 * model for the test.
 */
public class ModelInitializer {
    private final ArrayList<ModelInit> inits;

    ModelInitializer() {
        this.inits = new ArrayList<>();
    }

    void initSignal(String name, long value) {
        inits.add(new InitSignal(name, value));
    }

    void initProgramMemory(DataField memory) {
        inits.add(new InitProgramMemory(memory));
    }

    void initMemory(String ramName, int addr, long value) {
        inits.add(new InitMemory(ramName, addr, value));
    }

    /**
     * Aplies the init steps to the given model
     *
     * @param model the model to initialize
     * @throws TestingDataException TestingDataException
     */
    public void init(Model model) throws TestingDataException {
        for (ModelInit mi : inits)
            mi.init(model);
    }

    private interface ModelInit {
        void init(Model model) throws TestingDataException;
    }

    private static final class InitSignal implements ModelInit {
        private final String name;
        private final long value;

        private InitSignal(String name, long value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void init(Model model) throws TestingDataException {
            Signal.Setter s = model.getSignalSetter(name);
            if (s == null)
                throw new TestingDataException(Lang.get("err_testSignal_N_notFound", name));
            s.set(value, 0);
        }
    }

    private static final class InitProgramMemory implements ModelInit {
        private final DataField dataField;

        private InitProgramMemory(DataField dataField) {
            this.dataField = dataField;
        }

        @Override
        public void init(Model model) throws TestingDataException {
            List<Node> nodes = model.findNode(n -> n instanceof ProgramMemory && ((ProgramMemory) n).isProgramMemory());
            switch (nodes.size()) {
                case 0:
                    throw new TestingDataException(Lang.get("err_noRomFound"));
                case 1:
                    ((ProgramMemory) nodes.get(0)).setProgramMemory(dataField);
                    break;
                default:
                    throw new TestingDataException(Lang.get("err_multipleRomsFound"));
            }
        }
    }

    private static final class InitMemory implements ModelInit {
        private final String memoryName;
        private final int addr;
        private final long value;

        private InitMemory(String memoryName, int addr, long value) {
            this.memoryName = memoryName;
            this.addr = addr;
            this.value = value;
        }

        @Override
        public void init(Model model) throws TestingDataException {
            List<Node> nodes = model.findNode(n -> n instanceof RAMInterface && ((RAMInterface) n).getLabel().equals(memoryName));
            switch (nodes.size()) {
                case 0:
                    throw new TestingDataException(Lang.get("err_noMemoryFound", memoryName));
                case 1:
                    ((RAMInterface) nodes.get(0)).getMemory().setData(addr, value);
                    break;
                default:
                    throw new TestingDataException(Lang.get("err_multipleMemoriesFound", memoryName));
            }
        }
    }
}
