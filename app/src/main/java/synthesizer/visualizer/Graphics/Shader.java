package synthesizer.visualizer.Graphics;

import java.io.IOException;
import java.io.InputStream;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

public final class Shader {
    private int program;

    public Shader(InputStream vertexShaderSource, InputStream fragmentShaderSource) throws IOException {
        program = GL30.glCreateProgram();
        int vertexShader = loadShader(GL30.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = loadShader(GL30.GL_FRAGMENT_SHADER, fragmentShaderSource);

        GL30.glAttachShader(program, vertexShader);
        GL30.glAttachShader(program, fragmentShader);
        GL30.glLinkProgram(program);
        GL30.glValidateProgram(program);
    }

    private int loadShader(int type, InputStream source) throws IOException {
        int shader = GL30.glCreateShader(type);

        GL30.glShaderSource(shader, new String(source.readAllBytes()));
        GL30.glCompileShader(shader);

        return shader;
    }

    public void use() {
        GL30.glUseProgram(program);
    }

    public void dispose() {
        GL30.glUseProgram(0);
    }

    public void release() {
        GL30.glDeleteProgram(program);
    }

    public void loadUniform(int location, float f) {
        GL30.glUniform1f(location, f);
    }

    public void loadUniform(int location, int i) {
        GL30.glUniform1i(location, i);
    }

    public void loadUniform(int location, Vector3f v) {
        GL30.glUniform3f(location, v.x, v.y, v.z);
    }

    public void loadUniform(int location, Matrix4f m) {
        float[] buffer = new float[16];
        m.get(buffer);
        GL30.glUniformMatrix4fv(location, false, buffer);
    }

    public int getUniformLocation(String uniformName) {
        return GL30.glGetUniformLocation(program, uniformName);
    }

}
