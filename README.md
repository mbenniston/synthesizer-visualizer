# Synthesizer Visualizer

Visualization program written in Java for watching midi files be played on a virtual piano.
Uses the [midi](https://github.com/mbenniston/midi) library for loading midis
and the [synthesizer](https://github.com/mbenniston/synthesizer) library for augmenting the
audio
of various instruments.

Features:

- Loads, plays and displays basic midis
- Scrubbing through track
- Playback speed and volume controls
- Custom channel colors and background images

Missing features:

- Sound fonts
- Fancy shading effects

![Demo screenshot](/docs/demo-screenshot.jpg)
[Demo video](https://youtu.be/VOCLruf8pm8)

Technical details:

- Uses LWJGL and OpenGL 3.3 for rendering
- Uses ImGui for GUI widgets