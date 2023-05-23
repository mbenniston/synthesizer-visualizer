package synthesizer.visualizer.Application.Panels;

import org.json.JSONException;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import synthesizer.Config.InstrumentReader;
import synthesizer.visualizer.Application.Panel;
import synthesizer.visualizer.Application.Configs.MidiPlayerConfig;
import synthesizer.visualizer.Playback.InstrumentBank;
import synthesizer.visualizer.Playback.MidiInstrument;
import synthesizer.visualizer.Utils.Format;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileInputStream;
import java.io.IOException;

public class MidiControlPanel implements Panel {
    private final PickMidiHandler pickHandler;
    private final ScrubHandler scrubHandler;
    private final MidiChannelPanel[] channelPanels = new MidiChannelPanel[16];
    private double fileLengthInSeconds;
    private final MidiPlayerConfig config;
    private String lastInstrumentBankPath = null;

    public MidiControlPanel(
            MidiPlayerConfig config,
            PickMidiHandler pickHandler, ScrubHandler scrubHandler) {
        this.config = config;
        this.scrubHandler = scrubHandler;
        this.pickHandler = pickHandler;

        for (int i = 0; i < 16; i++) {
            channelPanels[i] = new MidiChannelPanel(config.channels[i]);
        }
    }

    @Override
    public void draw() {
        if (ImGui.begin("Midi Control", new ImBoolean(true),
                ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.HorizontalScrollbar)) {
            drawMenuBar();
            drawPlaybackSettings();
            drawVolumeSettings();
            drawChannelSettings();
            drawInstrumentSettings();
        }
        ImGui.end();

        drawChannelPanels();
    }

    private void drawPlaybackSettings() {
        ImGui.text("Playback");

        double midiTime = Math.min(config.currentMidiTime.get(), fileLengthInSeconds);

        ImGui.text(Format.formatDuration(midiTime) + "/" + Format.formatDuration(fileLengthInSeconds));

        ImFloat playbackTime = new ImFloat((float) midiTime);
        if (ImGui.sliderFloat("Time", playbackTime.getData(), 0, (float) fileLengthInSeconds)) {
            scrubHandler.onSeek(playbackTime.get());
        }

        if (config.playbackSpeed.get() == 0.0) {
            if (ImGui.button("Unpause")) {
                config.playbackSpeed.set(1);
            }

        } else {

            if (ImGui.button("Pause")) {
                config.playbackSpeed.set(0);
            }
        }

        ImFloat speed = new ImFloat((float) config.playbackSpeed.get());
        if (ImGui.sliderFloat("Playback speed", speed.getData(), 0, 4)) {
            config.playbackSpeed.set(speed.get());
        }

        if (ImGui.button("Reset speed")) {
            config.playbackSpeed.set(1);
        }
    }

    private void drawMenuBar() {
        ImGui.beginMenuBar();

        if (ImGui.beginMenu("File")) {

            if (ImGui.menuItem("Open")) {
                String path = TinyFileDialogs.tinyfd_openFileDialog(
                        "Open midi", null, null, null, false);

                if (path != null) {
                    pickHandler.onPick(path);
                }
            }

            ImGui.endMenu();
        }

        ImGui.endMenuBar();
    }

    private void drawVolumeSettings() {
        ImGui.separator();
        ImGui.text("Volume");

        float[] totalVolume = new float[] { (float) config.midiVolume.get() };
        ImGui.sliderFloat("Master Volume", totalVolume, 0, 2);
        config.midiVolume.set(totalVolume[0]);

        if (ImGui.button("Reset volume")) {
            config.midiVolume.set(1);
        }
    }

    private void drawChannelSettings() {
        ImGui.separator();
        ImGui.text("Channels");

        for (int i = 0; i < 16; i++) {
            if (!config.channels[i].enabled)
                continue;

            ImGui.pushID(i);

            if (channelPanels[i].isOpen.get()) {
                if (ImGui.button("Close Channel Panel")) {
                    channelPanels[i].close();
                }
            } else {
                if (ImGui.button("Configure Channel " + i)) {
                    channelPanels[i].open();
                }
            }
            ImGui.popID();
        }

        if (ImGui.button("Close all channel windows")) {
            for (MidiChannelPanel panel : channelPanels) {
                panel.close();
            }
        }
    }

    private void drawInstrumentSettings() {
        ImGui.separator();
        ImGui.text("Instruments");

        if (ImGui.button("Pick instrument bank")) {
            String path = TinyFileDialogs.tinyfd_openFileDialog(
                    "Open midi", null, null, null, false);

            if (path != null) {
                loadInstrumentBank(path);
            }
        }

        if (ImGui.button("Reload instrument bank")) {
            reloadInstrumentBank();
        }

        if (ImGui.button("Reset instrument bank")) {
            setToDefaultInstrumentBank();
        }
    }

    private void setToDefaultInstrumentBank() {
        config.instrumentBank.set(new InstrumentBank());
        lastInstrumentBankPath = null;

    }

    private void reloadInstrumentBank() {
        if (lastInstrumentBankPath != null) {
            loadInstrumentBank(lastInstrumentBankPath);
        }
    }

    private void drawChannelPanels() {
        for (int i = 0; i < 16; i++) {
            if (config.channels[i].enabled) {
                channelPanels[i].draw();
            }
        }
    }

    public void loadInstrumentBank(String path) {
        try {
            InstrumentBank instrumentBank = new InstrumentBank();
            var instruments = InstrumentReader.load(new FileInputStream(path));
            instrumentBank.setToDefaultInstrument();
            for (InstrumentReader.LoadedInstrument instrument : instruments.values()) {
                instrumentBank.setInstrument(new MidiInstrument(instrument));
            }

            config.instrumentBank.set(instrumentBank);
            lastInstrumentBankPath = path;
        } catch (JSONException | IOException | UnsupportedAudioFileException | CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void setFileLengthInSeconds(double length) {
        fileLengthInSeconds = length;
    }

    public interface PickMidiHandler {
        void onPick(String path);
    }

    public interface ScrubHandler {
        void onSeek(double seekTo);
    }
}
