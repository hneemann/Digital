/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.lang.Lang;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Helper for MIDI functions
 */
public final class MIDIHelper {
    private static MIDIHelper ourInstance = new MIDIHelper();

    /**
     * @return the MIDIHelper
     */
    public static MIDIHelper getInstance() {
        return ourInstance;
    }

    private Synthesizer synthesizer;
    private boolean isOpen;
    private TreeMap<String, Instrument> instrumentMap;

    private MIDIHelper() {
    }

    private Synthesizer getSynthesizer() throws NodeException {
        if (synthesizer == null) {
            try {
                synthesizer = MidiSystem.getSynthesizer();
                if (synthesizer == null)
                    throw new NodeException(Lang.get("err_midiSystemNotAvailable"));
            } catch (MidiUnavailableException e) {
                throw new NodeException(Lang.get("err_midiSystemNotAvailable"), e);
            }
        }
        return synthesizer;
    }

    /**
     * Opens the synthesizer
     *
     * @param model the mode used. If the model is closed also the synthesizer is closed
     * @throws NodeException NodeException
     */
    public void open(Model model) throws NodeException {
        if (!isOpen) {
            try {
                getSynthesizer().open();
            } catch (MidiUnavailableException e) {
                throw new NodeException(Lang.get("err_midiSystemNotAvailable"), e);
            }
            isOpen = true;

            model.addObserver(event -> {
                if (event.equals(ModelEvent.STOPPED))
                    close();
            }, ModelEvent.STOPPED);
        }
    }

    private void close() {
        if (isOpen) {
            synthesizer.close();
            isOpen = false;
        }
    }

    /**
     * Creates the channel to use
     *
     * @param num        the channel number
     * @param instrument the instrument to use
     * @return the channel
     * @throws NodeException NodeException
     */
    public MidiChannel getChannel(int num, String instrument) throws NodeException {
        Instrument instr = null;
        if (!instrument.isEmpty()) {
            instr = getInstrument(instrument);

            if (!getSynthesizer().loadInstrument(instr))
                throw new NodeException(Lang.get("err_midiInstrument_N_NotAvailable", instrument));
        }

        MidiChannel[] channels = getSynthesizer().getChannels();
        if (num >= channels.length) {
            close();
            throw new NodeException(Lang.get("err_midiChannel_N_NotAvailable", num));
        }

        MidiChannel channel = channels[num];
        if (channel == null) {
            close();
            throw new NodeException(Lang.get("err_midiChannel_N_NotAvailable", num));
        }

        if (instr != null) {
            final Patch patch = instr.getPatch();
            channel.programChange(patch.getBank(), patch.getProgram());
        }

        return channel;
    }

    /**
     * @return the list of available instruments
     * @throws NodeException NodeException
     */
    public String[] getInstruments() throws NodeException {
        return new ArrayList<>(getInstumentMap().keySet()).toArray(new String[0]);
    }

    private Instrument getInstrument(String instrument) throws NodeException {
        Instrument i = getInstumentMap().get(instrument);
        if (i == null)
            throw new NodeException(Lang.get("err_midiInstrument_N_NotAvailable", instrument));
        return i;
    }

    private TreeMap<String, Instrument> getInstumentMap() throws NodeException {
        if (instrumentMap == null) {
            instrumentMap = new TreeMap<>();
            for (Instrument i : getSynthesizer().getAvailableInstruments()) {
                instrumentMap.put(i.getName(), i);
            }
        }
        return instrumentMap;
    }

}
