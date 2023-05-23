package synthesizer.visualizer.Application.Panels;

import imgui.ImGui;
import imgui.type.ImBoolean;
import synthesizer.visualizer.Application.Panel;

public class WelcomePanel implements Panel {
    public final ImBoolean isOpen = new ImBoolean(true);

    @Override
    public void draw() {
        if (!isOpen.get())
            return;

        if (ImGui.begin("Welcome", isOpen)) {
            ImGui.textWrapped(
                    "Welcome to Synthesizer visualizer, this tool can be used to play midis and visualize their notes.");

            ImGui.separator();
            ImGui.textWrapped("Controls");
            ImGui.textWrapped("Press the 'E' key to show/hide all panels");
        }
        ImGui.end();

    }

    public void show() {
        isOpen.set(true);
    }

}
