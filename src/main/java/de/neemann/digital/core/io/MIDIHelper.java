/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ModelEventType;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Helper for MIDI functions
 */
public final class MIDIHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(MIDIHelper.class);
    private static MIDIHelper ourInstance = new MIDIHelper();

    /**
     * @return the MIDIHelper
     */
    public static MIDIHelper getInstance() {
        return ourInstance;
    }

    private SynthesizerInterface synthesizer;
    private boolean isOpen;
    private TreeMap<String, Instrument> instrumentMap;

    private MIDIHelper() {
    }

    private SynthesizerInterface getSynthesizer() throws NodeException {
        if (synthesizer == null) {
            try {
                final Synthesizer synth = MidiSystem.getSynthesizer();
                if (synth == null)
                    throw new NodeException(Lang.get("err_midiSystemNotAvailable"));
                synthesizer = new RealSynthesizer(synth);
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
    private void ensureOpen(Model model) throws NodeException {
        if (!isOpen) {
            try {
                getSynthesizer().open();
            } catch (MidiUnavailableException e) {
                if (System.getProperty("testdata") == null) {
                    throw new NodeException(Lang.get("err_midiSystemNotAvailable"), e);
                } else {
                    LOGGER.info("Use fake MIDI interface!");
                    synthesizer = new SynthesizerMock();
                }
            }

            isOpen = true;

            model.addObserver(event -> {
                if (event.equals(ModelEventType.CLOSED))
                    close();
            }, ModelEventType.CLOSED);
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
     * @param model      the model
     * @return the channel
     * @throws NodeException NodeException
     */
    public MidiChannel getChannel(int num, String instrument, Model model) throws NodeException {
        ensureOpen(model);

        Instrument instr = null;
        if (!instrument.isEmpty())
            instr = getInstrument(instrument);

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
            instrumentMap.put("", null);
            for (Instrument i : getSynthesizer().getAvailableInstruments()) {
                instrumentMap.put(i.getName(), i);
            }
        }
        return instrumentMap;
    }

    private interface SynthesizerInterface {
        void open() throws MidiUnavailableException;

        void close();

        Instrument[] getAvailableInstruments();

        MidiChannel[] getChannels();
    }

    private static final class RealSynthesizer implements SynthesizerInterface {
        private final Synthesizer synthesizer;

        private RealSynthesizer(Synthesizer synthesizer) {
            this.synthesizer = synthesizer;
        }

        @Override
        public void open() throws MidiUnavailableException {
            synthesizer.open();

            Soundbank soundbank = synthesizer.getDefaultSoundbank();
            if (soundbank == null)
                throw new MidiUnavailableException(Lang.get("err_midiInstrumentsNotAvailable"));

            if (!synthesizer.loadAllInstruments(soundbank))
                throw new MidiUnavailableException(Lang.get("err_midiInstrumentsNotAvailable"));
        }

        @Override
        public void close() {
            synthesizer.close();
        }

        @Override
        public Instrument[] getAvailableInstruments() {
            return synthesizer.getAvailableInstruments();
        }

        @Override
        public MidiChannel[] getChannels() {
            return synthesizer.getChannels();
        }
    }

    private static final class SynthesizerMock implements SynthesizerInterface {
        @Override
        public void open() {
        }

        @Override
        public void close() {
        }

        @Override
        public Instrument[] getAvailableInstruments() {
            return new Instrument[0];
        }

        @Override
        public MidiChannel[] getChannels() {
            final MidiChannel dummy = new MidiChannel() {
                @Override
                public void noteOn(int i, int i1) {
                }

                @Override
                public void noteOff(int i, int i1) {
                }

                @Override
                public void noteOff(int i) {
                }

                @Override
                public void setPolyPressure(int i, int i1) {
                }

                @Override
                public int getPolyPressure(int i) {
                    return 0;
                }

                @Override
                public void setChannelPressure(int i) {

                }

                @Override
                public int getChannelPressure() {
                    return 0;
                }

                @Override
                public void controlChange(int i, int i1) {

                }

                @Override
                public int getController(int i) {
                    return 0;
                }

                @Override
                public void programChange(int i) {

                }

                @Override
                public void programChange(int i, int i1) {

                }

                @Override
                public int getProgram() {
                    return 0;
                }

                @Override
                public void setPitchBend(int i) {

                }

                @Override
                public int getPitchBend() {
                    return 0;
                }

                @Override
                public void resetAllControllers() {

                }

                @Override
                public void allNotesOff() {

                }

                @Override
                public void allSoundOff() {

                }

                @Override
                public boolean localControl(boolean b) {
                    return false;
                }

                @Override
                public void setMono(boolean b) {

                }

                @Override
                public boolean getMono() {
                    return false;
                }

                @Override
                public void setOmni(boolean b) {

                }

                @Override
                public boolean getOmni() {
                    return false;
                }

                @Override
                public void setMute(boolean b) {

                }

                @Override
                public boolean getMute() {
                    return false;
                }

                @Override
                public void setSolo(boolean b) {

                }

                @Override
                public boolean getSolo() {
                    return false;
                }
            };
            return new MidiChannel[]{
                    dummy, dummy, dummy, dummy,
                    dummy, dummy, dummy, dummy,
                    dummy, dummy, dummy, dummy,
                    dummy, dummy, dummy, dummy};
        }
    }

}
