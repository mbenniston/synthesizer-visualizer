package synthesizer.visualizer.Application.Configs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONException;

import synthesizer.Config.InstrumentReader;
import synthesizer.Config.InstrumentReader.LoadedInstrument;
import synthesizer.WaveForms.Instrument;
import synthesizer.visualizer.Playback.InstrumentBank;
import synthesizer.visualizer.Playback.MidiInstrument;

// users keyboard settings
public class PlayerConfig {
    public final AtomicReference<Instrument> currentInstrument;
    public final InstrumentBank instrumentBank;

    private int currentProgram = 0;
    private String lastInstrumentBankPath = null;

    public PlayerConfig(AtomicReference<Instrument> currentInstrument) {
        this.currentInstrument = currentInstrument;

        instrumentBank = new InstrumentBank();
    }

    public void loadInstrumentBank(String path) {
        try {
            var instruments = InstrumentReader.load(new FileInputStream(path));
            instrumentBank.setToDefaultInstrument();
            for (LoadedInstrument instrument : instruments.values()) {
                instrumentBank.setInstrument(new MidiInstrument(instrument));
            }

            reloadInstrument();

            lastInstrumentBankPath = path;
        } catch (JSONException | IOException | UnsupportedAudioFileException | CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void loadInstrumentBank(InputStream stream) {
        try {
            var instruments = InstrumentReader.load(stream);
            instrumentBank.setToDefaultInstrument();
            for (LoadedInstrument instrument : instruments.values()) {
                instrumentBank.setInstrument(new MidiInstrument(instrument));
            }

            reloadInstrument();

            lastInstrumentBankPath = null;
        } catch (JSONException | IOException | UnsupportedAudioFileException | CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void reloadInstrumentBank() {
        if (lastInstrumentBankPath != null) {
            loadInstrumentBank(lastInstrumentBankPath);
        }
    }

    public int getCurrentProgram() {
        return currentProgram;
    }

    public void setCurrentProgram(int i) {
        try {
            currentInstrument.set(instrumentBank.getInstrument(i).clone());
            currentProgram = i;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void reloadInstrument() {
        try {
            currentInstrument.set(instrumentBank.getInstrument(currentProgram).clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
