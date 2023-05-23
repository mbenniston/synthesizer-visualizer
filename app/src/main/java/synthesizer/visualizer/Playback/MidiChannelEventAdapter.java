package synthesizer.visualizer.Playback;

import midi.Data.Event.Callbacks.MidiChannelEventVisitor;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceNoteOff;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceNoteOn;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceProgramChange;
import midi.Writing.MidiChannelEventWriter;

public class MidiChannelEventAdapter extends MidiChannelEventVisitor.DefaultMessageVisitor {
    private final MidiChannel channel;
    private InstrumentBank instrumentBank;
    private int currentProgramID = 0;

    public MidiChannelEventAdapter(MidiChannel channel, InstrumentBank instrumentBank) {
        this.channel = channel;
        this.instrumentBank = instrumentBank;
    }

    @Override
    public void visit(MidiVoiceNoteOff message) {
        channel.instrument.StopNote(
                message.noteId,
                channel.timingView.getCurrentTime());
    }

    @Override
    public void visit(MidiVoiceNoteOn message) {
        if (message.noteVelocity == 0) {
            channel.instrument.StopNote(
                    message.noteId,
                    channel.timingView.getCurrentTime());

        } else {
            channel.instrument.PlayNote(
                    message.noteId,
                    channel.timingView.getCurrentTime());
        }
    }

    @Override
    public void visit(MidiVoiceProgramChange event) {
        try {
            currentProgramID = event.programId;
            channel.instrument = instrumentBank.getInstrument(event.programId).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void setInstrumentBank(InstrumentBank bank) {
        instrumentBank = bank;
        try {
            channel.instrument = instrumentBank.getInstrument(currentProgramID).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        currentProgramID = 0;
        try {
            channel.instrument = instrumentBank.getInstrument(currentProgramID).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
