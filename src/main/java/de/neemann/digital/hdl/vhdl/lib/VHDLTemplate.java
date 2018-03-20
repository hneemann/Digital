/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.hgs.function.JavaClass;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.Separator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static de.neemann.digital.hdl.vhdl.VHDLLibrary.writePort;

/**
 * Reads a file with the vhdl code to create the entity.
 */
public class VHDLTemplate implements VHDLEntity {
    private static final JavaClass<VHDLTemplateFunctions> TEMP_FUNCTIONS_CLASS
            = new JavaClass<>(VHDLTemplateFunctions.class);
    private static final String ENTITY_PREFIX = "DIG_";

    private final Statement statements;
    private final String entityName;
    private HashMap<String, Entity> entities;
    private TempParameter parameter;

    /**
     * Creates a new instance
     *
     * @param name the name of the entity
     * @throws IOException IOException
     */
    public VHDLTemplate(String name) throws IOException {
        entityName = ENTITY_PREFIX + name;
        this.entities = new HashMap<>();
        try {
            statements = Parser.createFromJar(createFileName(entityName));
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

    /**
     * Sets the parameters for the template execution if there are some.
     * The given struct is set to the templates context as "param" map.
     *
     * @param parameter the parameters to use
     */
    public void setParameter(TempParameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void writeEntity(CodePrinter out, HDLNode node) throws IOException {
        try {
            Entity e = getEntity(node);
            if (!e.isWritten()) {
                out.print(e.getCode());
                e.setWritten();
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
            if (!e.getGenerics().isEmpty()) {
                out.println("generic map (").inc();
                Separator semic = new Separator(",\n");
                for (VHDLTemplateFunctions.Generic gen : e.getGenerics()) {
                    semic.check(out);
                    final Object value = node.getAttributes().hgsMapGet(gen.getName());
                    out.print(gen.getName()).print(" => ").print(gen.format(value));
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
        private final String name;
        private final VHDLTemplateFunctions helper;
        private boolean isWritten = false;

        private Entity(HDLNode node, String name) throws HGSEvalException {
            helper = new VHDLTemplateFunctions();
            final Context c = new Context()
                    .declareVar("elem", node.getAttributes())
                    .declareVar("vhdl", TEMP_FUNCTIONS_CLASS.createMap(helper));
            if (parameter != null)
                c.declareVar("param", parameter);

            statements.execute(c);
            code = c.toString();

            if (c.contains("entityName"))
                this.name = c.getVar("entityName").toString();
            else
                this.name = name;
        }

        private String getCode() {
            return code;
        }

        private String getPortDecl() {
            return helper.getPortDecl();
        }

        private boolean isWritten() {
            return isWritten;
        }

        private void setWritten() {
            isWritten = true;
        }

        private String getName() {
            return name;
        }

        private ArrayList<VHDLTemplateFunctions.Generic> getGenerics() {
            return helper.getGenerics();
        }
    }

}

