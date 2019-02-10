/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The speaker.
 */
public class Speaker extends Node implements Element {

    /**
     * The Speakers description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Speaker.class, input("N"), input("V"), input("en")) {}
            .addAttribute(Keys.MIDICHANNEL)
            .addAttribute(Keys.ROTATE);

    private final int chanNum;
    private ObservableValue note;
    private ObservableValue volume;
    private ObservableValue enable;
    private MidiChannel channel;
    private boolean lastEn = false;
    private int notePlaying;

    /**
     * Creates a new instance
     *
     * @param attributes the elements attributes
     */
    public Speaker(ElementAttributes attributes) {
        chanNum = attributes.get(Keys.MIDICHANNEL);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        note = inputs.get(0).checkBits(7, this, 0);
        volume = inputs.get(1).checkBits(7, this, 1);
        enable = inputs.get(2).checkBits(1, this, 2).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void readInputs() throws NodeException {
        boolean en = enable.getBool();
        if (channel != null) {
            if (!lastEn && en) {
                notePlaying = (int) note.getValue();
                int v = (int) volume.getValue();
                channel.noteOn(notePlaying, v);
            } else if (lastEn && !en) {
                channel.noteOff(notePlaying);
            }
        }
        lastEn = en;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    @Override
    public void init(Model model) throws NodeException {
        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel[] channels = synth.getChannels();
            if (chanNum >= channels.length)
                channel = channels[0];
            else
                channel = channels[chanNum];

            model.addObserver(event -> {
                if (event.equals(ModelEvent.STOPPED))
                    synth.close();
            }, ModelEvent.STOPPED);
        } catch (MidiUnavailableException e) {
            throw new NodeException(Lang.get("err_midiSystemNotAvailable"), e);
        }
    }
}
