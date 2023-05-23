package synthesizer.visualizer.Utils;

import java.io.IOException;

import midi.Data.MidiFile;
import midi.Data.Event.MidiEvent;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceNoteOff;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceNoteOn;
import midi.Playback.MidiEventExecutor;
import midi.Playback.MidiSequencer;
import midi.Playback.MidiTiming;

public class MidiFileUtils {
    public static double getMidiFileLengthInSeconds(MidiFile file) throws IOException {
        MidiTiming timing = new MidiTiming();
        timing.setTicksPerBeat(file.header.divisions);
        final double[] lastExecutionTime = new double[1];
        lastExecutionTime[0] = 0;

        MidiSequencer sequencer = new MidiSequencer(file, new MidiEventExecutor() {

            @Override
            public void onExecute(MidiEvent event, double executeTime) {
                if (event instanceof MidiVoiceNoteOn || event instanceof MidiVoiceNoteOff) {
                    lastExecutionTime[0] = executeTime;
                }
            }
        }, timing);

        while (!sequencer.isFinished()) {
            sequencer.step(timing.getCurrentTime() + 10000000);
        }

        return lastExecutionTime[0];
    }
}
