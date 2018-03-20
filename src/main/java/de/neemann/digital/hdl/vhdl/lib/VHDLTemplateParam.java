/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * A parameterized template.
 */
public class VHDLTemplateParam implements VHDLEntity {
    private final VHDLTemplate template;
    private final TempParameter param;

    /**
     * Creates a new template with parameters.
     * For code generation the given template is used, with the given
     * parameters applied to the entity node.
     *
     * @param template the templates to use
     * @param param    the parameters to pass to the template
     */
    public VHDLTemplateParam(VHDLTemplate template, TempParameter param) {
        this.template = template;
        this.param = param;
    }

    @Override
    public void writeEntity(CodePrinter out, HDLNode node) throws IOException {
        template.setParameter(param);
        template.writeEntity(out, node);
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        template.setParameter(param);
        return template.getName(node);
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        template.setParameter(param);
        template.writeDeclaration(out, node);
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException {
        template.setParameter(param);
        template.writeGenericMap(out, node);
    }

}
