package synthesizer.visualizer.Graphics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.system.MemoryUtil.*;

public class Texture {
    private int id;

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    private static ByteBuffer ioResourceToByteBuffer(InputStream inputStream) throws IOException {
        ByteBuffer buffer = BufferUtils.createByteBuffer(40096);
        ReadableByteChannel rbc = Channels.newChannel(inputStream);

        while (true) {
            int bytes = rbc.read(buffer);
            if (bytes == -1) {
                break;
            }
            if (buffer.remaining() == 0) {
                buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
            }
        }

        buffer.flip();
        return memSlice(buffer);
    }

    public Texture(InputStream data) throws IOException {
        id = GL30.glGenTextures();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            IntBuffer channels_in_file = stack.mallocInt(1);
            ByteBuffer decodedData = STBImage.stbi_load_from_memory(
                    ioResourceToByteBuffer(data),
                    x, y,
                    channels_in_file, 0);

            bind();
            int width = x.get(0);
            int height = y.get(0);
            int channels = channels_in_file.get(0);

            GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);

            switch (channels) {
                case 3:
                    GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, width, height, 0, GL30.GL_RGB,
                            GL30.GL_UNSIGNED_BYTE, decodedData);
                    break;
                case 4:
                    GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, width, height, 0, GL30.GL_RGBA,
                            GL30.GL_UNSIGNED_BYTE, decodedData);
                    break;
            }

            GL30.glTexParameterIi(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
            GL30.glTexParameterIi(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);

            unbind();
        }

    }

    public Texture() {
        id = GL30.glGenTextures();

        bind();
        GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);
        ByteBuffer buffer = BufferUtils.createByteBuffer(4);
        buffer.put((byte) 0xFF);
        buffer.put((byte) 0xFF);
        buffer.put((byte) 0xFF);
        buffer.put((byte) 0x80);
        buffer.flip();

        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, 1, 1, 0, GL30.GL_RGBA,
                GL30.GL_UNSIGNED_BYTE, buffer);
        GL30.glTexParameterIi(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameterIi(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);

        unbind();
    }

    public void bind() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
    }

    public void unbind() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }

    public void release() {
        GL30.glDeleteTextures(id);
    }

    public int getID() {
        return id;
    }
}
