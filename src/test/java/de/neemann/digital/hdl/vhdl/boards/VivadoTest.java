package de.neemann.digital.hdl.vhdl.boards;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLModel;
import de.neemann.digital.hdl.model.ModelList;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class VivadoTest extends TestCase {

    public void testConstrains() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/busInOut.dig");

        HDLModel model = new HDLModel(br.getCircuit(), br.getLibrary(), null).setName("main");

        CodePrinterStr cp = new CodePrinterStr();

        Vivado v = new Vivado("testType", "clockPin", 1, null);
        v.writeConstraints(cp, model);

        assertEquals(
                "set_property PACKAGE_PIN V17 [get_ports PORT_A[0]]\n" +
                        "set_property IOSTANDARD testType [get_ports PORT_A[0]]\n" +
                        "set_property PACKAGE_PIN V16 [get_ports PORT_A[1]]\n" +
                        "set_property IOSTANDARD testType [get_ports PORT_A[1]]\n" +
                        "\n" +
                        "set_property PACKAGE_PIN U16 [get_ports PORT_X[0]]\n" +
                        "set_property IOSTANDARD testType [get_ports PORT_X[0]]\n" +
                        "set_property PACKAGE_PIN E19 [get_ports PORT_X[1]]\n" +
                        "set_property IOSTANDARD testType [get_ports PORT_X[1]]\n" +
                        "\n" +
                        "set_property PACKAGE_PIN W16 [get_ports PORT_B]\n" +
                        "set_property IOSTANDARD testType [get_ports PORT_B]\n" +
                        "\n" +
                        "set_property PACKAGE_PIN U19 [get_ports PORT_Y]\n" +
                        "set_property IOSTANDARD testType [get_ports PORT_Y]\n" +
                        "\n" +
                        "set_property CFGBVS VCCO  [current_design]\n" +
                        "set_property CONFIG_VOLTAGE 3.3 [current_design]\n", cp.toString());
    }
}