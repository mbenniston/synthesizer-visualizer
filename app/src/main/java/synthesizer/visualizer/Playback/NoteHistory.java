package synthesizer.visualizer.Playback;

import java.util.ArrayList;
import java.util.List;

public class NoteHistory {

    private final ArrayList<NotePress> notePresses = new ArrayList<>();

    private boolean noteDown = false;
    private double lastNoteDownTime;

    public void setNoteUp(double executeTime) {
        if (noteDown) {
            NotePress notePress = new NotePress(
                    lastNoteDownTime,
                    executeTime);
            notePresses.add(notePress);
        }

        noteDown = false;
    }

    public void setNoteDown(double executeTime) {
        if (!noteDown) {
            noteDown = true;
            lastNoteDownTime = executeTime;
        }
    }

    public boolean isCurrentlyPressed() {
        return noteDown;
    }

    public double getLastTimePressed() {
        return lastNoteDownTime;
    }

    public void clear() {
        notePresses.clear();
        noteDown = false;
        lastNoteDownTime = 0;
    }

    public boolean wasPressedBetween(double startTime, double endTime) {
        if (noteDown && endTime >= lastNoteDownTime) {
            return true;
        }

        for (int i = notePresses.size() - 1; i >= 0; i--) {
            final NotePress press = notePresses.get(i);

            if (press.containsTime(startTime) || press.containsTime(endTime)) {
                return true;
            }

            if (press.endTime < startTime) {
                return false;
            }
        }

        return false;
    }

    public List<NotePress> getNotePresses() {
        return notePresses;
    }

    public static class NotePress {
        public final double startTime, endTime;

        public NotePress(double startTime, double endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public double getDuration() {
            return endTime - startTime;
        }

        public boolean containsTime(double time) {
            return time > startTime && time <= endTime;
        }
    }
}
