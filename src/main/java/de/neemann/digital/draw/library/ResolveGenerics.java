/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.analyse.SubstituteLibrary;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.hgs.function.Function;
import de.neemann.digital.hdl.hgs.function.InnerFunction;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * Resolves a generic circuit and makes it non generic
 */
public class ResolveGenerics {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolveGenerics.class);

    /**
     * Key uses to store the args for the generic circuits
     */
    public static final String GEN_ARGS_KEY = "genArgs";
    private static final String SETTINGS_KEY = "settings";
    private static final String GLOBALS_KEY = "global";
    private static final String THIS_KEY = "this";
    private final HashMap<String, Statement> map;
    private final HashMap<Args, CircuitHolder> circuitMap;
    private final Circuit circuit;
    private final LibraryInterface library;

    /**
     * Creates a new instance
     *
     * @param circuit the circuit to resolve
     * @param library the library to ude
     */
    public ResolveGenerics(Circuit circuit, LibraryInterface library) {
        this.circuit = circuit;
        this.library = library;
        map = new HashMap<>();
        circuitMap = new HashMap<>();
    }

    /**
     * Resolves the generics
     *
     * @param attributes the visual elements attributes
     * @return the resolved circuit
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public CircuitHolder resolveCircuit(ElementAttributes attributes) throws NodeException, ElementNotFoundException {
        try {
            Args args;
            if (attributes == null)
                args = createArgsFromGenericInitBlock();
            else
                args = createArgsFromParentCircuitEmbedding(attributes);

            CircuitHolder ch = circuitMap.get(args);
            if (ch == null) {
                ch = createResolvedCircuit(args);
                circuitMap.put(args, ch);
            }
            return ch;
        } catch (NodeException e) {
            e.setOrigin(circuit.getOrigin());
            throw e;
        }
    }

    private Args createArgsFromParentCircuitEmbedding(ElementAttributes attributes) throws NodeException {
        Context context = (Context) attributes.getFromCache(GEN_ARGS_KEY);
        if (context == null) {
            String argsCode = attributes.get(Keys.GENERIC);
            try {
                Statement s = getStatement(argsCode);
                context = new Context(circuit.getOrigin());
                s.execute(context);
            } catch (HGSEvalException | ParserException | IOException e) {
                throw new NodeException(Lang.get("err_evaluatingGenericsCode_N_N", null, argsCode), e);
            }
        }
        return new Args(context);
    }

    private Args createArgsFromGenericInitBlock() throws NodeException {
        Context context = new Context(circuit.getOrigin());
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
        if (circuit.getOrigin() != null) {
            try {
                context.declareVar(Context.BASE_FILE_KEY, circuit.getOrigin());
            } catch (HGSEvalException e) {
                // impossible
            }
        }

        return new Args(context);
    }

    private CircuitHolder createResolvedCircuit(Args args) throws NodeException, ElementNotFoundException {
        LOGGER.debug("create concrete circuit based on " + circuit.getOrigin() + " width: " + args);
        final Circuit c = circuit.createDeepCopy();
        ArrayList<VisualElement> newComponents = new ArrayList<>();
        ArrayList<Wire> newWires = new ArrayList<>();

        Globals globals = new Globals();
        for (VisualElement ve : c.getElements())
            if (ve.equalsDescription(GenericCode.DESCRIPTION)) {
                handleVisualElement(c, ve, args, newComponents, newWires, globals);
                globals.lock(); // allow write only in first code component
            }
        globals.lock(); // allow write only in code components
        for (VisualElement ve : c.getElements())
            if (!ve.equalsDescription(GenericCode.DESCRIPTION))
                handleVisualElement(c, ve, args, newComponents, newWires, globals);

        c.add(newWires);
        for (VisualElement ve : newComponents)
            c.add(ve);

        return new CircuitHolder(c, args);
    }

    private void handleVisualElement(Circuit c, VisualElement ve, Args args, ArrayList<VisualElement> newComponents, ArrayList<Wire> newWires, Globals globals) throws ElementNotFoundException, NodeException {
        ElementAttributes elementAttributes = ve.getElementAttributes();
        String gen = elementAttributes.get(Keys.GENERIC).trim();
        try {
            if (!gen.isEmpty()) {
                ElementTypeDescription elementTypeDescription = library.getElementType(ve.getElementName(), elementAttributes);

                boolean isCustom = elementTypeDescription instanceof ElementTypeDescriptionCustom;
                Statement genS = getStatement(gen);
                Context mod = createContext(c, newComponents, newWires, args)
                        .declareVar(GLOBALS_KEY, globals)
                        .declareVar("args", args);
                if (isCustom)
                    mod.declareFunc("setCircuit", new SetCircuitFunc(ve));
                else
                    mod.declareVar("this", new SubstituteLibrary.AllowSetAttributes(elementAttributes));
                genS.execute(mod);
                elementAttributes.putToCache(GEN_ARGS_KEY, mod);
            }
        } catch (HGSEvalException | ParserException | IOException e) {
            throw new NodeException(Lang.get("err_evaluatingGenericsCode_N_N", ve, gen), e);
        }
    }

    private Context createContext(Circuit circuit, ArrayList<VisualElement> newComponents, ArrayList<Wire> newWires, Args args) throws NodeException {
        try {
            Context context = new Context(circuit.getOrigin());
            if (circuit.getOrigin() != null)
                context.declareVar(Context.BASE_FILE_KEY, circuit.getOrigin());
            context.declareVar(SETTINGS_KEY, new SubstituteLibrary.AllowSetAttributes(circuit.getAttributes()));
            context.declareFunc("addWire", new AddWire(newWires));
            context.declareFunc("addComponent", new AddComponent(newComponents, args));
            return context;
        } catch (HGSEvalException e) {
            throw new NodeException("error setting the base filename", e);
        }
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

        @Override
        public String toString() {
            return "[" + args.toStringKeys() + "]";
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
            for (VisualElement gic : circuit.getElements(v ->
                    v.equalsDescription(GenericInitCode.DESCRIPTION)
                            || v.equalsDescription(GenericCode.DESCRIPTION)))
                circuit.delete(gic);
            for (VisualElement v : circuit.getElements()) {
                try {
                    boolean isCustom = library.getElementType(v.getElementName(), v.getElementAttributes()) instanceof ElementTypeDescriptionCustom;
                    if (isCustom)
                        v.getElementAttributes().set(Keys.GENERIC, createGenericCode((Context) v.getElementAttributes().getFromCache(GEN_ARGS_KEY)));
                    else
                        v.getElementAttributes().set(Keys.GENERIC, "");
                } catch (ElementNotFoundException e) {
                    // can not happen
                    e.printStackTrace();
                }
                v.getElementAttributes().removeFromCache(GEN_ARGS_KEY);
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

        if (!key.equals(Context.BASE_FILE_KEY) && !key.equals(SETTINGS_KEY) && !key.equals(GLOBALS_KEY) && !key.equals(THIS_KEY)) {
            contentSet.add(key);
            sb.append(key).append(":=");
            addToStringBuilder(sb, val);
            sb.append(";\n");
        }
    }

    private static void addToStringBuilder(StringBuilder sb, Object val) {
        if (val instanceof String) {
            sb.append("\"");
            escapeString(sb, (String) val);
            sb.append("\"");
        } else if (val instanceof Integer)
            sb.append("int(").append(val).append(")");
        else if (val instanceof List) {
            sb.append("[");
            boolean first = true;
            for (Object o : (List<?>) val) {
                if (first)
                    first = false;
                else
                    sb.append(",");
                addToStringBuilder(sb, o);
            }
            sb.append("]");
        } else if (val instanceof Map) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) val).entrySet()) {
                if (first)
                    first = false;
                else
                    sb.append(",");
                sb.append(e.getKey().toString());
                sb.append(":");
                addToStringBuilder(sb, e.getValue());
            }
            sb.append("}");
        } else
            sb.append(val);
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
        private final ArrayList<Wire> wires;

        private AddWire(ArrayList<Wire> wires) {
            super(4);
            this.wires = wires;
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            Vector p1 = new Vector(Value.toInt(args[0]) * SIZE, Value.toInt(args[1]) * SIZE);
            Vector p2 = new Vector(Value.toInt(args[2]) * SIZE, Value.toInt(args[3]) * SIZE);
            wires.add(new Wire(p1, p2));
            return null;
        }
    }

    private final class AddComponent extends Function {
        private final ArrayList<VisualElement> newComponents;
        private final Args args;

        private AddComponent(ArrayList<VisualElement> newComponents, Args args) {
            super(3);
            this.newComponents = newComponents;
            this.args = args;
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            String name = args[0].toString();
            Vector pos = new Vector(Value.toInt(args[1]) * SIZE, Value.toInt(args[2]) * SIZE);
            VisualElement ve = new VisualElement(name).setPos(pos).setShapeFactory(library.getShapeFactory());
            newComponents.add(ve);

            ElementAttributes elementAttributes = ve.getElementAttributes();
            try {
                ElementTypeDescription etd = library.getElementType(ve.getElementName(), ve.getElementAttributes());
                if (etd instanceof ElementTypeDescriptionCustom) {
                    ElementTypeDescriptionCustom etdc = (ElementTypeDescriptionCustom) etd;
                    if (etdc.isGeneric()) {
                        Context c = new Context(circuit.getOrigin()) {
                            @Override
                            public void hgsMapPut(String key, Object val) throws HGSEvalException {
                                this.declareVar(key, val);
                            }
                        }
                                .declareVar("args", this.args)
                                .declareVar(THIS_KEY, new SubstituteLibrary.AllowSetAttributes(elementAttributes));
                        elementAttributes.putToCache(GEN_ARGS_KEY, c);
                        return c;
                    }
                }
            } catch (ElementNotFoundException e) {
                e.printStackTrace();
            }

            return new SubstituteLibrary.AllowSetAttributes(elementAttributes);
        }
    }

    private static final class Globals implements HGSMap {
        private final HashMap<String, Object> map = new HashMap<>();
        private boolean writeEnable = true;

        @Override
        public void hgsMapPut(String key, Object val) throws HGSEvalException {
            if (writeEnable)
                map.put(key, val);
            else
                throw new HGSEvalException(Lang.get("err_writeInCodeComponentsOnly"));
        }

        @Override
        public Object hgsMapGet(String key) throws HGSEvalException {
            return map.get(key);
        }

        public void lock() {
            writeEnable = false;
        }
    }
}
