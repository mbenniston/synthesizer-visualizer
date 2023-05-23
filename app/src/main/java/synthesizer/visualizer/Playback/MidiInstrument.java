package synthesizer.visualizer.Playback;

import synthesizer.Config.InstrumentReader.LoadedInstrument;
import synthesizer.WaveForms.Instrument;

public class MidiInstrument {
    public final Instrument instrument;
    public final int programStartID, programEndID;

    public MidiInstrument(Instrument instrument, int programStartID, int programEndID) {
        this.instrument = instrument;
        this.programStartID = programStartID;
        this.programEndID = programEndID;
    }

    public MidiInstrument(LoadedInstrument loadedInstrument) {
        this.instrument = loadedInstrument.instrument();
        this.programStartID = loadedInstrument.programStartID();
        this.programEndID = loadedInstrument.programEndID();
    }
}
