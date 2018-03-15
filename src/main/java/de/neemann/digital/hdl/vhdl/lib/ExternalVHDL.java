/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.Application;
import de.neemann.digital.core.extern.ApplicationVHDLStdIO;
import de.neemann.digital.core.extern.Port;
import de.neemann.digital.core.extern.PortDefinition;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.Separator;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.util.HashMap;

import static de.neemann.digital.hdl.vhdl.VHDLGenerator.getType;

/**
 * Creates the vhdl code if a extrenal component is used.
 * Works only if the external component uses VHDL to define its behaviour.
 */
public class ExternalVHDL implements VHDLEntity {
    private HashMap<String, String> codeMap = new HashMap<>();

    @Override
    public void writeEntity(CodePrinter out, HDLNode node) throws IOException {
        out.print(node.get(Keys.EXTERNAL_CODE));
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        Application.Type t = node.getAttributes().get(Keys.APPLICATION_TYPE);
        Application app = Application.create(t);
        if (!(app instanceof ApplicationVHDLStdIO))
            throw new HDLException(Lang.get("err_canOnlyExportExtrnalVHDL"));

        return node.getAttributes().getCleanLabel();
    }

    @Override
    public boolean needsOutput(HDLNode node) throws HDLException {
        String label = node.getAttributes().getCleanLabel();
        String code = node.get(Keys.EXTERNAL_CODE);

        String oldCode = codeMap.get(label);
        if (oldCode == null) {
            codeMap.put(label, code);
            return true;
        }

        if (oldCode.equals(code))
            return false;

        throw new HDLException(Lang.get("err_ifExternalComponentIsUsedTwiceCodeMutBeIdentical"));
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        out.println("port (").inc();
        Separator comma = new Separator(";\n");
        for (Port p : new PortDefinition(node.getAttributes().get(Keys.EXTERNAL_INPUTS))) {
            comma.check(out);
            out.print(p.getName()).print(" : in ").print(getType(p.getBits()));
        }
        for (Port p : new PortDefinition(node.getAttributes().get(Keys.EXTERNAL_OUTPUTS))) {
            comma.check(out);
            out.print(p.getName()).print(" : out ").print(getType(p.getBits()));
        }
        out.println(");").dec();
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) {
    }

}
