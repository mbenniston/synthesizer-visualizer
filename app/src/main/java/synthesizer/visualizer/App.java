package synthesizer.visualizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import imgui.ImGui;
import imgui.app.Configuration;

import midi.Reading.MidiFileReader.MidiLoadError;
import synthesizer.visualizer.Application.*;
import synthesizer.visualizer.Application.Configs.ApplicationConfig;
import synthesizer.visualizer.Application.Configs.MidiChannelConfig;
import synthesizer.visualizer.Graphics.NoteHistoryRenderer;
import synthesizer.visualizer.Application.Panels.ApplicationControlPanel;
import synthesizer.visualizer.Application.Panels.MidiControlPanel;
import synthesizer.visualizer.Application.Panels.PlayerControlPanel;
import synthesizer.visualizer.Application.Panels.WelcomePanel;
import synthesizer.visualizer.Application.Panels.MidiControlPanel.PickMidiHandler;
import synthesizer.visualizer.Application.Panels.MidiControlPanel.ScrubHandler;
import synthesizer.visualizer.Playback.ChannelHistory;
import synthesizer.visualizer.Playback.NoteHistory;
import synthesizer.visualizer.Playback.Visualizer;
import synthesizer.visualizer.Utils.Constants;

public class App extends ApplicationWithImGui {
    private final ApplicationConfig applicationConfig = new ApplicationConfig();

    private Visualizer visualizer;
    private NoteHistoryRenderer historyRenderer;

    private MidiControlPanel midiControlPanel;
    private ArrayList<Panel> panels = new ArrayList<>();
    private boolean showPanels = true;

    private double lastMidiTime = 0;

    @Override
    protected void preRun() {
        visualizer = new Visualizer();

        try {
            historyRenderer = new NoteHistoryRenderer();
        } catch (IOException e1) {
            throw new RuntimeException("Could not initialize renderer");
        }

        final PickMidiHandler pickMidiHandler = new PickMidiHandler() {
            @Override
            public void onPick(String path) {
                try {
                    visualizer.loadMidi(path);
                    midiControlPanel.setFileLengthInSeconds(visualizer.getMidiFileLengthInSeconds());
                } catch (FileNotFoundException | MidiLoadError e) {
                }
            }
        };

        final ScrubHandler scrubHandler = new MidiControlPanel.ScrubHandler() {
            @Override
            public void onSeek(double seekTo) {
                visualizer.seek(seekTo);
            }
        };

        midiControlPanel = new MidiControlPanel(
                visualizer.getMidiPlayerConfig(),
                pickMidiHandler,
                scrubHandler);

        WelcomePanel welcomePanel = new WelcomePanel();
        panels.add(welcomePanel);
        panels.add(midiControlPanel);
        panels.add(new ApplicationControlPanel(applicationConfig, welcomePanel));
        panels.add(new PlayerControlPanel(visualizer.getPlayerConfig()));
    }

    @Override
    protected void preFrame() {
        visualizer.update();
        processInput();
        onRender();
    }

    private void processInput() {
        final int noteBase = 60;

        visualizer.updatePlayerNote(noteBase + 0,
                ImGui.isKeyDown(GLFW.GLFW_KEY_Z));
        visualizer.updatePlayerNote(noteBase + 1,
                ImGui.isKeyDown(GLFW.GLFW_KEY_S));
        visualizer.updatePlayerNote(noteBase + 2,
                ImGui.isKeyDown(GLFW.GLFW_KEY_X));
        visualizer.updatePlayerNote(noteBase + 3,
                ImGui.isKeyDown(GLFW.GLFW_KEY_D));
        visualizer.updatePlayerNote(noteBase + 4,
                ImGui.isKeyDown(GLFW.GLFW_KEY_C));
        visualizer.updatePlayerNote(noteBase + 5,
                ImGui.isKeyDown(GLFW.GLFW_KEY_V));
        visualizer.updatePlayerNote(noteBase + 6,
                ImGui.isKeyDown(GLFW.GLFW_KEY_G));
        visualizer.updatePlayerNote(noteBase + 7,
                ImGui.isKeyDown(GLFW.GLFW_KEY_B));
        visualizer.updatePlayerNote(noteBase + 8,
                ImGui.isKeyDown(GLFW.GLFW_KEY_H));
        visualizer.updatePlayerNote(noteBase + 9,
                ImGui.isKeyDown(GLFW.GLFW_KEY_N));
        visualizer.updatePlayerNote(noteBase + 10,
                ImGui.isKeyDown(GLFW.GLFW_KEY_J));
        visualizer.updatePlayerNote(noteBase + 11,
                ImGui.isKeyDown(GLFW.GLFW_KEY_M));
        visualizer.updatePlayerNote(noteBase + 12,
                ImGui.isKeyDown(GLFW.GLFW_KEY_COMMA));
    }

    protected void onRender() {
        renderNoteHistories();
        renderPressedNotes();
    }

    private void renderNoteHistories() {
        final ChannelHistory[] channelHistories = visualizer.getChannelHistories();
        final MidiChannelConfig[] channels = visualizer.getChannels();

        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFunc(GL30.GL_DST_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        if (applicationConfig.backgroundTexture != null) {
            historyRenderer.renderBackground(applicationConfig.backgroundTexture);
        }

        historyRenderer.updateViewport(getWindowSize().x, getWindowSize().y);
        historyRenderer.renderNoteHistories(visualizer.getPlayerNoteHistories(), visualizer.getPlayerTime(),
                new Vector3f(0, 1, 1),
                applicationConfig.noteHeightScale, null);

        for (int c = 0; c < Constants.NUM_MIDI_CHANNELS; c++) {
            ChannelHistory history = channelHistories[c];

            for (int i = 0; i < Constants.NUM_MIDI_NOTES; i++) {
                historyRenderer.renderNoteHistories(history.getNoteHistories(), visualizer.getMidiTime(),
                        channels[c].color,
                        applicationConfig.noteHeightScale, channels[c].texture);
            }
        }
        GL30.glDisable(GL30.GL_BLEND);
    }

    private void renderPressedNotes() {
        final double currentMidiTime = visualizer.getMidiTime();
        final ChannelHistory[] channelHistories = visualizer.getChannelHistories();
        final NoteHistory[] playerNoteHistories = visualizer.getPlayerNoteHistories();

        final boolean pressedNotes[] = new boolean[Constants.NUM_MIDI_NOTES];

        for (int c = 0; c < Constants.NUM_MIDI_CHANNELS; c++) {
            final ChannelHistory history = channelHistories[c];

            for (int i = 0; i < Constants.NUM_MIDI_NOTES; i++) {
                pressedNotes[i] |= history.getNoteHistories()[i].wasPressedBetween(lastMidiTime, currentMidiTime);
            }
        }

        for (int i = 0; i < Constants.NUM_MIDI_NOTES; i++) {
            pressedNotes[i] |= playerNoteHistories[i].isCurrentlyPressed();
        }

        historyRenderer.renderNotePresses(pressedNotes, applicationConfig.noteHeightScale);

        lastMidiTime = currentMidiTime;
    }

    @Override
    public void process() {
        if (ImGui.isKeyPressed(GLFW.GLFW_KEY_E, false)) {
            showPanels = !showPanels;
        }

        if (showPanels) {
            for (Panel panel : panels) {
                panel.draw();
            }
        }
    }

    @Override
    protected void postRun() {
        visualizer.stop();
        historyRenderer.release();
    }

    @Override
    protected void clearBuffer() {
        GL11.glClearColor(applicationConfig.backgroundColor.x, applicationConfig.backgroundColor.y,
                applicationConfig.backgroundColor.z, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void configure(Configuration config) {
        config.setTitle("Synthesizer-Visualizer");
    }

    public static void main(String[] args) {
        launch(new App());
    }
}