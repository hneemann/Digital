/*
 * Copyright (c) 2024 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.FileLocator;
import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEventType;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.RealTimeClock;
import de.neemann.digital.gui.ProgramMemoryLoader;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Is used to run a circuit headless
 */
public class Runner extends BasicCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);
    private final Argument<String> digFile;

    /**
     * Creates the run command
     */
    public Runner() {
        super("run");
        digFile = addArgument(new Argument<>("dig", "", false));
    }

    @Override
    protected void execute() throws CLIException {
        try {
            final CircuitLoader circuitLoader = new CircuitLoader(digFile.get(), false);
            final Circuit circuit = circuitLoader.getCircuit();

            long time = System.currentTimeMillis();
            ModelCreator modelCreator = new ModelCreator(circuit, circuitLoader.getLibrary());
            Model model = modelCreator.createModel(true);

            time = System.currentTimeMillis() - time;
            LOGGER.debug("model creation: " + time + " ms, " + model.getNodes().size() + " nodes");

            ArrayList<Clock> clocks = model.getClocks();
            if (clocks.size() == 0)
                throw new CLIException(Lang.get("cli_run_noClock"), null);

            ScheduledThreadPoolExecutor timerExecutor = new ScheduledThreadPoolExecutor(1);

            int threadRunnerCount = 0;
            boolean realTimeClockRunning = false;
            for (Clock c : clocks) {
                int frequency = c.getFrequency();
                if (frequency > 0) {
                    final RealTimeClock realTimeClock = new RealTimeClock(model, c, timerExecutor, null);
                    if (realTimeClock.isThreadRunner()) threadRunnerCount++;
                    realTimeClockRunning = true;
                }
            }
            if (threadRunnerCount > 1)
                throw new CLIException(Lang.get("err_moreThanOneFastClock"), null);

            if (!realTimeClockRunning) {
                throw new CLIException(Lang.get("cli_run_noClock"), null);
            }

            ElementAttributes settings = circuit.getAttributes();
            if (settings.get(Keys.PRELOAD_PROGRAM)) {
                File romHex = new FileLocator(settings.get(Keys.PROGRAM_TO_PRELOAD))
                        .setBaseFile(new File(digFile.get()))
                        .locate();
                new ProgramMemoryLoader(romHex, settings.get(Keys.BIG_ENDIAN_SETTING))
                        .preInit(model);
            }

            model.addObserver(event -> {
                if (event.getType() == ModelEventType.POSTCLOSED) {
                    timerExecutor.shutdownNow();
                }
            }, ModelEventType.POSTCLOSED);

            model.init();
        } catch (Exception e) {
            throw new CLIException(Lang.get("cli_errorRunningCircuit"), e);
        }
    }
}
