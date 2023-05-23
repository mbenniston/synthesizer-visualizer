package synthesizer.visualizer.Playback;

import midi.Data.Event.MidiEvent;
import midi.Data.Event.MidiEvents.MidiChannelEvent;
import midi.Data.MidiFile;
import midi.Playback.MidiEventExecutor;
import midi.Playback.MidiSequencer;
import midi.Playback.MidiTiming;

public class MidiPlayer {
    private static final int CHANNELS = 16;

    private final MidiTiming timing = new MidiTiming();
    private final MidiChannelEventRouter channelRouter = new MidiChannelEventRouter();
    private InstrumentBank instrumentBank = new InstrumentBank();

    private MidiFile file;
    private MidiSequencer sequencer;
    private MidiChannel[] channels = new MidiChannel[CHANNELS];
    private MidiChannelEventAdapter[] adapters = new MidiChannelEventAdapter[CHANNELS];

    public MidiPlayer(MidiFile file) throws CloneNotSupportedException {
        this.file = file;

        sequencer = new MidiSequencer(file, new MidiEventExecutor() {
            @Override
            public void onExecute(MidiEvent event, double time) {
                if (event instanceof MidiChannelEvent) {
                    channelRouter.onRecieve((MidiChannelEvent) event);
                }
            }
        }, timing);

        for (int i = 0; i < CHANNELS; i++) {
            MidiChannel channel = new MidiChannel();
            channel.instrument = instrumentBank.defaultInstrument.clone();
            channel.timingView = timing.view;

            channels[i] = channel;
            adapters[i] = new MidiChannelEventAdapter(channel, instrumentBank);
            channelRouter.adapters[i] = adapters[i];
        }
    }

    public void setInstrumentBank(InstrumentBank bank) {
        for (int i = 0; i < CHANNELS; i++) {
            adapters[i].setInstrumentBank(bank);
        }
        instrumentBank = bank;
    }

    public InstrumentBank getInstrumentBank() {
        return instrumentBank;
    }

    public void seek(double seekTime) {
        sequencer.seek(seekTime);
    }

    public void update(double currentTime) {
        sequencer.step(currentTime);
        updateChannels();
    }

    public double sample(double currentTime) {
        double amplitude = 0.0;
        for (MidiChannel channel : channels) {
            amplitude += channel.instrument.sample(currentTime);
        }
        return amplitude;
    }

    private void updateChannels() {
        final double currentTime = timing.getCurrentTime();

        for (MidiChannel channel : channels) {
            channel.instrument.update(currentTime);
        }
    }

    private void resetChannels() {
        for (MidiChannel channel : channels) {
            try {
                channel.instrument = instrumentBank.defaultInstrument.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        for (int i = 0; i < CHANNELS; i++) {
            adapters[i].reset();
        }
    }

    public void setFile(MidiFile file) {
        this.file = file;

        sequencer.setFile(file);
        resetChannels();
    }

    public boolean isPlaying() {
        return !sequencer.isFinished();
    }

    public MidiFile getFile() {
        return file;
    }

    public MidiChannel[] getChannels() {
        return channels;
    }

    public MidiTiming.MidiTimingView getTimingView() {
        return timing.view;
    }
}
