/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2.entities;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.hgs.function.JavaClass;
import de.neemann.digital.hdl.model2.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl2.Separator;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

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

    /**
     * Creates a new instance
     *
     * @param name the name of the entity
     * @param cl   the classloader used to load the template from
     * @throws IOException IOException
     */
    public VHDLTemplate(String name, ClassLoader cl) throws IOException {
        entityName = ENTITY_PREFIX + name;
        this.entities = new HashMap<>();
        try {
            statements = Parser.createFromJar(createFileName(entityName), cl);
        } catch (ParserException e) {
            throw new IOException("error parsing template " + createFileName(entityName), e);
        }
    }

    private static String createFileName(String name) {
        return "vhdl/" + name + ".tem";
    }

    /**
     * Returns true, if a hdl template is available.
     *
     * @param etd the {@link ElementTypeDescription}
     * @return true if VHDL template is available
     */
    public static boolean isTemplate(ElementTypeDescription etd) {
        ClassLoader cl = etd.getClassLoader();
        if (cl == null)
            cl = ClassLoader.getSystemClassLoader();
        URL url = cl.getResource(createFileName(ENTITY_PREFIX + etd.getName()));
        return url != null;
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
    public String print(CodePrinter out, HDLNode node, File root) throws HGSEvalException, IOException {
        Entity e = getEntity(node, root);
        if (!e.isWritten()) {
            out.println().println(Value.trimRight(e.getCode())).println();
            e.setWritten();
        }
        return e.getName();
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node, File root) throws IOException {
        try {
            final Entity e = getEntity(node, root);
            if (!e.getGenerics().isEmpty()) {
                out.println("generic map (").inc();
                Separator semic = new Separator(out, ",\n");
                for (VHDLTemplateFunctions.Generic gen : e.getGenerics()) {
                    semic.check();
                    final Object value = node.getElementAttributes().hgsMapGet(gen.getName());
                    out.print(gen.getName()).print(" => ").print(gen.format(value));
                }
                out.println(")").dec();
            }
        } catch (HGSEvalException e) {
            throw new IOException("error evaluating the template " + createFileName(entityName), e);
        }
    }

    private Entity getEntity(HDLNode node, File root) throws HGSEvalException {
        Entity newGenerated = new Entity(node, entityName, root);

        Entity e = entities.get(newGenerated.name);
        if (e == null) {
            entities.put(newGenerated.name, newGenerated);
            return newGenerated;
        } else {
            if (!(newGenerated.code.equals(e.code)))
                throw new HGSEvalException(Lang.get("err_ifExternalComponentIsUsedTwiceCodeMustBeIdentical_N", newGenerated.name));
            else
                return e;
        }
    }

    private final class Entity {
        private final String code;
        private final String name;
        private final VHDLTemplateFunctions helper;
        private boolean isWritten = false;

        private Entity(HDLNode node, String name, File root) throws HGSEvalException {
            helper = new VHDLTemplateFunctions();
            final Context c = new Context(root)
                    .declareVar("elem", node.getElementAttributes())
                    .declareVar("vhdl", TEMP_FUNCTIONS_CLASS.createMap(helper));

            try {
                statements.execute(c);
            } catch (HGSEvalException e) {
                throw new HGSEvalException("error evaluating hgs code " + name, e);
            }
            code = c.toString();

            if (c.contains("entityName"))
                this.name = c.getVar("entityName").toString();
            else
                this.name = name;
        }

        private String getCode() {
            return code;
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

