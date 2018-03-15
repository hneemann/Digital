/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Key;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * A parameterized template.
 */
public class VHDLEntityParam implements VHDLEntity {

    private final VHDLEntity template;
    private final TempParameter param;

    /**
     * creates a new template with parameters
     *
     * @param template the templates to use
     * @param param    the parameters to pass to the template
     */
    public VHDLEntityParam(VHDLEntity template, TempParameter param) {
        this.template = template;
        this.param = param;
    }

    private void setParam(HDLNode node) {
        node.getAttributes().set(new Key<>("param", 0), param);
    }

    @Override
    public void writeEntity(CodePrinter out, HDLNode node) throws IOException, HDLException {
        setParam(node);
        template.writeEntity(out, node);
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        setParam(node);
        return template.getName(node);
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        setParam(node);
        template.writeDeclaration(out, node);
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException, HDLException {
        setParam(node);
        template.writeGenericMap(out, node);
    }

}
