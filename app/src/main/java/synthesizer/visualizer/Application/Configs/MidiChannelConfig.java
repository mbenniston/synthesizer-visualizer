package synthesizer.visualizer.Application.Configs;

import java.util.Random;

import org.joml.Vector3f;

import com.google.common.util.concurrent.AtomicDouble;

import synthesizer.Playback.AudioPlayer;
import synthesizer.visualizer.Graphics.Texture;
import synthesizer.visualizer.Utils.Constants;

public class MidiChannelConfig {
    public final int index;
    public final AtomicDouble volume;

    public boolean enabled;
    public String name;
    public Texture texture;
    public Vector3f color;

    public final float[] sampleValues = new float[Constants.NUM_CHANNEL_MONITOR_SAMPLES];

    public MidiChannelConfig(int index, AtomicDouble volume) {
        this.index = index;
        this.volume = volume;

        color = new Vector3f();
        Random random = new Random();
        random.setSeed(index);
        color.x = random.nextFloat(0.0f, 1.0f);
        color.y = random.nextFloat(0.0f, 1.0f);
        color.z = random.nextFloat(0.0f, 1.0f);
    }
}
