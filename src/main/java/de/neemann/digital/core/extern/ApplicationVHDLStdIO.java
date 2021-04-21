/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Parser;
import de.neemann.digital.hdl.hgs.Statement;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import static de.neemann.digital.core.extern.VHDLTokenizer.Token.*;

/**
 * Base class of applications which are able to interpret VHDL-Code.
 * The generated vhdl code is able to operate with the {@link de.neemann.digital.core.extern.handler.StdIOInterface}.
 */
public abstract class ApplicationVHDLStdIO implements Application {
    private static final Statement TEMPLATE =
            Parser.createFromJarStatic("vhdl/VHDLStdIOTemplate.tem");

    /**
     * Creates a vhdl file in a temp directory.
     *
     * @param label   the name of the vhdl code
     * @param code    the vhdl code
     * @param inputs  the inputs
     * @param outputs the outputs
     * @param root    the projects main folder
     * @return the vhdl file
     * @throws IOException IOException
     */
    public File createVHDLFile(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws IOException {
        File dir = Files.createTempDirectory("digital_vhdl_").toFile();

        File file = new File(dir, label + ".vhdl");

        try (Writer w = new FileWriter(file)) {
            w.write(createVHDL(label, code, inputs, outputs, root));
        } catch (HGSEvalException e) {
            throw new IOException("error evaluating the template", e);
        }

        return file;
    }

    /**
     * Creates vhdl code
     *
     * @param label   the name of the vhdl code
     * @param code    the vhdl code
     * @param inputs  the inputs
     * @param outputs the outputs
     * @param root    the projects main folder
     * @return the vhdl code
     * @throws HGSEvalException HGSEvalException
     */
    public String createVHDL(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws HGSEvalException {
        Context context = new Context(root)
                .declareVar("entityName", label)
                .declareVar("code", code)
                .declareVar("inputs", inputs)
                .declareVar("outputs", outputs);

        TEMPLATE.execute(context);
        return context.toString();
    }

    @Override
    public boolean ensureConsistency(ElementAttributes attributes, File rootPath) {
        try {
            String code = Application.getCode(attributes, rootPath);
            VHDLTokenizer st = new VHDLTokenizer(new StringReader(code));
            while (!st.value().equalsIgnoreCase("entity"))
                st.next();

            String label = st.consumeIdent();

            st.consumeIdent("is");
            st.consumeIdent("port");
            st.consume(OPEN);

            PortDefinition in = new PortDefinition("");
            PortDefinition out = new PortDefinition("");
            while (true) {
                scanPorts(st, in, out);
                if (st.peek() != SEMICOLON)
                    break;
                st.consume(SEMICOLON);
            }
            st.consume(CLOSE);

            if (in.size() > 0 && out.size() > 0) {
                attributes.set(Keys.LABEL, label);
                attributes.set(Keys.EXTERNAL_INPUTS, in.toString());
                attributes.set(Keys.EXTERNAL_OUTPUTS, out.toString());
                return true;
            } else
                return false;

        } catch (NoSuchElementException | ParseException | VHDLTokenizer.TokenizerException | IOException e) {
            return false;
        }
    }

    private void scanPorts(VHDLTokenizer st, PortDefinition in, PortDefinition out) throws ParseException, IOException, VHDLTokenizer.TokenizerException {
        ArrayList<String> vars = new ArrayList<>();
        vars.add(st.consumeIdent());
        while (true) {
            switch (st.next()) {
                case COLON:
                    switch (st.consumeIdent().toLowerCase()) {
                        case "in":
                            scanPort(st, vars, in);
                            break;
                        case "out":
                            scanPort(st, vars, out);
                            break;
                        default:
                            throw new ParseException("unexpected token " + st);
                    }
                    return;
                case COMMA:
                    vars.add(st.consumeIdent());
                    break;
                default:
                    throw new ParseException("unexpected token " + st);
            }
        }
    }

    private void scanPort(VHDLTokenizer st, ArrayList<String> vars, PortDefinition port) throws ParseException, IOException, VHDLTokenizer.TokenizerException {
        switch (st.consumeIdent().toLowerCase()) {
            case "std_logic":
                for (String var : vars)
                    port.addPort(var, 1);
                break;
            case "std_logic_vector":
                st.consume(OPEN);
                int upper = st.consumeNumber();
                st.consumeIdent("downto");
                int lower = st.consumeNumber();
                st.consume(CLOSE);

                if (lower != 0)
                    throw new ParseException("lower is not zero");

                for (String var : vars)
                    port.addPort(var, upper + 1);
                break;
            default:
                throw new ParseException("unexpected token " + st);
        }
    }

    private static final class ParseException extends Exception {
        private ParseException(String message) {
            super(message);
        }
    }

}
