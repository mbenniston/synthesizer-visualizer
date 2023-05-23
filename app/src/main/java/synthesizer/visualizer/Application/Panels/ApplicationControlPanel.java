package synthesizer.visualizer.Application.Panels;

import java.io.FileInputStream;
import java.io.IOException;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import synthesizer.visualizer.Application.Panel;
import synthesizer.visualizer.Application.Configs.ApplicationConfig;
import synthesizer.visualizer.Graphics.Texture;

public class ApplicationControlPanel implements Panel {
    private final ApplicationConfig config;
    private final WelcomePanel welcomePanel;

    public ApplicationControlPanel(ApplicationConfig config, WelcomePanel welcomePanel) {
        this.config = config;
        this.welcomePanel = welcomePanel;
    }

    @Override
    public void draw() {
        if (ImGui.begin("Application Control", new ImBoolean(true),
                ImGuiWindowFlags.HorizontalScrollbar)) {

            drawBackgroundSettings();
            drawNoteSettings();
            drawMiscSettings();
        }
        ImGui.end();
    }

    private void drawBackgroundSettings() {
        ImGui.text("Background settings");

        float[] color = new float[] { config.backgroundColor.x, config.backgroundColor.y,
                config.backgroundColor.z };

        if (ImGui.colorEdit3("Background color", color)) {
            config.backgroundColor.x = color[0];
            config.backgroundColor.y = color[1];
            config.backgroundColor.z = color[2];
        }

        if (ImGui.button("Pick background file")) {
            String path = TinyFileDialogs.tinyfd_openFileDialog(
                    "Open midi", null, null, null, false);

            if (path != null) {
                try {
                    config.backgroundTexture = new Texture(new FileInputStream(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (ImGui.button("Reset background texture")) {
            if (config.backgroundTexture != null) {
                config.backgroundTexture.release();
            }
            config.backgroundTexture = null;
        }
    }

    private void drawNoteSettings() {
        ImGui.separator();
        ImGui.text("Note settings");

        float[] noteHeightScale = new float[] { config.noteHeightScale };
        ImGui.sliderFloat("Note Height Scale", noteHeightScale, 0, 1);
        config.noteHeightScale = noteHeightScale[0];

        if (ImGui.button("Reset Note Height Scale")) {
            config.noteHeightScale = 0.1f;
        }
    }

    private void drawMiscSettings() {
        ImGui.separator();
        ImGui.text("Misc");

        if (ImGui.button("Show welcome message")) {
            welcomePanel.show();
        }
    }
}
