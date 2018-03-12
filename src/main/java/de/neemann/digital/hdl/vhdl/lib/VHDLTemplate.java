/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;

/**
 * Reads a file with the vhdl code to create the entity
 */
public class VHDLTemplate implements VHDLEntity {
    private final static String ENTITY_PREFIX = "DIG_";
    private final Context staticContext;
    private final Statement statements;
    private HashSet<String> entitiesWritten;
    private String entityName;

    /**
     * Creates a new instance
     *
     * @param name the name of the entity
     * @throws IOException IOException
     */
    public VHDLTemplate(String name) throws IOException {
        entityName = ENTITY_PREFIX + name;
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(createFileName(entityName));
        if (inputStream == null)
            throw new IOException("file not present: " + createFileName(entityName));
        try (Reader in = new InputStreamReader(inputStream, "utf-8")) {
            Parser p = new Parser(in);
            statements = p.parse();
            staticContext = p.getStaticContext();
        } catch (ParserException e) {
            throw new IOException("error parsing template", e);
        }
        entitiesWritten = new HashSet<>();
    }

    private static String createFileName(String name) {
        return "vhdl/" + name + ".tem";
    }

    @Override
    public void writeHeader(CodePrinter out, HDLNode node) throws IOException {
        try {
            statements.execute(new Context().setVar("elem", node.getAttributes()));
            entitiesWritten.add(getEntityName(node));
        } catch (EvalException e) {
            throw new IOException("error evaluating the template", e);
        }
    }

    private String getEntityName(HDLNode node) throws EvalException {
        String name = entityName;
        if (staticContext.contains("name")) {
            Object funcObj = staticContext.getVar("name");
            if (funcObj instanceof FirstClassFunction)
                name = ((FirstClassFunction) funcObj).evaluate(node.getAttributes()).toString();
        }
        return name;
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        try {
            return getEntityName(node);
        } catch (EvalException e) {
            throw new HDLException("error requesting the entities name", e);
        }
    }

    @Override
    public boolean needsOutput(HDLNode node) throws HDLException {
        try {
            return !entitiesWritten.contains(getEntityName(node));
        } catch (EvalException e) {
            throw new HDLException("error requesting the entities name", e);
        }
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {

    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException, HDLException {
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException, HDLException {

    }

    @Override
    public String getDescription(HDLNode node) {
        return null;
    }
}
