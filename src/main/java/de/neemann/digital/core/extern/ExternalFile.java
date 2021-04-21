/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.*;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;

/**
 * The external component
 */
public class ExternalFile extends External {

    /**
     * The external component description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(ExternalFile.class) {
        @Override
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) {
            return new PortDefinition(elementAttributes.get(Keys.EXTERNAL_INPUTS)).getPinDescriptions(PinDescription.Direction.input);
        }

        @Override
        public PinDescriptions getOutputDescriptions(ElementAttributes elementAttributes) {
            return new PortDefinition(elementAttributes.get(Keys.EXTERNAL_OUTPUTS)).getPinDescriptions(PinDescription.Direction.output);
        }
    }
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.WIDTH)
            .addAttribute(Keys.EXTERNAL_INPUTS)
            .addAttribute(Keys.EXTERNAL_OUTPUTS)
            .addAttribute(Keys.EXTERNAL_CODE_FILE)
            .addAttribute(Keys.APPLICATION_TYPE)
            .addAttribute(Keys.GHDL_OPTIONS)
            .addAttribute(Keys.IVERILOG_OPTIONS)
            .supportsHDL();

    private final ElementAttributes attr;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public ExternalFile(ElementAttributes attr) {
        super(attr);
        this.attr = attr;
    }

    @Override
    public void init(Model model) throws NodeException {
        File file = attr.getFile(Keys.EXTERNAL_CODE_FILE, model.getRootPath());
        try {
            setCode(Application.readCode(file));
        } catch (IOException e) {
            throw new NodeException(Lang.get("err_errorLoadingHDLFile_N", file));
        }
        super.init(model);
    }
}
