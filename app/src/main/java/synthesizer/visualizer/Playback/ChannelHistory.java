package synthesizer.visualizer.Playback;

import midi.Data.Event.Callbacks.MidiChannelEventVisitor;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceNoteOff;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceNoteOn;

public class ChannelHistory extends MidiChannelEventVisitor.DefaultMessageVisitor {
    public static final int NUM_NOTES = 128;

    private final NoteHistory[] noteHistories = new NoteHistory[NUM_NOTES];
    private double executeTime;

    public ChannelHistory() {
        for (int i = 0; i < NUM_NOTES; i++) {
            noteHistories[i] = new NoteHistory();
        }
    }

    @Override
    public void visit(MidiVoiceNoteOff event) {
        noteHistories[event.noteId].setNoteUp(executeTime);
    }

    @Override
    public void visit(MidiVoiceNoteOn event) {
        if (event.noteVelocity != 0) {
            noteHistories[event.noteId].setNoteDown(executeTime);
        } else {
            noteHistories[event.noteId].setNoteUp(executeTime);
        }
    }

    public void clear() {
        for (int i = 0; i < NUM_NOTES; i++) {
            noteHistories[i].clear();
        }
        executeTime = 0.0f;
    }

    public void setExecuteTime(double executeTime) {
        this.executeTime = executeTime;
    }

    public NoteHistory[] getNoteHistories() {
        return noteHistories;
    }
}
