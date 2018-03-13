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
    private final static String ENTITY_PREFIX = "DIG_";
    private final Context staticContext;
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
            Parser p = new Parser(in)
                    .addFunction("type", new FunctionType())
                    .addFunction("value", new FunctionValue())
                    .addFunction("beginGenericPort", new Function(0) {
                        @Override
                        public Object calcValue(Context c, ArrayList<Expression> args) throws EvalException {
                            c.setVar("portStartPos", c.length());
                            return null;
                        }
                    })
                    .addFunction("endGenericPort", new Function(0) {
                        @Override
                        public Object calcValue(Context c, ArrayList<Expression> args) throws EvalException {
                            int start = Expression.toInt(c.getVar("portStartPos"));
                            String portDecl = c.toString().substring(start);
                            c.setVar("portDecl", portDecl);
                            return null;
                        }
                    })
                    .addFunction("registerGeneric", new Function(1) {
                        @Override
                        public Object calcValue(Context c, ArrayList<Expression> args) throws EvalException {
                            List<String> generics;
                            if (c.contains("generics"))
                                generics = (List<String>) c.getVar("generics");
                            else {
                                generics = new ArrayList<>();
                                c.setVar("generics", generics);
                            }
                            String name = Expression.toString(args.get(0).value(c));
                            generics.add(name);
                            return null;
                        }
                    });
            statements = p.parse();
            staticContext = p.getStaticContext();
        } catch (ParserException e) {
            throw new IOException("error parsing template", e);
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
        } catch (EvalException e) {
            throw new IOException("error evaluating the template", e);
        }
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        try {
            return getEntity(node).getName();
        } catch (EvalException e) {
            throw new HDLException("error requesting the entities name", e);
        }
    }

    @Override
    public boolean needsOutput(HDLNode node) throws HDLException {
        try {
            return !getEntity(node).isWritten();
        } catch (EvalException e) {
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
        } catch (EvalException e) {
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
                for (String name : e.getGenerics()) {
                    Key key = Keys.getKeyByName(name);
                    if (key != null) {
                        semic.check(out);
                        out.print(name).print(" => ").print(node.get(key).toString());
                    } else
                        throw new HDLException("unknown generic key: " + name);
                }
                out.println(")").dec();
            }
        } catch (EvalException e) {
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

    private String getEntityName(HDLNode node) throws EvalException {
        String name = entityName;
        if (staticContext.contains("entityName")) {
            Object funcObj = staticContext.getVar("entityName");
            if (funcObj instanceof FirstClassFunction)
                name = ((FirstClassFunction) funcObj).evaluate(node.getAttributes()).toString();
            else
                name = funcObj.toString();
        }
        return name;
    }

    private Entity getEntity(HDLNode node) throws EvalException {
        String name = getEntityName(node);
        Entity e = entities.get(name);
        if (e == null) {
            e = new Entity(node, name);
            entities.put(name, e);
        }
        return e;
    }


    private final class Entity {

        private final String code;
        private final String portDecl;
        private final String name;
        private final List<String> generics;
        private boolean isWritten = false;

        private Entity(HDLNode node, String name) throws EvalException {
            this.name = name;
            final Context c = new Context(staticContext, new StringBuilder())
                    .setVar("elem", node.getAttributes());
            statements.execute(c);
            code = c.toString();
            if (c.contains("portDecl"))
                portDecl = c.getVar("portDecl").toString();
            else
                portDecl = null;
            if (c.contains("generics"))
                generics = (List<String>) c.getVar("generics");
            else
                generics = null;
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

        public List<String> getGenerics() {
            return generics;
        }
    }

    private final static class FunctionType extends FuncAdapter {
        @Override
        protected Object f(long n) {
            if (n == 1)
                return "std_logic";
            else
                return "std_logic_vector (" + (n - 1) + " downto 0)";
        }
    }

    private final class FunctionValue extends Function {
        /**
         * Creates a new function
         */
        private FunctionValue() {
            super(2);
        }

        @Override
        public Object calcValue(Context c, ArrayList<Expression> args) throws EvalException {
            int val = Expression.toInt(args.get(0).value(c));
            int bits = Expression.toInt(args.get(1).value(c));
            return MultiplexerVHDL.getBin(val, bits);
        }
    }
}

