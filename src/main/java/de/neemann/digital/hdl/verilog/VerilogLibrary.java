/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog;

import de.neemann.digital.core.arithmetic.Add;
import de.neemann.digital.core.arithmetic.BitExtender;
import de.neemann.digital.core.arithmetic.Comparator;
import de.neemann.digital.core.arithmetic.Mul;
import de.neemann.digital.core.arithmetic.Sub;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.basic.NAnd;
import de.neemann.digital.core.basic.NOr;
import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.basic.Or;
import de.neemann.digital.core.basic.XNOr;
import de.neemann.digital.core.basic.XOr;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.Ground;
import de.neemann.digital.core.io.VDD;
import de.neemann.digital.core.memory.Register;
import de.neemann.digital.core.wiring.BitSelector;
import de.neemann.digital.core.wiring.Decoder;
import de.neemann.digital.core.wiring.Demultiplexer;
import de.neemann.digital.core.wiring.Driver;
import de.neemann.digital.core.wiring.DriverInvSel;
import de.neemann.digital.core.wiring.Multiplexer;
import de.neemann.digital.core.wiring.PriorityEncoder;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.verilog.lib.RegisterVerilog;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import de.neemann.digital.hdl.verilog.lib.AddVerilog;
import de.neemann.digital.hdl.verilog.lib.BitExtenderVerilog;
import de.neemann.digital.hdl.verilog.lib.BitSelectorVerilog;
import de.neemann.digital.hdl.verilog.lib.ComparatorVerilog;
import de.neemann.digital.hdl.verilog.lib.ConstVerilog;
import de.neemann.digital.hdl.verilog.lib.CustomElemVerilog;
import de.neemann.digital.hdl.verilog.lib.DecoderVerilog;
import de.neemann.digital.hdl.verilog.lib.DemultiplexerVerilog;
import de.neemann.digital.hdl.verilog.lib.DriverVerilog;
import de.neemann.digital.hdl.verilog.lib.MulVerilog;
import de.neemann.digital.hdl.verilog.lib.MultiplexerVerilog;
import de.neemann.digital.hdl.verilog.lib.NotVerilog;
import de.neemann.digital.hdl.verilog.lib.VerilogElement;
import de.neemann.digital.hdl.verilog.lib.OperateVerilog;
import de.neemann.digital.hdl.verilog.lib.PriorityEncoderVerilog;
import de.neemann.digital.hdl.verilog.lib.SplitterVerilog;
import de.neemann.digital.hdl.verilog.lib.VerilogFileTemplate;
import de.neemann.digital.lang.Lang;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ideras
 */
public class VerilogLibrary {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerilogLibrary.class);

    private final CustomElemVerilog customElemVerilog;
    private final ElementLibrary elementLibrary;
    private final HashMap<String, VerilogElement> map;
    private final ArrayList<HDLNode> nodeList = new ArrayList<>();

    /**
     * Creates a new instance
     *
     * @param elementLibrary the elements library
     */
    public VerilogLibrary(ElementLibrary elementLibrary) {
        this.elementLibrary = elementLibrary;
        map = new HashMap<>();

        put(And.DESCRIPTION, new OperateVerilog(VOperator.AND, And.DESCRIPTION));
        put(NAnd.DESCRIPTION, new OperateVerilog(VOperator.NAND, NAnd.DESCRIPTION));
        put(Or.DESCRIPTION, new OperateVerilog(VOperator.OR, Or.DESCRIPTION));
        put(NOr.DESCRIPTION, new OperateVerilog(VOperator.NOR, NOr.DESCRIPTION));
        put(XOr.DESCRIPTION, new OperateVerilog(VOperator.XOR, XOr.DESCRIPTION));
        put(XNOr.DESCRIPTION, new OperateVerilog(VOperator.XNOR, XNOr.DESCRIPTION));
        put(Not.DESCRIPTION, new NotVerilog());
        put(Comparator.DESCRIPTION, new ComparatorVerilog());
        put(Splitter.DESCRIPTION, new SplitterVerilog());
        put(Const.DESCRIPTION, new ConstVerilog(Const.DESCRIPTION));
        put(VDD.DESCRIPTION, new ConstVerilog(VDD.DESCRIPTION));
        put(Ground.DESCRIPTION, new ConstVerilog(Ground.DESCRIPTION));
        put(PriorityEncoder.DESCRIPTION, new PriorityEncoderVerilog());
        put(Multiplexer.DESCRIPTION, new MultiplexerVerilog());
        put(Demultiplexer.DESCRIPTION, new DemultiplexerVerilog());
        put(BitExtender.DESCRIPTION, new BitExtenderVerilog());
        put(BitSelector.DESCRIPTION, new BitSelectorVerilog());
        put(Add.DESCRIPTION, new AddVerilog(Add.DESCRIPTION, VOperator.ADD));
        put(Sub.DESCRIPTION, new AddVerilog(Sub.DESCRIPTION, VOperator.SUB));
        put(Register.DESCRIPTION, new RegisterVerilog());
        put(Driver.DESCRIPTION, new DriverVerilog(false));
        put(DriverInvSel.DESCRIPTION, new DriverVerilog(true));
        put(Decoder.DESCRIPTION, new DecoderVerilog());
        put(Mul.DESCRIPTION, new MulVerilog());
        customElemVerilog = new CustomElemVerilog();
    }

    private void put(ElementTypeDescription description, VerilogElement velem) {
        map.put(description.getName(), velem);
    }

    /**
     * Returns the associated verilog element for a given node
     *
     * @param node the HDL node
     * @return the associated verilog element.
     * @throws HDLException HDLException
     */
    public VerilogElement getVerilogElement(HDLNode node) throws HDLException {
        String elementName = node.getOrigName();
        VerilogElement e = map.get(elementName);

        if (e == null) {
            if (node.isCustom()) {
                return customElemVerilog;
            } else {
                ElementTypeDescription description = null;
                try {
                    description = elementLibrary.getElementType(elementName);
                } catch (ElementNotFoundException e1) {
                    // does not matter, affects only comments in the vhdl file
                }
                try {
                    e = new VerilogFileTemplate(elementName, description);
                    map.put(elementName, e);
                } catch (IOException ex) {
                    LOGGER.info(ex.getMessage());
                }
            }
        }

        if (e == null) {
            throw new HDLException(Lang.get("err_verilogNoElement_N", elementName));
        }

        return e;
    }

    /**
     * Returns the verilog name of the given node
     *
     * @param node the node
     * @return the name
     */
    public String getName(HDLNode node) {
        if (!nodeList.contains(node)) {
            nodeList.add(node);
            node.setHDLName(node.getOrigName());

        }
        return node.getHDLName();
    }
}
