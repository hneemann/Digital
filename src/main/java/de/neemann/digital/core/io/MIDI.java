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
            = new ElementTypeDescription(MIDI.class,
            input("N"),
            input("V"),
            input("OnOff"),
            input("C").setClock())
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIDICHANNEL)
            .addAttribute(Keys.MIDIINSTRUMENT);

    private final int chanNum;
    private final String instrument;
    private ObservableValue note;
    private ObservableValue volume;
    private ObservableValue clock;
    private ObservableValue onOff;
    private MidiChannel channel;
    private boolean lastCl = false;

    /**
     * Creates a new instance
     *
     * @param attributes the elements attributes
     */
    public MIDI(ElementAttributes attributes) {
        chanNum = attributes.get(Keys.MIDICHANNEL);
        instrument = attributes.get(Keys.MIDIINSTRUMENT);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        note = inputs.get(0).checkBits(7, this, 0);
        volume = inputs.get(1).checkBits(7, this, 1);
        onOff = inputs.get(2).checkBits(1, this, 2);
        clock = inputs.get(3).checkBits(1, this, 3).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void readInputs() throws NodeException {
        boolean cl = clock.getBool();
        if (!lastCl && cl) {
            int note = (int) this.note.getValue();
            if (onOff.getBool()) {
                int v = (int) volume.getValue();
                channel.noteOn(note, v);
            } else
                channel.noteOff(note);
        }
        lastCl = cl;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    @Override
    public void init(Model model) throws NodeException {
        MIDIHelper.getInstance().open(model);
        channel = MIDIHelper.getInstance().getChannel(chanNum, instrument);
    }
}
