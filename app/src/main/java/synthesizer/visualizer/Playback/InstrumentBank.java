package synthesizer.visualizer.Playback;

import synthesizer.WaveForms.Instrument;
import synthesizer.WaveForms.StandardVoices;

public class InstrumentBank {
    public final Instrument defaultInstrument;
    private final Instrument[] instrumentPrograms = new Instrument[128];

    public InstrumentBank() {
        defaultInstrument = new Instrument(StandardVoices.createDefaultVoice2());
        setToDefaultInstrument();
    }

    public void setToDefaultInstrument() {
        for (int i = 0; i < 128; i++) {
            instrumentPrograms[i] = defaultInstrument;
        }
    }

    public void setInstrument(MidiInstrument instrument) {
        for (int i = instrument.programStartID; i <= instrument.programEndID; i++) {
            instrumentPrograms[i] = instrument.instrument;
        }
    }

    public Instrument getInstrument(int programID) {
        return instrumentPrograms[programID];
    }

}
