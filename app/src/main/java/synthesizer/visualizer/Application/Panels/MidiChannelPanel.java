package synthesizer.visualizer.Application.Panels;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.io.FileInputStream;
import java.io.IOException;

import synthesizer.visualizer.Application.Panel;
import synthesizer.visualizer.Application.Configs.MidiChannelConfig;
import synthesizer.visualizer.Graphics.Texture;

public class MidiChannelPanel implements Panel {
    private final MidiChannelConfig channelData;

    public final ImBoolean isOpen = new ImBoolean(false);

    public void open() {
        isOpen.set(true);
    }

    public void close() {
        isOpen.set(false);
    }

    public MidiChannelPanel(MidiChannelConfig channelData) {
        this.channelData = channelData;
    }

    @Override
    public void draw() {
        if (!isOpen.get())
            return;

        if (ImGui.begin("Midi Channel " + channelData.index, isOpen)) {

            ImGui.labelText("" + channelData.name, "Program");

            ImFloat volume = new ImFloat((float) channelData.volume.get());
            if (ImGui.sliderFloat("Volume", volume.getData(), 0, 1)) {
                channelData.volume.set(volume.get());
            }

            if (ImGui.button("Pick texture")) {
                String texturePath = TinyFileDialogs.tinyfd_openFileDialog(
                        "Choose texture", null, null, null, false);

                if (texturePath != null) {
                    try {
                        channelData.texture = new Texture(new FileInputStream(texturePath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (ImGui.button("Reset texture")) {
                if (channelData.texture != null) {
                    channelData.texture.release();
                }
                channelData.texture = null;
            }

            float[] color = new float[] { channelData.color.x, channelData.color.y, channelData.color.z };
            if (ImGui.colorEdit3("Color", color)) {
                channelData.color.x = color[0];
                channelData.color.y = color[1];
                channelData.color.z = color[2];
            }

            ImGui.plotLines("Monitor", channelData.sampleValues, channelData.sampleValues.length);

        }
        ImGui.end();
    }

}
