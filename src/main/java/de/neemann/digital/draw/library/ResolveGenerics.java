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
import java.util.Objects;

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
    public CircuitHolder resolveCircuit(VisualElement visualElement, Circuit circuit, LibraryInterface library) throws NodeException, ElementNotFoundException {
        final Args args = createArgs(visualElement, circuit);

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
                                .declareFunc("setCircuit", new SetCircuitFunc(ve));
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
        return new CircuitHolder(c, args);
    }

    private Args createArgs(VisualElement visualElement, Circuit circuit) throws NodeException {
        Context context;
        if (visualElement != null) {
            context = visualElement.getGenericArgs();
            if (context == null) {
                String argsCode = visualElement.getElementAttributes().get(Keys.GENERIC);
                try {
                    Statement s = getStatement(argsCode);
                    context = new Context();
                    s.execute(context);
                } catch (HGSEvalException | ParserException | IOException e) {
                    final NodeException ex = new NodeException(Lang.get("err_evaluatingGenericsCode_N_N", visualElement, argsCode), e);
                    ex.setOrigin(circuit.getOrigin());
                    throw ex;
                }
            }
        } else
            context = new Context();

        return new Args(context);
    }

    private Statement getStatement(String code) throws IOException, ParserException {
        Statement genS = map.get(code);
        if (genS == null) {
            genS = new Parser(code).parse(false);
            map.put(code, genS);
        }
        return genS;
    }

    /**
     * Holds the args of a circuit.
     * Implements the access to the parents args values.
     */
    public static final class Args implements HGSMap {
        private final Context args;

        private Args(Context args) {
            this.args = args;
        }

        @Override
        public Object hgsMapGet(String key) throws HGSEvalException {
            Object v = args.hgsMapGet(key);
            if (v == null) {
                Object a = args.hgsMapGet("args");
                if (a instanceof HGSMap) {
                    return ((HGSMap) a).hgsMapGet(key);
                }
            }
            return v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Args that = (Args) o;
            return args.equals(that.args);
        }

        @Override
        public int hashCode() {
            return Objects.hash(args);
        }
    }

    /**
     * Holds the circuit and the args that created that circuit.
     */
    public static final class CircuitHolder {
        private final Circuit circuit;
        private final Args args;

        private CircuitHolder(Circuit circuit, Args args) {
            this.circuit = circuit;
            this.args = args;
        }

        /**
         * @return teturns the created circuit
         */
        public Circuit getCircuit() {
            return circuit;
        }

        /**
         * @return the args that created the circuit
         */
        public Args getArgs() {
            return args;
        }
    }

    private static final class SetCircuitFunc extends Function {
        private final VisualElement ve;

        private  SetCircuitFunc(VisualElement ve) {
            super(1);
            this.ve = ve;
        }

        @Override
        protected Object f(Object... args) {
            ve.setElementName(args[0].toString());
            return null;
        }

        // All setCircuit functions are considered identical.
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o != null && getClass() == o.getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
