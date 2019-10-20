/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ProgramMemory;
import de.neemann.digital.core.memory.RAMInterface;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UnitTester {
    private final ShapeFactory sf;
    private final ElementLibrary library;
    private final Model model;
    private boolean initCalled = false;

    public UnitTester(File file) throws IOException, ElementNotFoundException, PinException, NodeException {
        library = new ElementLibrary();
        library.setRootFilePath(file.getParentFile());
        sf = new ShapeFactory(library);
        Circuit circuit = Circuit.loadCircuit(file, sf);
        model = new ModelCreator(circuit, library).createModel(false);
    }

    public UnitTester writeDataTo(MemoryFilter filter, DataField data) throws TestException {
        getMemory(filter).setProgramMemory(data);
        return this;
    }

    public ProgramMemory getMemory(MemoryFilter filter) throws TestException {
        Node node = getNode(n -> n instanceof ProgramMemory && filter.accept((ProgramMemory) n));
        return (ProgramMemory) node;
    }

    public RAMInterface getRAM() throws TestException {
        return getRAM(pm -> true);
    }

    public RAMInterface getRAM(MemoryFilter filter) throws TestException {
        Node node = getNode(n -> n instanceof RAMInterface && filter.accept((RAMInterface) n));
        return (RAMInterface) node;
    }

    public Node getNode(Model.NodeFilter<Node> filter) throws TestException {
        List<Node> list = model.findNode(filter);
        if (list.size() == 0)
            throw new TestException("no node found");
        else if (list.size() > 1)
            throw new TestException("multiple nodes found");
        else {
            return list.get(0);
        }
    }

    public UnitTester runToBreak() throws TestException, NodeException {
        if (!model.isRunToBreakAllowed())
            throw new TestException("model has no break or no clock element");

        getModel().runToBreak();
        return this;
    }

    public Model getModel() throws NodeException {
        if (!initCalled) {
            model.init();
            initCalled = true;
        }
        return model;
    }

    public static class TestException extends Exception {
        private TestException(String message) {
            super(message);
        }
    }

    public interface MemoryFilter {
        boolean accept(ProgramMemory pm);
    }
}
