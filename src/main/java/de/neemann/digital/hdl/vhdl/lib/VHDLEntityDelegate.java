package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * Base class for delegates
 */
public abstract class VHDLEntityDelegate implements VHDLEntity {

    /**
     * get the delegate
     *
     * @param node the node
     * @return the delegate class
     */
    public abstract VHDLEntity getDelegate(HDLNode node);

    @Override
    public void writeHeader(CodePrinter out, HDLNode node) throws IOException {
        getDelegate(node).writeHeader(out, node);
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        return getDelegate(node).getName(node);
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        return getDelegate(node).needsOutput(node);
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        getDelegate(node).writeDeclaration(out, node);
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException, HDLException {
        getDelegate(node).writeArchitecture(out, node);
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException, HDLException {
        getDelegate(node).writeGenericMap(out, node);
    }

    @Override
    public boolean createsSignals(HDLNode node) {
        return getDelegate(node).createsSignals(node);
    }

    @Override
    public String getDescription(HDLNode node) {
        return getDelegate(node).getDescription(node);
    }
}
