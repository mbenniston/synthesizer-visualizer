package synthesizer.visualizer.Utils;

public class Format {

    public static String formatDuration(double duration) {
        int minutes = (int) duration / 60;
        int seconds = (int) duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
