package synthesizer.visualizer.Application.Panels;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import synthesizer.visualizer.Application.Panel;
import synthesizer.visualizer.Application.Configs.PlayerConfig;
import synthesizer.visualizer.Utils.GeneralMidiInstrumentNames;

public class PlayerControlPanel implements Panel {
    private final PlayerConfig config;

    public PlayerControlPanel(PlayerConfig config) {
        this.config = config;
    }

    @Override
    public void draw() {
        if (ImGui.begin("User Instrument Control", new ImBoolean(true),
                ImGuiWindowFlags.HorizontalScrollbar)) {

            ImGui.text("Instrument settings");

            if (ImGui.button("Pick instrument bank")) {
                String path = TinyFileDialogs.tinyfd_openFileDialog(
                        "Open midi", null, null, null, false);

                if (path != null) {
                    config.loadInstrumentBank(path);
                }
            }

            if (ImGui.button("Reload instrument bank")) {
                config.reloadInstrumentBank();
            }

            if (ImGui.button("Reset instrument bank")) {
                config.instrumentBank.setToDefaultInstrument();
            }

            final String[] programNames = GeneralMidiInstrumentNames.NAMES;
            int currentProgram = config.getCurrentProgram();

            if (ImGui.beginCombo("Program", programNames[currentProgram])) {

                for (int i = 0; i < programNames.length; i++) {
                    boolean isSelected = i == currentProgram;

                    if (ImGui.selectable(programNames[i], isSelected)) {
                        config.setCurrentProgram(i);
                        currentProgram = config.getCurrentProgram();
                    }

                    if (isSelected) {
                        ImGui.setItemDefaultFocus();
                    }
                }

                ImGui.endCombo();
            }

        }
        ImGui.end();
    }
}
