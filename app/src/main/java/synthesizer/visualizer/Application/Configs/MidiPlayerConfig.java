package synthesizer.visualizer.Application.Configs;

import com.google.common.util.concurrent.AtomicDouble;
import synthesizer.visualizer.Playback.InstrumentBank;

import java.util.concurrent.atomic.AtomicReference;

public class MidiPlayerConfig {
    public final MidiChannelConfig[] channels;
    public final AtomicDouble playbackSpeed;
    public final AtomicDouble currentMidiTime;
    public final AtomicDouble midiVolume;
    public final AtomicReference<InstrumentBank> instrumentBank;

    public MidiPlayerConfig(MidiChannelConfig[] channels,
            AtomicDouble playbackSpeed,
            AtomicDouble currentMidiTime,
            AtomicDouble midiVolume,
            AtomicReference<InstrumentBank> instrumentBank) {
        this.channels = channels;
        this.playbackSpeed = playbackSpeed;
        this.currentMidiTime = currentMidiTime;
        this.midiVolume = midiVolume;
        this.instrumentBank = instrumentBank;
    }
}
