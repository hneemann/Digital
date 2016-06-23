package de.neemann.digital.gui.remote;

import de.neemann.digital.gui.DigitalRemoteInterface;

import java.io.File;

/**
 * Handler to control the simulator
 */
public class DigitalHandler implements HandlerInterface {
    private final DigitalRemoteInterface digitalRemoteInterface;

    /**
     * Creates a new server instance
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
                digitalRemoteInterface.start();
                break;
            case "run":
                digitalRemoteInterface.runToBreak();
                break;
            case "load":
                File file = new File(args);
                if (file.exists())
                    digitalRemoteInterface.loadRom(file);
                else
                    return "file not found";
                break;
            default:
                return "unknown command: "+command;
        }
        return "ok";
    }
}
