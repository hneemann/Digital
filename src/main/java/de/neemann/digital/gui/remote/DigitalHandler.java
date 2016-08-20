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

        try {
            handle(command.toLowerCase(), args);
            return "ok";
        } catch (RemoteException e) {
            return e.getMessage();
        }
    }

    private void handle(String command, String args) throws RemoteException {
        switch (command) {
            case "step":
                digitalRemoteInterface.doSingleStep();
                break;
            case "start":
                digitalRemoteInterface.start();
                break;
            case "debug":
                digitalRemoteInterface.debug();
                break;
            case "run":
                digitalRemoteInterface.runToBreak();
                break;
            case "stop":
                digitalRemoteInterface.stop();
                break;
            case "load":
                File file = new File(args);
                digitalRemoteInterface.loadRom(file);
                break;
            default:
                throw new RemoteException(Lang.get("msg_remoteUnknownCommand", command));
        }
    }
}
