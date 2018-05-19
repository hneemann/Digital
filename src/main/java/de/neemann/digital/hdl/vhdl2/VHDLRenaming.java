/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.hdl.model2.HDLModel;

import java.util.Collections;
import java.util.HashSet;

/**
 * Renames the labels to valid VHDL names.
 */
public class VHDLRenaming implements HDLModel.Renaming {

    private static final HashSet<String> KEYWORDS = new HashSet<>();

    static {
        Collections.addAll(KEYWORDS,
                "abs", "access", "after", "alias", "all", "and", "architecture", "array", "assert",
                "attribute", "begin", "block", "body", "buffer", "bus", "case", "component", "configuration",
                "constant", "disconnect", "downto", "else", "elsif", "end", "entity", "exit", "file",
                "for", "function", "generate", "generic", "group", "guarded", "if", "impure", "in",
                "inertial", "inout", "is", "label", "library", "linkage", "literal", "loop",
                "map", "mod", "nand", "new", "next", "nor", "not", "null", "of",
                "on", "open", "or", "others", "out", "package", "port", "postponed", "procedure",
                "process", "pure", "range", "record", "register", "reject", "rem", "report", "return",
                "rol", "ror", "select", "severity", "signal", "shared", "sla", "sll", "sra",
                "srl", "subtype", "then", "to", "transport", "type", "unaffected", "units", "until",
                "use", "variable", "wait", "when", "while", "with", "xnor", "xor");
    }


    @Override
    public String checkName(String name) {
        if (isKeyword(name))
            return "p_" + name;
        else {
            if (Character.isDigit(name.charAt(0)))
                name = "n" + name;
            return cleanName(name);
        }
    }

    private boolean isKeyword(String str) {
        return KEYWORDS.contains(str.toLowerCase());
    }

    private String cleanName(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))
                sb.append(c);
            else {
                switch (c) {
                    case '/':
                    case '!':
                    case '~':
                    case '\u00AC':
                        sb.append("not");
                        break;
                    case '=':
                        sb.append("eq");
                        break;
                    case '<':
                        sb.append("le");
                        break;
                    case '>':
                        sb.append("gr");
                        break;
                    default:
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '_')
                            sb.append("_");
                }
            }
        }

        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '_')
            sb.setLength(sb.length() - 1);

        return sb.toString();
    }


}
