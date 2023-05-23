package synthesizer.visualizer.Playback;

import midi.Data.Event.Callbacks.MidiChannelEventListener;
import midi.Data.Event.Callbacks.MidiChannelEventVisitor;
import midi.Data.Event.MidiEvents.MidiChannelEvent;
import midi.Data.Event.MidiEvents.MidiChannelEvents.*;

public class MidiChannelEventRouter extends MidiChannelEventVisitor {
    public MidiChannelEventListener[] adapters = new MidiChannelEventListener[16];

    @Override
    public void visit(MidiChannelEvent message) {
        super.visit(message);
    }

    @Override
    public void visit(MidiVoiceNoteOff message) {
        adapters[message.channel].onRecieve(message);
    }

    @Override
    public void visit(MidiVoiceNoteOn message) {
        adapters[message.channel].onRecieve(message);
    }

    @Override
    public void visit(MidiVoiceAfterTouch message) {
        adapters[message.channel].onRecieve(message);

    }

    @Override
    public void visit(MidiVoiceControlChange message) {
        adapters[message.channel].onRecieve(message);
    }

    @Override
    public void visit(MidiVoiceProgramChange message) {
        adapters[message.channel].onRecieve(message);
    }

    @Override
    public void visit(MidiVoiceChannelPressure message) {
        adapters[message.channel].onRecieve(message);
    }

    @Override
    public void visit(MidiVoicePitchBend message) {
        adapters[message.channel].onRecieve(message);
    }
}
