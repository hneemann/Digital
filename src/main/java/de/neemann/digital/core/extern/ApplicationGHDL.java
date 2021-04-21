/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.handler.ProcessInterface;
import de.neemann.digital.core.extern.handler.StdIOInterface;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Abstraction of the ghdl Application.
 * See https://github.com/ghdl/ghdl
 */
public class ApplicationGHDL extends ApplicationVHDLStdIO {

    private final ElementAttributes attr;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public ApplicationGHDL(ElementAttributes attr) {
        this.attr = attr;
    }

    @Override
    public ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws IOException {
        File file = null;
        try {
            String ghdl = getGhdlPath().getPath();

            file = createVHDLFile(label, code, inputs, outputs, root);
            ProcessStarter.start(file.getParentFile(), new Options()
                    .add(ghdl)
                    .add("-a")
                    .add(attr, Keys.GHDL_OPTIONS)
                    .add(file.getName())
                    .getArray());
            ProcessStarter.start(file.getParentFile(), new Options()
                    .add(ghdl)
                    .add("-e")
                    .add(attr, Keys.GHDL_OPTIONS)
                    .add("stdIOInterface")
                    .getArray());
            ProcessBuilder pb = new ProcessBuilder(new Options()
                    .add(ghdl)
                    .add("-r")
                    .add(attr, Keys.GHDL_OPTIONS)
                    .add("stdIOInterface")
                    .add("--unbuffered")
                    .getList()).redirectErrorStream(true).directory(file.getParentFile());
            return new GHDLProcessInterface(pb.start(), file.getParentFile());
        } catch (IOException e) {
            if (file != null)
                ProcessStarter.removeFolder(file.getParentFile());
            if (ghdlNotFound(e))
                throw new IOException(Lang.get("err_ghdlNotInstalled"));
            else
                throw e;
        }
    }

    private boolean ghdlNotFound(Throwable e) {
        while (e != null) {
            if (e instanceof ProcessStarter.CouldNotStartProcessException)
                return true;
            e = e.getCause();
        }
        return false;
    }

    @Override
    public boolean checkSupported() {
        return true;
    }

    @Override
    public String checkCode(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) throws IOException {
        File file = null;
        try {
            String ghdl = getGhdlPath().getPath();

            file = createVHDLFile(label, code, inputs, outputs, root);
            String m1 = ProcessStarter.start(file.getParentFile(), new Options()
                    .add(ghdl)
                    .add("-a")
                    .add(attr, Keys.GHDL_OPTIONS)
                    .add(file.getName())
                    .getArray());
            String m2 = ProcessStarter.start(file.getParentFile(), new Options()
                    .add(ghdl)
                    .add("-e")
                    .add(attr, Keys.GHDL_OPTIONS)
                    .add("stdIOInterface")
                    .getArray());
            return ProcessStarter.joinStrings(m1, m2);
        } catch (IOException e) {
            if (ghdlNotFound(e))
                throw new IOException(Lang.get("err_ghdlNotInstalled"));
            else
                throw e;
        } finally {
            if (file != null)
                ProcessStarter.removeFolder(file.getParentFile());
        }
    }

    private static File getGhdlPath() {
        return Settings.getInstance().get(Keys.SETTINGS_GHDL_PATH);
    }

    private static final class GHDLProcessInterface extends StdIOInterface {
        private final File folder;

        private GHDLProcessInterface(Process process, File folder) {
            super(process);
            this.folder = folder;
        }

        @Override
        public String getConsoleOutNoWarn(LinkedList<String> consoleOut) {
            StringBuilder sb = new StringBuilder();
            for (String s : consoleOut) {
                if (!s.contains("(assertion warning)"))
                    sb.append(s).append("\n");
            }
            return sb.toString();
        }

        @Override
        public void close() throws IOException {
            super.close();
            ProcessStarter.removeFolder(folder);
        }
    }
}
