/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.hgs.function.Function;
import de.neemann.digital.hdl.hgs.function.InnerFunction;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.Separator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.neemann.digital.hdl.vhdl.VHDLLibrary.writePort;

/**
 * Reads a file with the vhdl code to create the entity
 */
public class VHDLTemplate implements VHDLEntity {

    private static final Context VHDLCONTEXT = new Context()
            .declareFunc("zero", new FunctionZero())
            .declareFunc("type", new FunctionType())
            .declareFunc("genericType", new FunctionGenericType())
            .declareFunc("value", new FunctionValue())
            .declareFunc("beginGenericPort", new InnerFunction(0) {
                @Override
                public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
                    c.declareVar("portStartPos", c.length());
                    return null;
                }
            })
            .declareFunc("endGenericPort", new InnerFunction(0) {
                @Override
                public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
                    int start = Value.toInt(c.getVar("portStartPos"));
                    String portDecl = c.toString().substring(start);
                    c.declareVar("portDecl", portDecl);
                    return null;
                }
            })
            .declareFunc("registerGeneric", new InnerFunction(-1) {
                @Override
                public Object call(Context c, ArrayList<Expression> args) throws HGSEvalException {
                    List<Generic> generics;
                    if (c.contains("generics"))
                        generics = (List<Generic>) c.getVar("generics");
                    else {
                        generics = new ArrayList<>();
                        c.declareVar("generics", generics);
                    }
                    String name = Value.toString(args.get(0).value(c));
                    if (args.size() == 1)
                        generics.add(new Generic(name, "Integer"));
                    else if (args.size() == 2)
                        generics.add(new Generic(name, Value.toString(args.get(1).value(c))));
                    else
                        throw new HGSEvalException("registerGeneric needs one or two arguments!");
                    return null;
                }
            });

    private final static String ENTITY_PREFIX = "DIG_";
    private final Statement statements;
    private final String entityName;
    private HashMap<String, Entity> entities;

    /**
     * Creates a new instance
     *
     * @param name the name of the entity
     * @throws IOException IOException
     */
    public VHDLTemplate(String name) throws IOException {
        entityName = ENTITY_PREFIX + name;
        this.entities = new HashMap<>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(createFileName(entityName));
        if (inputStream == null)
            throw new IOException("file not present: " + createFileName(entityName));
        try (Reader in = new InputStreamReader(inputStream, "utf-8")) {
            Parser p = new Parser(in);
            statements = p.parse();
        } catch (ParserException e) {
            throw new IOException("error parsing template " + createFileName(entityName), e);
        }
    }

    private static String createFileName(String name) {
        return "vhdl/" + name + ".tem";
    }

    /**
     * Creates the name of the file used to load the vhdl file for the given element
     *
     * @param elementName the element name
     * @return the filename
     */
    public static String neededFileName(String elementName) {
        return createFileName(ENTITY_PREFIX + elementName);
    }

    @Override
    public void writeEntity(CodePrinter out, HDLNode node) throws IOException {
        try {
            Entity e = getEntity(node);
            if (!e.isWritten()) {
                out.print(e.getCode());
                e.setWritten(true);
            }
        } catch (HGSEvalException e) {
            throw new IOException("error evaluating the template " + createFileName(entityName), e);
        }
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        try {
            return getEntity(node).getName();
        } catch (HGSEvalException e) {
            throw new HDLException("Error requesting the entities name of " + createFileName(entityName), e);
        }
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        try {
            String port = getEntity(node).getPortDecl();
            if (port != null) {
                out.dec().print(port).inc();
            } else {
                out.println("port (").inc();
                Separator semic = new Separator(";\n");
                for (Port p : node.getPorts()) {
                    semic.check(out);
                    writePort(out, p);
                }
                out.println(" );").dec();
            }
        } catch (HGSEvalException e) {
            throw new IOException("error evaluating the template " + createFileName(entityName), e);
        }
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException {
        try {
            final Entity e = getEntity(node);
            if (e.getGenerics() != null) {
                out.println("generic map (").inc();
                Separator semic = new Separator(",\n");
                for (Generic gen : e.getGenerics()) {
                    semic.check(out);
                    final Object value = node.getAttributes().hgsMapGet(gen.name);
                    out.print(gen.name).print(" => ").print(gen.format(value));
                }
                out.println(")").dec();
            }
        } catch (HGSEvalException e) {
            throw new IOException("error evaluating the template " + createFileName(entityName), e);
        }
    }

    private Entity getEntity(HDLNode node) throws HGSEvalException {
        Entity newGenerated = new Entity(node, entityName);

        Entity e = entities.get(newGenerated.name);
        if (e == null) {
            entities.put(newGenerated.name, newGenerated);
            return newGenerated;
        } else {
            if (!(newGenerated.code.equals(e.code)))
                throw new HGSEvalException("Multiple entities with same name '" + newGenerated.name + "'!");
            else
                return e;
        }
    }

    private final class Entity {

        private final String code;
        private final String portDecl;
        private final String name;
        private final List<Generic> generics;
        private boolean isWritten = false;

        private Entity(HDLNode node, String name) throws HGSEvalException {
            final Context c = new Context(VHDLCONTEXT)
                    .declareVar("elem", node.getAttributes());
            statements.execute(c);
            code = c.toString();
            if (c.contains("portDecl"))
                portDecl = c.getVar("portDecl").toString();
            else
                portDecl = null;
            if (c.contains("generics"))
                generics = (List<Generic>) c.getVar("generics");
            else
                generics = null;

            if (c.contains("entityName"))
                this.name = c.getVar("entityName").toString();
            else
                this.name = name;
        }

        private String getCode() {
            return code;
        }

        private String getPortDecl() {
            return portDecl;
        }

        private boolean isWritten() {
            return isWritten;
        }

        private void setWritten(boolean written) {
            isWritten = written;
        }

        private String getName() {
            return name;
        }

        public List<Generic> getGenerics() {
            return generics;
        }
    }

    private final static class FunctionType extends Function {

        private FunctionType() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            int bits = Value.toInt(args[0]);
            if (bits == 0)
                throw new HGSEvalException("zero bits is not allowed!");
            if (bits == 1)
                return "std_logic";
            else
                return "std_logic_vector (" + (bits - 1) + " downto 0)";
        }

    }

    private final static class FunctionGenericType extends Function {

        private FunctionGenericType() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            int n = Value.toInt(args[0]);
            if (n == 1)
                return "std_logic";
            else
                return "std_logic_vector ((Bits-1) downto 0)";
        }

    }

    private final static class FunctionZero extends Function {

        private FunctionZero() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            int n = Value.toInt(args[0]);
            if (n == 1)
                return "'0'";
            else
                return "(others => '0')";
        }

    }

    private final static class FunctionValue extends Function {
        /**
         * Creates a new function
         */
        private FunctionValue() {
            super(2);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            int val = Value.toInt(args[0]);
            int bits = Value.toInt(args[1]);
            return getBin(val, bits);
        }

        private static String getBin(int val, int bits) {
            String s = Integer.toBinaryString(val);
            while (s.length() < bits)
                s = "0" + s;

            if (bits > 1)
                s = "\"" + s + "\"";
            else
                s = "'" + s + "'";

            return s;
        }

    }

    private static final class Generic {

        private final String name;
        private final String type;

        private Generic(String name, String type) {
            this.name = name;
            this.type = type.toLowerCase();
        }

        public String format(Object o) throws HGSEvalException {
            switch (type) {
                case "integer":
                    return Long.toString(Value.toLong(o));
                case "real":
                    return Double.toString(Value.toDouble(o));
                case "std_logic":
                    return "'" + (Value.toBool(o) ? 1 : 0) + "'";
                default:
                    throw new HGSEvalException("type " + type + " not allowed as generic");
            }
        }
    }
}

