/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.analyse.SubstituteLibrary;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.hgs.function.Function;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.util.HashMap;

/**
 * Resolves a generic circuit and makes it non generic
 */
public class ResolveGenerics {

    private final HashMap<String, Statement> map;

    /**
     * Creates a new instance
     */
    public ResolveGenerics() {
        map = new HashMap<>();
    }

    /**
     * Resolves the generics
     *
     * @param visualElement the visual element
     * @param circuit       the circuit to resolve
     * @param library       the library to ude
     * @return the resolved circuit
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public Circuit resolveCircuit(VisualElement visualElement, Circuit circuit, LibraryInterface library) throws NodeException, ElementNotFoundException {
        Context args;
        if (visualElement != null) {
            args = visualElement.getGenericArgs();
            if (args == null) {
                String argsCode = visualElement.getElementAttributes().get(Keys.GENERIC);
                try {
                    Statement s = getStatement(argsCode);
                    args = new Context();
                    s.execute(args);
                } catch (HGSEvalException | ParserException | IOException e) {
                    final NodeException ex = new NodeException(Lang.get("err_evaluatingGenericsCode_N_N", visualElement, argsCode), e);
                    ex.setOrigin(circuit.getOrigin());
                    throw ex;
                }
            }
        } else
            args = new Context();

        Circuit c = circuit.createDeepCopy();
        for (VisualElement ve : c.getElements()) {
            String gen = ve.getElementAttributes().get(Keys.GENERIC).trim();
            try {
                if (!gen.isEmpty()) {
                    boolean isCustom = library.getElementType(ve.getElementName(), ve.getElementAttributes()).isCustom();
                    Statement genS = getStatement(gen);
                    if (isCustom) {
                        Context mod = new Context()
                                .declareVar("args", args)
                                .declareFunc("setCircuit", new Function(1) {
                                    @Override
                                    protected Object f(Object... args) {
                                        ve.setElementName(args[0].toString());
                                        return null;
                                    }
                                });
                        genS.execute(mod);
                        ve.setGenericArgs(mod);
                    } else {
                        Context mod = new Context()
                                .declareVar("args", args)
                                .declareVar("this", new SubstituteLibrary.AllowSetAttributes(ve.getElementAttributes()));
                        genS.execute(mod);
                    }
                }
            } catch (HGSEvalException | ParserException | IOException e) {
                final NodeException ex = new NodeException(Lang.get("err_evaluatingGenericsCode_N_N", ve, gen), e);
                ex.setOrigin(circuit.getOrigin());
                throw ex;
            }
        }
        return c;
    }

    private Statement getStatement(String code) throws IOException, ParserException {
        Statement genS = map.get(code);
        if (genS == null) {
            genS = new Parser(code).parse(false);
            map.put(code, genS);
        }
        return genS;
    }

}
