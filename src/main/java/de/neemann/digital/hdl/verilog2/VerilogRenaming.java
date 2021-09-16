/*
 * Copyright (c) 2018 Helmut Neemann, Ivan Deras.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2;

import de.neemann.digital.hdl.model2.HDLModel;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Renames the labels to valid Verilog names.
 */
public class VerilogRenaming implements HDLModel.Renaming {

    private static final HashSet<String> KEYWORDS = new HashSet<>(Arrays.asList("always",
            "and", "assign", "automatic", "begin", "buf", "bufif0", "bufif1", "case", "casex",
            "casez", "cell", "cmos", "config", "deassign", "default", "defparam", "design",
            "disable", "edge", "else", "end", "endcase", "endconfig", "endfunction", "endgenerate",
            "endmodule", "endprimitive", "endspecify", "endtable", "endtask", "event", "for",
            "force", "forever", "fork", "function", "generate", "genvar", "highz0", "highz1",
            "if", "ifnone", "incdir", "include", "initial", "inout", "input", "instance", "integer",
            "join", "large", "liblist", "library", "localparam", "macromodule", "medium", "module",
            "nand", "negedge", "nmos", "nor", "noshowcancelledno", "not", "notif0", "notif1",
            "or", "output", "parameter", "pmos", "posedge", "primitive", "pull0", "pull1",
            "pulldown", "pullup", "pulsestyle_oneventglitch", "pulsestyle_ondetectglitch",
            "remos", "real", "realtime", "reg", "release", "repeat", "rnmos", "rpmos", "rtran",
            "rtranif0", "rtranif1", "scalared", "showcancelled", "signed", "small", "specify",
            "specparam", "strong0", "strong1", "supply0", "supply1", "table", "task", "time",
            "tran", "tranif0", "tranif1", "tri", "tri0", "tri1", "triand", "trior", "trireg",
            "unsigned", "use", "vectored", "wait", "wand", "weak0", "weak1", "while", "wire",
            "wor", "xnor", "xor"));

    @Override
    public String checkName(String name) {
        if (isKeyword(name) || !isFirstCharValid(name))
            // Escaped identifier, the space is part of the identifier.
            return "\\" + replaceWhitespace(name) + " ";
        else
            return cleanName(name);
    }

    private String replaceWhitespace(String name) {
        return name
                .replace(' ', '_')
                .replace('\t', '_');
    }

    private boolean isFirstCharValid(String name) {
        char c = name.charAt(0);

        return ((c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c == '_'));
    }

    private boolean isKeyword(String str) {
        return KEYWORDS.contains(str.toLowerCase());
    }

    private String cleanName(String name) {
        StringBuilder sb = new StringBuilder();
        boolean needScaping = false;

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || (c == '_') || (c == '$'))
                sb.append(c);
            else {
                switch (c) {
                    case '\\':
                        break;
                    case '\t':
                    case ' ':
                        sb.append("_");
                        break;
                    case '/':
                    case '!':
                    case '~':
                    case '\u00AC':
                        sb.append("not");
                        break;
                    default:
                        sb.append(c);
                        needScaping = true;
                }
            }
        }

        if (needScaping) {
            sb.insert(0, "\\"); // Escaped identifier
            sb.append(" "); // The space is part of an escaped identifier
        }

        return sb.toString();
    }


}
