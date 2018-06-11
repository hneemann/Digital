/*
 * Copyright (c) 2018 Ivan Deras.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.boards;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.integration.ToBreakRunner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;

public class ISEProjectGeneratorTest extends TestCase  {

    public void testISEProjectExport() throws IOException, PinException, NodeException, ElementNotFoundException, HDLException {
        HDLModel m = createModel("dig/hdl/model2/clock_mimasv1.dig");
        MimasV1Board b = new MimasV1Board();
        File dir = Files.createTempDirectory("digital_verilog_" + getTime() + "_").toFile();
        File file = new File(dir, "clock_mimasv1.v");

        System.out.println(dir.getAbsolutePath());
        b.writeFiles(file, m);
        File iseProjectFile = new File(dir, "clock_mimasv1_ise" + File.separator + "clock_mimasv1.xise");
        String output = readAllFile(iseProjectFile);

        assertEquals( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n"
                    + "<project xmlns=\"http://www.xilinx.com/XMLSchema\" xmlns:xil_pn=\"http://www.xilinx.com/XMLSchema\">\n"
                    + "  <header>\n"
                    + "  </header>\n"
                    + "  <version xil_pn:ise_version=\"14.7\" xil_pn:schema_version=\"2\"/>\n"
                    + "  <files>\n"
                    + "    <file xil_pn:name=\"../clock_mimasv1.v\" xil_pn:type=\"FILE_VERILOG\">\n"
                    + "      <association xil_pn:name=\"BehavioralSimulation\" xil_pn:seqID=\"2\"/>\n"
                    + "      <association xil_pn:name=\"Implementation\" xil_pn:seqID=\"2\"/>\n"
                    + "    </file>\n"
                    + "    <file xil_pn:name=\"../clock_mimasv1_constraints.ucf\" xil_pn:type=\"FILE_UCF\">\n"
                    + "      <association xil_pn:name=\"Implementation\" xil_pn:seqID=\"0\"/>\n"
                    + "    </file>\n"
                    + "  </files>\n"
                    + "  <autoManagedFiles>\n"
                    + "  </autoManagedFiles>\n"
                    + "  <properties>\n"
                    + "    <property xil_pn:name=\"Create Binary Configuration File\" xil_pn:value=\"true\" xil_pn:valueState=\"non-default\"/>\n"
                    + "    <property xil_pn:name=\"Create Bit File\" xil_pn:value=\"true\" xil_pn:valueState=\"default\"/>\n"
                    + "    <property xil_pn:name=\"Device Family\" xil_pn:value=\"Spartan6\" xil_pn:valueState=\"non-default\"/>\n"
                    + "    <property xil_pn:name=\"Device\" xil_pn:value=\"xc6slx9\" xil_pn:valueState=\"non-default\"/>\n"
                    + "    <property xil_pn:name=\"Package\" xil_pn:value=\"tqg144\" xil_pn:valueState=\"non-default\"/>\n"
                    + "    <property xil_pn:name=\"Implementation Top File\" xil_pn:value=\"../clock_mimasv1.v\" xil_pn:valueState=\"non-default\"/>\n"
                    + "    <property xil_pn:name=\"Working Directory\" xil_pn:value=\".\" xil_pn:valueState=\"non-default\"/>\n"
                    + "  </properties>\n"
                    + "  <bindings/>\n"
                    + "  <libraries/>\n"
                    + "</project>\n", output);
    }

    private String readAllFile(File f) throws FileNotFoundException, IOException {
        BufferedReader r = new BufferedReader(new FileReader(f));
        StringBuilder sb = new StringBuilder();
        String str;

        while ((str = r.readLine()) != null) {
            sb.append(str).append("\n");
        }

        return sb.toString();
    }

    HDLModel createModel(String filePath) throws IOException, PinException, NodeException, ElementNotFoundException, HDLException {
        ToBreakRunner br = new ToBreakRunner(filePath);

        HDLModel m = new HDLModel(br.getLibrary());

        return m.create(br.getCircuit(), null);
    }

    private String getTime() {
        DateFormat f = new SimpleDateFormat("YY-MM-dd_HH-mm_ss");
        return f.format(new Date());
    }
}
