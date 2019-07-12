/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementFactory;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
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
    private String description;
    private NetList netList;
    private boolean isCustom = true;
    private String declarationDefault;

    /**
     * Creates a new element
     *
     * @param file    the file which is loaded
     * @param circuit the circuit
     * @throws PinException PinException
     */
    ElementTypeDescriptionCustom(File file, Circuit circuit) throws PinException {
        super(file.getName(), (ElementFactory) null, circuit.getInputNames());
        this.file = file;
        this.circuit = circuit;
        resolveGenerics = new ResolveGenerics();
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
     * @param library                 the library used
     * @return the {@link ModelCreator}
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    ModelCreator getModelCreator(String subName, int depth, VisualElement errorVisualElement, VisualElement containingVisualElement, LibraryInterface library) throws PinException, NodeException, ElementNotFoundException {
        if (netList == null)
            netList = new NetList(circuit);

        if (depth > MAX_DEPTH)
            throw new NodeException(Lang.get("err_recursiveNestingAt_N0", circuit.getOrigin()));

        if (isGeneric()) {
            Circuit c = resolveGenerics.resolveCircuit(containingVisualElement, circuit, library).getCircuit();

            return new ModelCreator(c, library, true, new NetList(netList, errorVisualElement), subName, depth, errorVisualElement);
        } else
            return new ModelCreator(circuit, library, true, new NetList(netList, errorVisualElement), subName, depth, errorVisualElement);
    }

    @Override
    public boolean isCustom() {
        return isCustom;
    }

    /**
     * Used by element substitution to allow to mark custom circuits which replace a built-in
     * component to be not custom.
     *
     * @return this for chained calls
     */
    public ElementTypeDescriptionCustom isSubstitutedBuiltIn() {
        isCustom = false;
        return this;
    }

    /**
     * @return the generics field default value
     * @throws NodeException NodeException
     */
    public String getDeclarationDefault() throws NodeException {
        if (declarationDefault == null)
            declarationDefault = createDeclarationDefault();
        return declarationDefault;
    }

    private String createDeclarationDefault() throws NodeException {
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
}
