package synthesizer.visualizer.Graphics;

import org.lwjgl.opengl.GL30;

public final class Quad {
    private int vertexArray;

    public Quad() {
        vertexArray = GL30.glGenVertexArrays();

        final float[] data = new float[] {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
        };

        int vertexBuffer = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vertexBuffer);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STATIC_DRAW);

        GL30.glBindVertexArray(vertexArray);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 0, 0);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void bind() {
        GL30.glBindVertexArray(vertexArray);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void drawUnbound() {
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
    }

    public void draw() {
        bind();
        drawUnbound();
        unbind();
    }

    public void release() {
        GL30.glDeleteVertexArrays(vertexArray);
    }

}
