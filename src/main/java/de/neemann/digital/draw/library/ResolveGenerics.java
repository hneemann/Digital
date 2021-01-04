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
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.hgs.function.Function;
import de.neemann.digital.hdl.hgs.function.InnerFunction;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.util.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * Resolves a generic circuit and makes it non generic
 */
public class ResolveGenerics {

    private final HashMap<String, Statement> map;
    private LibraryInterface library;

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
        this.library = library;
        final Circuit c = circuit.createDeepCopy();
        ArrayList<VisualElement> newComponents = new ArrayList<>();

        final Args args = createArgs(visualElement, c, newComponents);

        for (VisualElement ve : c.getElements()) {
            String gen = ve.getElementAttributes().get(Keys.GENERIC).trim();
            try {
                if (!gen.isEmpty()) {
                    boolean isCustom = library.getElementType(ve.getElementName(), ve.getElementAttributes()).isCustom();
                    Statement genS = getStatement(gen);
                    Context mod = createContext(c, newComponents);
                    if (isCustom) {
                        mod.declareVar("args", args)
                                .declareFunc("setCircuit", new SetCircuitFunc(ve));
                        genS.execute(mod);
                    } else {
                        mod.declareVar("args", args)
                                .declareVar("this", new SubstituteLibrary.AllowSetAttributes(ve.getElementAttributes()));
                        genS.execute(mod);
                    }
                    ve.setGenericArgs(mod);
                }
            } catch (HGSEvalException | ParserException | IOException e) {
                final NodeException ex = new NodeException(Lang.get("err_evaluatingGenericsCode_N_N", ve, gen), e);
                ex.setOrigin(circuit.getOrigin());
                throw ex;
            }
        }
        for (VisualElement ve : newComponents)
            c.add(ve);

        return new CircuitHolder(c, args);
    }

    private Context createContext(Circuit circuit, ArrayList<VisualElement> newComponents) throws NodeException {
        try {
            Context context = new Context();
            if (circuit.getOrigin() != null)
                context.declareVar(Context.BASE_FILE_KEY, circuit.getOrigin().getPath());
            context.declareFunc("addWire", new AddWire(circuit));
            context.declareFunc("addComponent", new AddComponent(newComponents));
            return context;
        } catch (HGSEvalException e) {
            throw new NodeException("error setting the base filename", e);
        }
    }

    private Args createArgs(VisualElement visualElement, Circuit circuit, ArrayList<VisualElement> newComponents) throws NodeException {
        Context context;
        if (visualElement != null) {
            context = visualElement.getGenericArgs();
            if (context == null) {
                String argsCode = visualElement.getElementAttributes().get(Keys.GENERIC);
                try {
                    Statement s = getStatement(argsCode);
                    context = createContext(circuit, newComponents);
                    s.execute(context);
                } catch (HGSEvalException | ParserException | IOException e) {
                    final NodeException ex = new NodeException(Lang.get("err_evaluatingGenericsCode_N_N", visualElement, argsCode), e);
                    ex.setOrigin(circuit.getOrigin());
                    throw ex;
                }
            }
        } else {
            context = createContext(circuit, newComponents);
            List<VisualElement> g = circuit.getElements(v -> v.equalsDescription(GenericInitCode.DESCRIPTION) && v.getElementAttributes().get(Keys.ENABLED));
            if (g.size() == 0)
                throw new NodeException(Lang.get("err_noGenericInitCode"));
            if (g.size() > 1)
                throw new NodeException(Lang.get("err_multipleGenericInitCodes"));
            String argsCode = g.get(0).getElementAttributes().get(Keys.GENERIC);
            try {
                getStatement(argsCode).execute(context);
            } catch (IOException | ParserException | HGSEvalException e) {
                throw new NodeException(Lang.get("err_inGenericInitCode"), e);
            }
        }

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
    public final class CircuitHolder {
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

        /**
         * Converts a circuit that is only suitable for creating a model
         * to a circuit that can also be edited and saved.
         *
         * @return this for chained calls
         */
        public CircuitHolder cleanupConcreteCircuit() {
            for (VisualElement gic : circuit.getElements(v -> v.equalsDescription(GenericInitCode.DESCRIPTION)))
                circuit.delete(gic);
            for (VisualElement v : circuit.getElements()) {
                try {
                    boolean isCustom = library.getElementType(v.getElementName(), v.getElementAttributes()).isCustom();
                    if (isCustom)
                        v.getElementAttributes().set(Keys.GENERIC, createGenericCode(v.getGenericArgs()));
                    else
                        v.getElementAttributes().set(Keys.GENERIC, "");
                } catch (ElementNotFoundException e) {
                    // can not happen
                    e.printStackTrace();
                }
                v.setGenericArgs(null);
            }

            circuit.getAttributes().set(Keys.IS_GENERIC, false);

            return this;
        }
    }

    private static String createGenericCode(Context args) {
        StringBuilder sb = new StringBuilder();
        HashSet<String> contentSet = new HashSet<>();
        addVal(sb, "", args, contentSet);
        return sb.toString();
    }

    private static void addVal(StringBuilder sb, String key, Object val, HashSet<String> contentSet) {
        if (contentSet.contains(key))
            return;

        if (val instanceof InnerFunction)
            return;

        if (val instanceof Context) {
            Context c = (Context) val;
            for (String k : c.getKeySet()) {
                Object v = c.hgsMapGet(k);
                if (!(v instanceof Args))
                    addVal(sb, k, v, contentSet);
            }
            for (String k : c.getKeySet()) {
                Object v = c.hgsMapGet(k);
                if (v instanceof Args)
                    addVal(sb, k, ((Args) v).args, contentSet);
            }
            return;
        }

        if (!key.equals(Context.BASE_FILE_KEY)) {
            contentSet.add(key);
            sb.append(key).append(":=");
            if (val instanceof String) {
                sb.append("\"");
                escapeString(sb, (String) val);
                sb.append("\"");
            } else if (val instanceof Integer)
                sb.append("int(").append(val).append(")");
            else
                sb.append(val);
            sb.append(";\n");
        }
    }

    static void escapeString(StringBuilder sb, String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                default:
                    sb.append(c);
            }
        }
    }

    private static final class SetCircuitFunc extends Function {
        private final VisualElement ve;

        private SetCircuitFunc(VisualElement ve) {
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

    private final static class AddWire extends Function {
        private final Circuit circuit;

        private AddWire(Circuit circuit) {
            super(4);
            this.circuit = circuit;
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            Vector p1 = new Vector(Value.toInt(args[0]) * SIZE, Value.toInt(args[1]) * SIZE);
            Vector p2 = new Vector(Value.toInt(args[2]) * SIZE, Value.toInt(args[3]) * SIZE);
            circuit.add(new Wire(p1, p2));
            return null;
        }
    }

    private final class AddComponent extends Function {
        private final ArrayList<VisualElement> newComponents;

        private AddComponent(ArrayList<VisualElement> newComponents) {
            super(3);
            this.newComponents = newComponents;
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            String name = args[0].toString();
            Vector pos = new Vector(Value.toInt(args[1]) * SIZE, Value.toInt(args[2]) * SIZE);
            VisualElement ve = new VisualElement(name).setPos(pos).setShapeFactory(library.getShapeFactory());
            newComponents.add(ve);
            return new SubstituteLibrary.AllowSetAttributes(ve.getElementAttributes());
        }
    }
}
