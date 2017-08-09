package de.neemann.digital.hdl.printer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Pretty printer for code witch handles indentation and writes to a buffer
 */
public class CodePrinterStr extends CodePrinter {

    /**
     * Creates a new instance
     */
    public CodePrinterStr() {
        this(2);
    }

    /**
     * Creates a new instance with the given indentation
     *
     * @param identWidth the indentation
     */
    public CodePrinterStr(int identWidth) {
        super(new ByteArrayOutputStream(), identWidth);
    }

    @Override
    public String toString() {
        try {
            close();
        } catch (IOException e) {
            // can not happen
        }
        return out.toString();
    }
}
