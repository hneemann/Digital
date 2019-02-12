/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;

import javax.sound.midi.MidiChannel;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The speaker.
 */
public class MIDI extends Node implements Element {

    /**
     * The Speakers description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(MIDI.class) {
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) throws NodeException {
            if (elementAttributes.get(Keys.MIDI_PROG_CHANGE))
                return new PinDescriptions(input("N"),
                        input("V"),
                        input("OnOff"),
                        input("PC"),
                        input("en"),
                        input("C").setClock()).setLangKey(getPinLangKey());
            else
                return new PinDescriptions(input("N"),
                        input("V"),
                        input("OnOff"),
                        input("en"),
                        input("C").setClock()).setLangKey(getPinLangKey());
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.MIDI_CHANNEL)
            .addAttribute(Keys.MIDI_INSTRUMENT)
            .addAttribute(Keys.MIDI_PROG_CHANGE);

    private final int chanNum;
    private final String instrument;
    private final boolean progChangeEnable;
    private ObservableValue note;
    private ObservableValue volume;
    private ObservableValue clock;
    private ObservableValue onOff;
    private ObservableValue en;
    private ObservableValue progChange;
    private MidiChannel channel;
    private boolean lastCl = false;

    /**
     * Creates a new instance
     *
     * @param attributes the elements attributes
     */
    public MIDI(ElementAttributes attributes) {
        chanNum = attributes.get(Keys.MIDI_CHANNEL) - 1;
        instrument = attributes.get(Keys.MIDI_INSTRUMENT);
        progChangeEnable = attributes.get(Keys.MIDI_PROG_CHANGE);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        note = inputs.get(0).checkBits(7, this, 0);
        volume = inputs.get(1).checkBits(7, this, 1);
        onOff = inputs.get(2).checkBits(1, this, 2);
        if (progChangeEnable) {
            progChange = inputs.get(3).checkBits(1, this, 3);
            en = inputs.get(4).checkBits(1, this, 4);
            clock = inputs.get(5).checkBits(1, this, 5).addObserverToValue(this);
        } else {
            en = inputs.get(3).checkBits(1, this, 3);
            clock = inputs.get(4).checkBits(1, this, 4).addObserverToValue(this);
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void readInputs() throws NodeException {
        boolean cl = clock.getBool();
        if (!lastCl && cl && en.getBool()) {
            int note = (int) this.note.getValue();
            if (progChange != null && progChange.getBool()) {
                channel.programChange(note);
            } else {
                if (onOff.getBool()) {
                    int v = (int) volume.getValue();
                    channel.noteOn(note, v);
                } else
                    channel.noteOff(note);
            }
        }
        lastCl = cl;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    @Override
    public void init(Model model) throws NodeException {
        channel = MIDIHelper.getInstance().getChannel(chanNum, instrument, model);
    }
}
