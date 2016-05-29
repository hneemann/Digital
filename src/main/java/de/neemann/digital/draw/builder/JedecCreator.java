package de.neemann.digital.draw.builder;

import de.neemann.digital.draw.builder.Gal16v8.BuilderCollector;
import de.neemann.digital.draw.builder.jedec.FuseMapFillerException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface used to create Jedec files.
 * Every supported device implements this interface.
 *
 * @author hneemann
 */
public interface JedecCreator<T extends JedecCreator> {

    /**
     * @return builder to add expressions
     */
    BuilderCollector getBuilder();

    /**
     * Sets a pin number for a signal.
     * If no pin is set a suited pin is chosen automatically
     *
     * @param name the signals name
     * @param pin  the pin to use
     * @return this for chained calls
     * @throws FuseMapFillerException FuseMapFillerException
     */
    T assignPin(String name, int pin) throws FuseMapFillerException;

    /**
     * Writes the JEDEC file to the given output stream
     *
     * @param out the output stream
     * @throws FuseMapFillerException FuseMapFillerException
     * @throws IOException            IOException
     */
    void writeTo(OutputStream out) throws FuseMapFillerException, IOException;
}
