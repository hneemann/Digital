/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.hdl.hgs.Parser;
import de.neemann.digital.hdl.hgs.ParserException;
import de.neemann.digital.hdl.hgs.refs.Reference;
import de.neemann.digital.hdl.hgs.refs.ReferenceToStruct;
import de.neemann.digital.hdl.hgs.refs.ReferenceToVar;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 * The description of a nested element.
 * This is a complete circuit which is used as a element.
 */
public final class ElementTypeDescriptionCustom extends ElementTypeDescription {
    private static final int MAX_DEPTH = 30;
    private final File file;
    private final Circuit circuit;
    private final ResolveGenerics resolveGenerics;
    private final LibraryInterface library;
    private String description;
    private NetList netList;
    private String declarationDefault;

    /**
     * Creates a new element
     *
     * @param file    the file which is loaded
     * @param circuit the circuit
     * @throws PinException PinException
     */
    ElementTypeDescriptionCustom(File file, Circuit circuit, ElementLibrary library) throws PinException {
        super(file.getName(), (ElementFactory) null, circuit.getInputNames());
        this.file = file;
        this.circuit = circuit;
        this.library = library;
        resolveGenerics = new ResolveGenerics(circuit, library);
        setShortName(file.getName());
        addAttribute(Keys.ROTATE);
        addAttribute(Keys.LABEL);
        addAttribute(Keys.SHAPE_TYPE);
        if (isGeneric())
            addAttribute(Keys.GENERIC);
    }

    /**
     * Returns the filename
     * The returned file is opened if the user wants to modify the element
     *
     * @return the filename
     */
    public File getFile() {
        return file;
    }

    /**
     * @return the elements attributes
     */
    public ElementAttributes getAttributes() {
        return circuit.getAttributes();
    }

    /**
     * @return the circuit
     */
    public Circuit getCircuit() {
        return circuit;
    }

    /**
     * Returns the resolved circuit if it is a generic circuit
     *
     * @param attributes the defining attributes
     * @return the resolved circuit
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public Circuit getResolvedCircuit(ElementAttributes attributes) throws NodeException, ElementNotFoundException {
        if (isGeneric())
            return resolveGenerics.resolveCircuit(attributes).getCircuit();
        else
            return circuit;
    }

    /**
     * Sets a custom description for this field
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription(ElementAttributes elementAttributes) {
        if (description != null)
            return description;
        else
            return super.getDescription(elementAttributes);
    }

    /**
     * Gets a {@link ModelCreator} of this circuit.
     * Every time this method is called a new {@link ModelCreator} is created.
     *
     * @param subName                 name of the circuit, used to name unique elements
     * @param depth                   recursion depth, used to detect a circuit which contains itself
     * @param containingVisualElement the containing visual element
     * @return the {@link ModelCreator}
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    ModelCreator getModelCreator(LibraryInterface library, String subName, int depth, VisualElement errorVisualElement, VisualElement containingVisualElement) throws PinException, NodeException, ElementNotFoundException {
        if (netList == null)
            netList = new NetList(circuit);

        if (depth > MAX_DEPTH)
            throw new NodeException(Lang.get("err_recursiveNestingAt_N0", circuit.getOrigin()));

        if (isGeneric()) {
            Circuit c = resolveGenerics.resolveCircuit(containingVisualElement.getElementAttributes()).getCircuit();

            return new ModelCreator(c, library, true, new NetList(new NetList(c), errorVisualElement), subName, depth, errorVisualElement);
        } else
            return new ModelCreator(circuit, library, true, new NetList(netList, errorVisualElement), subName, depth, errorVisualElement);
    }

    /**
     * @return the generics field default value
     * @throws NodeException NodeException
     */
    public String getDeclarationDefault() throws NodeException {
        if (declarationDefault == null)
            declarationDefault = createDeclarationDefault(circuit);
        return declarationDefault;
    }

    /**
     * Creates the default for custom element declarations
     *
     * @param circuit the circuit
     * @return the default code template
     * @throws NodeException NodeException
     */
    public static String createDeclarationDefault(Circuit circuit) throws NodeException {
        TreeSet<String> nameSet = new TreeSet<>();
        for (VisualElement ve : circuit.getElements()) {
            String gen = ve.getElementAttributes().get(Keys.GENERIC).trim();
            if (!gen.isEmpty()) {
                try {
                    Parser p = new Parser(gen);
                    p.enableRefReadCollection();
                    p.parse(false);
                    for (Reference r : p.getRefsRead()) {
                        if (r instanceof ReferenceToStruct) {
                            ReferenceToStruct st = (ReferenceToStruct) r;
                            if (st.getParent() instanceof ReferenceToVar) {
                                ReferenceToVar var = (ReferenceToVar) st.getParent();
                                if (var.getName().equals("args")) {
                                    nameSet.add(st.getName());
                                }
                            }
                        }
                    }
                } catch (ParserException | IOException e) {
                    final NodeException ex = new NodeException(Lang.get("err_evaluatingGenericsCode_N_N", ve, gen), e);
                    ex.setOrigin(circuit.getOrigin());
                    throw ex;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String name : nameSet)
            sb.append(name).append(" := ;\n");
        return sb.toString();
    }

    /**
     * @return true if the circuit is generic
     */
    public boolean isGeneric() {
        return circuit.getAttributes().get(Keys.IS_GENERIC);
    }

    @Override
    public PinDescriptions getInputDescription(ElementAttributes elementAttributes) throws NodeException {
        if (isGeneric()) {
            try {
                Circuit c = resolveGenerics.resolveCircuit(elementAttributes).getCircuit();
                return new PinDescriptions(c.getInputNames());
            } catch (Exception e) {
                return super.getInputDescription(elementAttributes);
            }
        } else
            return super.getInputDescription(elementAttributes);
    }

    @Override
    public PinDescriptions getOutputDescriptions(ElementAttributes elementAttributes) throws PinException {
        if (isGeneric()) {
            try {
                Circuit c = resolveGenerics.resolveCircuit(elementAttributes).getCircuit();
                return new PinDescriptions(c.getOutputNames());
            } catch (Exception e) {
                return super.getOutputDescriptions(elementAttributes);
            }
        } else
            return super.getOutputDescriptions(elementAttributes);
    }
}
