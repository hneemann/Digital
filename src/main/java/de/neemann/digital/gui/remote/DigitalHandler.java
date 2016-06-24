package de.neemann.digital.gui.remote;

import de.neemann.digital.gui.DigitalRemoteInterface;
import de.neemann.digital.lang.Lang;

import java.io.File;

/**
 * Handler to control the simulator
 */
public class DigitalHandler implements HandlerInterface {
    private final DigitalRemoteInterface digitalRemoteInterface;

    /**
     * Creates a new server instance
     *
     * @param digitalRemoteInterface the remote interface which is used by the server
     */
    public DigitalHandler(DigitalRemoteInterface digitalRemoteInterface) {
        this.digitalRemoteInterface = digitalRemoteInterface;
    }

    @Override
    public String handleRequest(String request) {
        int p = request.indexOf(':');
        String command = request;
        String args = null;
        if (p >= 0) {
            command = request.substring(0, p);
            args = request.substring(p + 1);
        }

        return handle(command.toLowerCase(), args);
    }

    private String handle(String command, String args) {
        switch (command) {
            case "step":
                digitalRemoteInterface.doSingleStep();
                break;
            case "start":
                if (!digitalRemoteInterface.start())
                    return Lang.get("msg_errorCreatingModel");
                break;
            case "run":
                digitalRemoteInterface.runToBreak();
                break;
            case "stop":
                digitalRemoteInterface.stop();
                break;
            case "load":
                File file = new File(args);
                if (file.exists()) {
                    if (!digitalRemoteInterface.loadRom(file))
                        return Lang.get("msg_noRomFound");
                } else
                    return Lang.get("msg_errorFileNotFound", args);
                break;
            default:
                return Lang.get("msg_remoteUnknownCommand", command);
        }
        return "ok";
    }
}
