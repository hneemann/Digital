/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.VerilogTokenizer.Token;
import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.Parser;
import de.neemann.digital.hdl.hgs.Statement;

import java.io.*;
import java.nio.file.Files;
import java.util.NoSuchElementException;


/**
 * Base class of applications which are able to interpret Verilog-Code.
 * The generated verilog code is able to operate with the {@link de.neemann.digital.core.extern.handler.StdIOInterface}.
 */
public abstract class ApplicationVerilogStdIO implements Application {
    private Token currToken;

    private static final Statement TEMPLATE =
            Parser.createFromJarStatic("verilog/VerilogStdIOTemplate.vtpl");

    /**
     * Creates a verilog file in a temp directory.
     *
     * @param label   the name of the verilog code
     * @param code    the verilog code
     * @param inputs  the inputs
     * @param outputs the outputs
     * @param root    the projects main folder
     * @return the verilog file
     * @throws IOException IOException
     */
    public File createVerilogFile(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws IOException {
        File dir = Files.createTempDirectory("digital_verilog_").toFile();

        File file = new File(dir, label + ".v");

        try (Writer w = new FileWriter(file)) {
            w.write(createVerilog(label, code, inputs, outputs, root));
        } catch (HGSEvalException e) {
            throw new IOException("error evaluating the template", e);
        }

        return file;
    }

    /**
     * Creates the verilog code
     *
     * @param label   the name of the verilog module
     * @param code    the verilog code
     * @param inputs  the inputs
     * @param outputs the outputs
     * @param root    the projects main folder
     * @return the verilog code
     * @throws HGSEvalException HGSEvalException
     */
    public String createVerilog(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws HGSEvalException {
        Context context = new Context(root)
                .declareVar("moduleName", label)
                .declareVar("code", code)
                .declareVar("inputs", inputs)
                .declareVar("outputs", outputs);

        TEMPLATE.execute(context);
        return context.toString();
    }

    private void match(Token tkExpect, String tkText, VerilogTokenizer st) throws ParseException, IOException, VerilogTokenizer.TokenizerException {
        if (currToken != tkExpect) {
            throw new ParseException("unexpected '" + tkText + "'");
        }
        currToken = st.nextToken();
    }

    @Override
    public boolean ensureConsistency(ElementAttributes attributes, File root) {
        try {
            String code = Application.getCode(attributes, root);
            VerilogTokenizer st = new VerilogTokenizer(new StringReader(code));

            PortDefinition in;
            PortDefinition out;
            String label;

            currToken = st.nextToken();

            match(Token.MODULE, "keyword 'module'", st);
            label = st.value();
            match(Token.IDENT, "identifier", st);
            match(Token.OPENPAR, "'('", st);

            in = new PortDefinition("");
            out = new PortDefinition("");
            scanPortArgs(st, in, out);

            if (currToken == Token.SEMICOLON) {
                if (in.size() == 0 && out.size() == 0) {
                    do {
                        currToken = st.nextToken();
                        if (currToken == Token.INPUT || currToken == Token.OUTPUT)
                            scanPort(st, in, out);
                    } while ((currToken != Token.ENDMODULE) && (currToken != Token.EOF));
                }
            } else {
                return false;
            }

            if (in.size() > 0 && out.size() > 0) {
                attributes.set(Keys.LABEL, label);
                attributes.set(Keys.EXTERNAL_INPUTS, in.toString());
                attributes.set(Keys.EXTERNAL_OUTPUTS, out.toString());
                return true;
            } else
                return false;

        } catch (NoSuchElementException | ParseException | VerilogTokenizer.TokenizerException | IOException e) {
            return false;
        }
    }

    private void scanPortArgs(VerilogTokenizer st, PortDefinition in, PortDefinition out) throws ParseException, IOException, VerilogTokenizer.TokenizerException {
        while (true) {
            switch (currToken) {
                case IDENT:
                    currToken = st.nextToken();
                    break;
                case INPUT:
                case OUTPUT:
                    scanPort(st, in, out);
                    break;
                case CLOSEPAR:
                    currToken = st.nextToken();
                    return;
                case COMMA:
                    currToken = st.nextToken();
                    break;
                default:
                    throw new ParseException("unexpected '" + st.value() + "'");
            }
        }
    }

    private void scanPort(VerilogTokenizer st, PortDefinition in, PortDefinition out) throws ParseException, IOException, VerilogTokenizer.TokenizerException {
        boolean isInput;

        switch (currToken) {
            case INPUT:
                isInput = true;
                currToken = st.nextToken();
                if (currToken == Token.WIRE) {
                    currToken = st.nextToken();
                }
                break;
            case OUTPUT:
                isInput = false;
                currToken = st.nextToken();
                if (currToken == Token.WIRE
                        || currToken == Token.REG) {
                    currToken = st.nextToken();
                }
                break;
            default:
                throw new ParseException("unexpected '" + st.value() + "'");
        }

        int bits = 1;
        if (currToken == Token.OPENBRACKET) {
            match(Token.OPENBRACKET, "", st);
            String rangeStart = st.value();
            match(Token.NUMBER, "a number", st);
            match(Token.COLON, "':'", st);
            String rangeEnd = st.value();
            match(Token.NUMBER, "a number", st);
            match(Token.CLOSEBRACKET, "']'", st);
            bits = (Integer.parseInt(rangeStart) - Integer.parseInt(rangeEnd)) + 1;
        }
        String name = st.value();
        match(Token.IDENT, "identifier", st);

        if (isInput) {
            in.addPort(name, bits);
        } else {
            out.addPort(name, bits);
        }

        while (currToken == Token.COMMA) {
            match(Token.COMMA, "comma", st);
            if (currToken != Token.IDENT)
                return;
            name = st.value();
            match(Token.IDENT, "identifier", st);

            if (isInput) {
                in.addPort(name, bits);
            } else {
                out.addPort(name, bits);
            }
        }
    }

    private static final class ParseException extends Exception {
        private ParseException(String message) {
            super(message);
        }
    }

}
