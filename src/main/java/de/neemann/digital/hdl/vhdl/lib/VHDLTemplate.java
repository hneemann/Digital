/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.hgs.function.FuncAdapter;
import de.neemann.digital.hdl.hgs.function.Function;
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

import static de.neemann.digital.hdl.vhdl.lib.VHDLEntitySimple.writePort;

/**
 * Reads a file with the vhdl code to create the entity
 */
public class VHDLTemplate implements VHDLEntity {

    private static final Context VHDLCONTEXT = new Context()
            .addFunc("zero", new FunctionZero())
            .addFunc("type", new FunctionType())
            .addFunc("genericType", new FunctionGenericType())
            .addFunc("value", new FunctionValue())
            .addFunc("beginGenericPort", new Function(0) {
                @Override
                public Object calcValue(Context c, ArrayList<Expression> args) throws HGSEvalException {
                    c.setVar("portStartPos", c.length());
                    return null;
                }
            })
            .addFunc("endGenericPort", new Function(0) {
                @Override
                public Object calcValue(Context c, ArrayList<Expression> args) throws HGSEvalException {
                    int start = Value.toInt(c.getVar("portStartPos"));
                    String portDecl = c.toString().substring(start);
                    c.setVar("portDecl", portDecl);
                    return null;
                }
            })
            .addFunc("registerGeneric", new Function(-1) {
                @Override
                public Object calcValue(Context c, ArrayList<Expression> args) throws HGSEvalException {
                    List<Generic> generics;
                    if (c.contains("generics"))
                        generics = (List<Generic>) c.getVar("generics");
                    else {
                        generics = new ArrayList<>();
                        c.setVar("generics", generics);
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
    private String entityName;
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

    @Override
    public void writeHeader(CodePrinter out, HDLNode node) throws IOException {
        try {
            Entity e = getEntity(node);
            out.print(e.getCode());
            e.setWritten(true);
        } catch (HGSEvalException e) {
            throw new IOException("error evaluating the template", e);
        }
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        try {
            return getEntity(node).getName();
        } catch (HGSEvalException e) {
            throw new HDLException("error requesting the entities name", e);
        }
    }

    @Override
    public boolean needsOutput(HDLNode node) throws HDLException {
        try {
            return !getEntity(node).isWritten();
        } catch (HGSEvalException e) {
            throw new HDLException("error requesting the entities name", e);
        }
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        try {
            String port = getEntity(node).getPortDecl();
            if (port != null) {
                out.print(port);
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
            throw new IOException("error evaluating the template", e);
        }
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws HDLException, IOException {
        try {
            final Entity e = getEntity(node);
            if (e.getGenerics() != null) {
                out.println("generic map (").inc();
                Separator semic = new Separator(",\n");
                for (Generic gen : e.getGenerics()) {
                    Key key = Keys.getKeyByName(gen.name);
                    if (key != null) {
                        semic.check(out);
                        out.print(gen.name).print(" => ").print(gen.format(node.get(key)));
                    } else
                        throw new HDLException("unknown generic key: " + gen.name);
                }
                out.println(")").dec();
            }
        } catch (HGSEvalException e) {
            throw new IOException("error evaluating the template", e);
        }
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) {
    }

    @Override
    public String getDescription(HDLNode node) {
        return null;
    }

    private Entity getEntity(HDLNode node) throws HGSEvalException {
        Entity newGenerated = new Entity(node, entityName);

        Entity e = entities.get(newGenerated.name);
        if (e == null) {
            entities.put(newGenerated.name, newGenerated);
            return newGenerated;
        } else {
            if (!(newGenerated.code.equals(e.code)))
                throw new HGSEvalException("multiple entities with same name " + newGenerated.name);
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
                    .setVar("elem", node.getAttributes());
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

    private final static class FunctionType extends FuncAdapter {

        private FunctionType() {
            super(1);
        }

        @Override
        protected Object f(Object... args) throws HGSEvalException {
            int n = Value.toInt(args[0]);
            if (n == 1)
                return "std_logic";
            else
                return "std_logic_vector (" + (n - 1) + " downto 0)";
        }

    }

    private final static class FunctionGenericType extends FuncAdapter {

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

    private final static class FunctionZero extends FuncAdapter {

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
        public Object calcValue(Context c, ArrayList<Expression> args) throws HGSEvalException {
            int val = Value.toInt(args.get(0).value(c));
            int bits = Value.toInt(args.get(1).value(c));
            return MultiplexerVHDL.getBin(val, bits);
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
            long v = Value.toLong(o);
            switch (type) {
                case "integer":
                    return Long.toString(v);
                case "std_logic":
                    return "'" + (v & 1) + "'";
                default:
                    throw new HGSEvalException("type " + type + " not allowed as generic");
            }
        }
    }
}

