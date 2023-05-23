package synthesizer.visualizer.Playback;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONException;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.common.util.concurrent.AtomicDoubleArray;

import midi.Data.MidiFile;
import synthesizer.Playback.AudioPlayer;
import synthesizer.WaveForms.Instrument;
import synthesizer.WaveForms.StandardVoices;
import synthesizer.visualizer.Utils.Constants;

public class AudioPlayerState {
    public final AtomicDouble playerThreadTime = new AtomicDouble();

    public final AtomicReference<MidiFile> midiFile = new AtomicReference<>();
    public final AtomicDouble midiTime = new AtomicDouble();
    public final AtomicDouble midiPlaybackSpeed = new AtomicDouble(1.0);
    public final AtomicDouble[] midiChannelVolumes = new AtomicDouble[Constants.NUM_MIDI_CHANNELS];
    public final AtomicDouble midiOverallVolume = new AtomicDouble(1.0);

    public final AtomicReference<Instrument> playerInstrument = new AtomicReference<>();
    public final AtomicBoolean[] playerNoteStates = new AtomicBoolean[Constants.NUM_MIDI_NOTES];
    public final AtomicBoolean playerRunning = new AtomicBoolean(true);
    public final AtomicReference<InstrumentBank> midiInstrumentBank = new AtomicReference<>();

    public final SampleMonitor[] midiChannelMonitors = new SampleMonitor[Constants.NUM_MIDI_CHANNELS];

    public AudioPlayerState() {
        try {
            playerInstrument.set(new Instrument(StandardVoices.createDefaultVoice2()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 128; i++) {
            playerNoteStates[i] = new AtomicBoolean();
        }

        for (int i = 0; i < 16; i++) {
            midiChannelVolumes[i] = new AtomicDouble(1.0);
            midiChannelMonitors[i] = new SampleMonitor(Constants.NUM_CHANNEL_MONITOR_SAMPLES);
        }
    }
}