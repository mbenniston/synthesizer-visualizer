package synthesizer.visualizer.Playback;

import java.util.Iterator;

public class SampleMonitor implements Iterable<Double> {
    private final double[] samples;
    private final int bufferSize;
    private int sampleIndex = 0;

    public SampleMonitor(int bufferSize) {
        this.bufferSize = bufferSize;
        samples = new double[bufferSize];
    }

    public void pushSample(double sample) {
        sampleIndex = (sampleIndex + 1) % bufferSize;
        samples[sampleIndex] = sample;
    }

    public void copyTo(SampleMonitor destination) {
        System.arraycopy(samples, 0, destination.samples, 0, bufferSize);
        destination.sampleIndex = sampleIndex;
    }

    public class SampleIterator implements Iterator<Double> {
        private final double[] samples = SampleMonitor.this.samples;
        private final int sampleHeadIndex = SampleMonitor.this.sampleIndex;
        private int i = 0;

        @Override
        public boolean hasNext() {
            return i < bufferSize;
        }

        @Override
        public Double next() {
            if (i >= bufferSize) {
                return null;
            }

            double sample = samples[(sampleHeadIndex + i) % bufferSize];
            i++;
            return sample;
        }
    }

    @Override
    public Iterator<Double> iterator() {
        return new SampleIterator();
    }
}
