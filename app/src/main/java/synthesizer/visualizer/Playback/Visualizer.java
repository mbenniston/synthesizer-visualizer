package synthesizer.visualizer.Playback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import midi.Data.MidiFile;
import midi.Data.MidiTrack;
import midi.Data.Event.MidiEvent;
import midi.Data.Event.MidiEvents.MidiChannelEvent;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceNoteOn;
import midi.Data.Event.MidiEvents.MidiChannelEvents.MidiVoiceProgramChange;
import midi.Playback.MidiEventExecutor;
import midi.Playback.MidiSequencer;
import midi.Playback.MidiTiming;
import midi.Reading.MidiFileReader;
import midi.Reading.MidiFileReader.MidiLoadError;
import synthesizer.visualizer.Application.Configs.MidiChannelConfig;
import synthesizer.visualizer.Application.Configs.MidiPlayerConfig;
import synthesizer.visualizer.Application.Configs.PlayerConfig;
import synthesizer.visualizer.Playback.SampleMonitor.SampleIterator;
import synthesizer.visualizer.Utils.Constants;
import synthesizer.visualizer.Utils.GeneralMidiInstrumentNames;
import synthesizer.visualizer.Utils.MidiFileUtils;

public class Visualizer {
    private AudioThread audioThread;
    private final AudioPlayerState sharedState = new AudioPlayerState();

    private final MidiTiming timing = new MidiTiming();
    private final MidiSequencer sequencer;
    private double midiFileLengthInSeconds = 0;

    private final NoteHistory[] playerNoteHistories = new NoteHistory[Constants.NUM_MIDI_NOTES];
    private final ChannelHistory[] channelHistories = new ChannelHistory[Constants.NUM_MIDI_CHANNELS];

    private final MidiChannelConfig[] channels = new MidiChannelConfig[Constants.NUM_MIDI_CHANNELS];

    private final MidiPlayerConfig midiPlayerConfig;

    private final PlayerConfig playerConfig;
    private double currentMidiTime;
    private double currentPlayerTime;

    public Visualizer() {
        playerConfig = new PlayerConfig(sharedState.playerInstrument);
        midiPlayerConfig = new MidiPlayerConfig(channels, sharedState.midiPlaybackSpeed, sharedState.midiTime,
                sharedState.midiOverallVolume, sharedState.midiInstrumentBank);

        final String path = "midis/moon.mid";
        MidiFile file = null;
        try {
            file = MidiFileReader
                    .load(Visualizer.class.getClassLoader().getResourceAsStream(path));
        } catch (MidiLoadError e1) {
            throw new RuntimeException("Could not open default midi file");
        }

        timing.setTicksPerBeat(file.header.divisions);

        for (int i = 0; i < Constants.NUM_MIDI_CHANNELS; i++) {
            channelHistories[i] = new ChannelHistory();
            channels[i] = new MidiChannelConfig(i, sharedState.midiChannelVolumes[i]);
        }

        setChannelsEnabled(file);

        sequencer = new MidiSequencer(file, new MidiEventExecutor() {
            @Override
            public void onExecute(MidiEvent event, double currentTime) {

                if (event instanceof MidiVoiceProgramChange) {
                    MidiVoiceProgramChange e = ((MidiVoiceProgramChange) event);
                    channels[e.channel].name = GeneralMidiInstrumentNames.getNameFromProgramID(e.programId);
                }

                if (event instanceof MidiChannelEvent) {
                    MidiChannelEvent channelEvent = (MidiChannelEvent) event;
                    ChannelHistory history = channelHistories[channelEvent.channel];
                    history.setExecuteTime(currentTime);
                    history.onRecieve(channelEvent);
                }
            }
        }, timing);

        for (int i = 0; i < Constants.NUM_MIDI_NOTES; i++) {
            playerNoteHistories[i] = new NoteHistory();
        }

        audioThread = new AudioThread(sharedState);
        audioThread.start();

        playerConfig.loadInstrumentBank(Visualizer.class.getClassLoader().getResourceAsStream("midi-instruments.json"));
    }

    public void update() {
        currentPlayerTime = sharedState.playerThreadTime.get();
        currentMidiTime = sharedState.midiTime.get();

        sequencer.step(currentMidiTime);

        synchronized (sharedState.midiChannelMonitors) {
            SampleIterator[] iterators = new SampleIterator[16];
            for (int c = 0; c < Constants.NUM_MIDI_CHANNELS; c++) {
                iterators[c] = (SampleIterator) sharedState.midiChannelMonitors[c].iterator();
            }

            for (int i = 0; i < Constants.NUM_CHANNEL_MONITOR_SAMPLES; i++) {
                for (int c = 0; c < Constants.NUM_MIDI_CHANNELS; c++) {
                    channels[c].sampleValues[i] = (float) (double) iterators[c].next();
                }
            }
        }
    }

    public void updatePlayerNote(int index, boolean isDown) {
        boolean wasDown = sharedState.playerNoteStates[index].get();
        if (isDown && !wasDown) {
            playerNoteHistories[index].setNoteDown(currentPlayerTime);
        } else if (!isDown && wasDown) {
            playerNoteHistories[index].setNoteUp(currentPlayerTime);
        }

        sharedState.playerNoteStates[index].set(isDown);
    }

    public void setChannelsEnabled(MidiFile file) {
        for (MidiChannelConfig config : channels) {
            config.enabled = false;
        }

        for (MidiTrack track : file.tracks) {
            for (MidiEvent event : track.events) {
                if (event instanceof MidiVoiceNoteOn) {
                    channels[((MidiChannelEvent) event).channel].enabled = true;
                }
            }
        }
    }

    public void loadMidi(String path) throws FileNotFoundException, MidiLoadError {
        MidiFile loadedFile = MidiFileReader.load(new FileInputStream(path));
        sequencer.setFile(loadedFile);
        sharedState.midiFile.set(loadedFile);

        setChannelsEnabled(loadedFile);
        clearHistories();
        try {
            midiFileLengthInSeconds = MidiFileUtils.getMidiFileLengthInSeconds(loadedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearHistories() {
        for (int i = 0; i < 16; i++) {
            channelHistories[i].clear();
        }
    }

    public void seek(double seekTo) {
        clearHistories();

        sharedState.midiTime.set(seekTo);
        sequencer.seek(seekTo, true);
        sequencer.step(seekTo);

    }

    public void stop() {
        sharedState.playerRunning.set(false);
        try {
            audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public double getMidiTime() {
        return currentMidiTime;
    }

    public double getPlayerTime() {
        return currentPlayerTime;
    }

    public double getMidiFileLengthInSeconds() {
        return midiFileLengthInSeconds;
    }

    public MidiPlayerConfig getMidiPlayerConfig() {
        return midiPlayerConfig;
    }

    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public NoteHistory[] getPlayerNoteHistories() {
        return playerNoteHistories;
    }

    public ChannelHistory[] getChannelHistories() {
        return channelHistories;
    }

    public MidiChannelConfig[] getChannels() {
        return channels;
    }
}
