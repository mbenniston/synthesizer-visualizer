package synthesizer.visualizer.Graphics;

import java.io.IOException;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;

import synthesizer.visualizer.Playback.NoteHistory;
import synthesizer.visualizer.Playback.NoteHistory.NotePress;

public class NoteHistoryRenderer {
    private final Shader shader;
    private final Quad quad;
    private final int timeLoc;
    private final int projLoc;
    private final int texLoc;
    private final int modelLoc;
    private final int colorLoc;

    private final Matrix4f modelMatrix = new Matrix4f(),
            projectionMatrix = new Matrix4f();

    private final double historyTimeSeconds = 3;
    private double pixelsPerSecond;
    private float scale;
    private float noteWidth;
    private float viewportWidth, viewportHeight;

    private final Texture defaultTexture;

    public NoteHistoryRenderer() throws IOException {
        shader = new Shader(
                NoteHistoryRenderer.class.getClassLoader().getResourceAsStream("shaders/flat.vert"),
                NoteHistoryRenderer.class.getClassLoader().getResourceAsStream("shaders/flat.frag"));
        quad = new Quad();

        timeLoc = shader.getUniformLocation("u_time");
        projLoc = shader.getUniformLocation("u_proj");
        texLoc = shader.getUniformLocation("u_texture");
        modelLoc = shader.getUniformLocation("u_model");
        colorLoc = shader.getUniformLocation("u_color");
        defaultTexture = new Texture();
    }

    private void use() {
        shader.use();
        shader.loadUniform(timeLoc, (float) GLFW.glfwGetTime());
        shader.loadUniform(projLoc, projectionMatrix);
        shader.loadUniform(texLoc, 0);

        quad.bind();

        defaultTexture.bind();

        // GL30.glEnable(GL30.GL_BLEND);
        // GL30.glBlendFunc(GL30.GL_DST_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void renderBackground(Texture texture) {
        use();
        texture.bind();
        modelMatrix.identity();
        modelMatrix.scale(new Vector3f(viewportWidth, viewportHeight, 0.0f));
        shader.loadUniform(modelLoc, modelMatrix);
        quad.drawUnbound();
        texture.unbind();
        dispose();
    }

    private void dispose() {
        shader.dispose();
        quad.unbind();
    }

    public void release() {
        shader.release();
        quad.release();
    }

    public void updateViewport(int windowWidth, int windowHeight) {
        GL30.glViewport(0, 0, windowWidth, windowHeight);
        projectionMatrix.setOrtho(0.0f, (float) windowWidth, 0.0f, (float) windowHeight, 0.0f, 1.0f);

        pixelsPerSecond = windowHeight / historyTimeSeconds;
        noteWidth = (float) (windowWidth / ((128 / 12.0) * 7));
        scale = (float) pixelsPerSecond;
        viewportWidth = windowWidth;
        viewportHeight = windowHeight;
    }

    public void renderNoteHistories(NoteHistory[] histories, double currentTime, Vector3f color,
            float noteHeightScale, Texture noteTexture) {
        use();

        if (noteTexture != null) {
            noteTexture.bind();
        }

        final float noteHeight = viewportHeight * noteHeightScale;

        shader.loadUniform(colorLoc, color);

        double x = 0.0;

        final boolean[] blackKeys = new boolean[] {
                false,
                true,
                false,
                true,
                false,
                false,
                true,
                false,
                true,
                false,
                true,
                false,
        };

        for (int i = 0; i < 128; i++) {
            NoteHistory noteHistory = histories[i];

            int octaveIndex = i % 12;
            boolean isBlackKey = blackKeys[octaveIndex];

            for (NotePress press : noteHistory.getNotePresses()) {

                // max threshold
                if ((noteHeight + (float) (currentTime - press.endTime)
                        * scale) > viewportHeight)
                    continue;
                // min threshold
                if ((noteHeight + (float) (currentTime - press.startTime)
                        * scale) < 0)
                    continue;

                modelMatrix.identity();
                modelMatrix.translate(
                        new Vector3f((float) (isBlackKey ? x - 0.25f : x) * noteWidth,
                                noteHeight + (float) (currentTime - press.endTime)
                                        * scale,
                                0));
                modelMatrix.scale(new Vector3f(isBlackKey ? noteWidth / 2 : noteWidth,
                        (float) Math.ceil(scale * press.getDuration()),
                        1));
                shader.loadUniform(modelLoc, modelMatrix);
                quad.drawUnbound();
            }

            if (noteHistory.isCurrentlyPressed()) {
                modelMatrix.identity();
                modelMatrix.translate(
                        new Vector3f((float) (isBlackKey ? x - 0.25f : x) * noteWidth,
                                noteHeight + (float) 0 * scale,
                                0));
                modelMatrix.scale(new Vector3f(isBlackKey ? noteWidth / 2 : noteWidth,
                        scale * (float) (currentTime
                                - noteHistory.getLastTimePressed()),
                        1));
                shader.loadUniform(modelLoc, modelMatrix);
                quad.drawUnbound();
            }
            if (!isBlackKey) {
                x += 1.0f;
            }
        }
        dispose();
    }

    public void renderNotePresses(boolean[] pressedNotes, float noteHeightScale) {
        use();
        final float noteHeight = viewportHeight * noteHeightScale;

        Vector3f color = new Vector3f();

        double x = 0.0;

        final boolean[] blackKeys = new boolean[] {
                false,
                true,
                false,
                true,
                false,
                false,
                true,
                false,
                true,
                false,
                true,
                false,
        };

        // draw white keys

        for (int i = 0; i < 128; i++) {

            boolean notePressed = pressedNotes[i];

            int octaveIndex = i % 12;
            boolean isBlackKey = blackKeys[octaveIndex];

            color = new Vector3f(0.9f);

            if (notePressed) {
                color = new Vector3f(0.7f);
            }
            shader.loadUniform(colorLoc, color);

            modelMatrix.identity();
            modelMatrix.translate(
                    new Vector3f((float) (x) * noteWidth,
                            0,
                            0));
            modelMatrix.scale(new Vector3f(noteWidth,
                    (noteHeight),
                    1));
            shader.loadUniform(modelLoc, modelMatrix);
            if (!isBlackKey) {
                quad.drawUnbound();
                x += 1.0f;
            }
        }

        x = 0;

        // draw black keys

        for (int i = 0; i < 128; i++) {

            boolean notePressed = pressedNotes[i];

            int octaveIndex = i % 12;
            boolean isBlackKey = blackKeys[octaveIndex];

            color = new Vector3f(0.3f);

            if (notePressed) {
                color = new Vector3f(0.1f);
            }
            shader.loadUniform(colorLoc, color);

            modelMatrix.identity();
            modelMatrix.translate(
                    new Vector3f((float) (x - 0.25f) * noteWidth,
                            noteHeight * 0.33333f,
                            0));
            modelMatrix.scale(new Vector3f(noteWidth / 2,
                    (noteHeight * 0.66666f),
                    1));
            shader.loadUniform(modelLoc, modelMatrix);
            if (isBlackKey) {
                quad.drawUnbound();
            } else {
                x += 1.0f;
            }
        }

        dispose();
    }
}
