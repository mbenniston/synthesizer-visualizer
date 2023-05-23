package synthesizer.visualizer.Playback;

import midi.Data.MidiFile;
import synthesizer.Playback.AudioPlayer;
import synthesizer.WaveForms.Instrument;
import synthesizer.WaveForms.StandardVoices;
import synthesizer.visualizer.Utils.Constants;

public class AudioThread extends Thread {
    private final AudioPlayerState sharedState;

    private Instrument playerInstrument;
    private MidiPlayer midiPlayer;

    private double midiPlaybackSpeed = 1;
    private double lastSampleTime = 0;
    private double lastSentMidiTime;
    private double midiVolume = 1.0;
    private final boolean[] previousNoteStates = new boolean[Constants.NUM_MIDI_NOTES];

    private final SampleMonitor[] channelMonitors = new SampleMonitor[Constants.NUM_MIDI_CHANNELS];

    public AudioThread(AudioPlayerState sharedState) {
        this.sharedState = sharedState;
        playerInstrument = new Instrument(StandardVoices.createDefaultVoice2());

        for (int i = 0; i < Constants.NUM_MIDI_CHANNELS; i++) {
            channelMonitors[i] = new SampleMonitor(Constants.NUM_CHANNEL_MONITOR_SAMPLES);
        }
    }

    @Override
    public void run() {

        AudioPlayer.play(new AudioPlayer.SampleProvider() {

            @Override
            public boolean isPlaying() {
                return sharedState.playerRunning.get();
            }

            @Override
            public void onBlockStart(double startTime, long startSample) {
                syncState(startTime);
            }

            @Override
            public double nextSample(double time, long sample) {
                return onSample(time);
            }
        });
    }

    private void syncState(double startTime) {
        syncInstrument(startTime);
        syncMidiPlayer(startTime);
        syncChannelMonitors();
    }

    private void syncChannelMonitors() {
        synchronized (sharedState.midiChannelMonitors) {
            for (int i = 0; i < Constants.NUM_MIDI_CHANNELS; i++) {
                channelMonitors[i].copyTo(sharedState.midiChannelMonitors[i]);
            }
        }
    }

    private void syncInstrument(double startTime) {
        playerInstrument = sharedState.playerInstrument.get();

        for (int i = 0; i < Constants.NUM_MIDI_NOTES; i++) {
            boolean noteWasDown = previousNoteStates[i];
            boolean noteIsDown = sharedState.playerNoteStates[i].get();

            if (noteIsDown && !noteWasDown) {
                playerInstrument.PlayNote(i, startTime);
            } else if (!noteIsDown && noteWasDown) {
                playerInstrument.StopNote(i, startTime);
            }

            previousNoteStates[i] = noteIsDown;
        }
        sharedState.playerThreadTime.set(startTime);
    }

    private void syncMidiPlayer(double startTime) {
        checkForUpdatedMidiFile();
        updatePlayer();

        midiPlaybackSpeed = sharedState.midiPlaybackSpeed.get();
        midiVolume = sharedState.midiOverallVolume.get();
    }

    private void updatePlayer() {
        if (midiPlayer != null) {
            double lastMidiTime = sharedState.midiTime.get();

            if (lastMidiTime != lastSentMidiTime) {
                midiPlayer.seek(lastMidiTime);
            }

            double midiTime = midiPlayer.getTimingView().getCurrentTime();
            sharedState.midiTime.set(midiTime);
            lastSentMidiTime = midiTime;

            final MidiChannel[] channels = midiPlayer.getChannels();

            for (int i = 0; i < Constants.NUM_MIDI_CHANNELS; i++) {
                channels[i].instrument.setVolumeScale(sharedState.midiChannelVolumes[i].get());
            }
        }
    }

    private void checkForUpdatedMidiFile() {
        MidiFile file = sharedState.midiFile.get();

        if (midiPlayer == null && file != null) {
            // first file load
            try {
                midiPlayer = new MidiPlayer(file);
                sharedState.midiTime.set(0);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                sharedState.midiFile.set(null);
            }
        }

        if (midiPlayer != null) {
            // updated file
            if (midiPlayer.getFile() != file) {
                midiPlayer.setFile(file);
                sharedState.midiTime.set(0);
            }

            // updated instrument bank
            InstrumentBank nextBank = sharedState.midiInstrumentBank.get();

            if (nextBank != null && midiPlayer.getInstrumentBank() != nextBank) {
                midiPlayer.setInstrumentBank(nextBank);
            }
        }
    }

    private double onSample(double time) {
        double outputAmplitude = 0;

        double deltaTime = time - lastSampleTime;
        lastSampleTime = time;

        if (playerInstrument != null) {
            playerInstrument.update(time);
            outputAmplitude += playerInstrument.sample(time);
        }

        if (midiPlayer != null) {
            final double midiSampleTime = midiPlayer.getTimingView().getCurrentTime() + deltaTime * midiPlaybackSpeed;
            midiPlayer.update(midiSampleTime);

            final MidiChannel[] channels = midiPlayer.getChannels();

            for (int i = 0; i < Constants.NUM_MIDI_CHANNELS; i++) {
                final double sample = channels[i].instrument.sample(midiSampleTime);
                channelMonitors[i].pushSample(sample);
                outputAmplitude += sample * midiVolume;
            }
        }

        // clipping
        if (outputAmplitude > 1) {
            outputAmplitude = 1;
        }
        if (outputAmplitude < -1) {
            outputAmplitude = -1;
        }

        return outputAmplitude;
    }
}