package de.neemann.digital.builder.ATF1502;

import de.neemann.digital.builder.ExpressionToFileExporter;
import de.neemann.digital.gui.Main;

import java.io.*;

/**
 * Created by hneemann on 10.03.17.
 */
public class CreateCHN implements ExpressionToFileExporter.PostProcess {
    @Override
    public File execute(File file) throws IOException {
        File chnFile = Main.checkSuffix(file, "chn");
        System.out.println("create chn from " + file);

        try (Writer chn = new OutputStreamWriter(new FileOutputStream(chnFile), "UTF-8")) {
            chn.write("1 4 1 0 \r\n"
                    + "\r\n"
                    + "ATF1502AS\r\n"
                    + "10\r\n"
                    + "1\r\n");
            chn.write(chnFile.getPath());
        }

        return chnFile;
    }
}
