# urmusic 5
_The Free and Open Source Music Visualizer Tool_

## What's urmusic?
urmusic is a free and open-source (GPL-3) software that allows you to easily create your own music visualizer and create a music video for it! It makes use of hardware acceleration to render frames as fast as possible.

## What does it look like?
Currently, I didn't really work on the UI, to focus on features and stability. Here are a few screenshots I have taken:

![urmusic5 - Default layout](/screenshots/urmusic5-default_view.png "Default layout")

![urmusic5 - Custom layout](/screenshots/urmusic5-custom_view.png "Custom layout")

![urmusic5 - Custom layout with multiple windows and JavaScript panel](/screenshots/urmusic5-custom_double_view_scripting.png "Custom layout with multiple windows and JavaScript panel")

## Features
- Music visualization
- Standard video effects including:
  - Audio spectrum/scope
  - Circle mask (can also be used for vignette effects or lightning)
  - Gaussian blur
  - Image display
  - Mirror effect
  - ...
  - \+ your very own [plugin](https://gitlab.com/nasso/urmusic5-core/wikis/Making-a-plugin)?
- Plugin system
- Hardware acceleration
- Key-frame based animations
- JavaScript scripting for advanced animations
- Fast video exporting to various containers and codecs
- Blender-like customizable GUI layout with support for multiple windows and screens (not themable yet!)
- +4K and +60 FPS rendering (although it might not be available everywhere, being limited by the hardware capabilities)
- ...
- \+ your very own [contribution](https://gitlab.com/nasso/urmusic5-core/blob/master/CONTRIBUTING.md#core)?

## Using it
See the [sourceforge project page](https://sourceforge.net/projects/urmusic5/) to download the latest release.

## Building
I'm providing the Eclipse project for those who wants it. Yes, I am using Eclipse, no I'm not planning to switch to `[insert your favorite IDE here]`. Yes, I'm using the libraries JAR files and dependencies, no, I'm not using any other particular build system.

_Note:_ This repo only contains the source for the core, without any effect. The source for the standard effects plugin can be found on [this repo](https://gitlab.com/nasso/urmusic5-plugin-stdfx).

### Dependencies
- [JOGL](http://jogamp.org/jogl/www) [2.3.2](http://jogamp.org/wiki/index.php/Release_2.3.2), to use OpenGL
- [Google GSON](https://github.com/google/gson) [2.8.1](https://github.com/google/gson/releases/tag/gson-parent-2.8.1), to parse the lang files that are in JSON
- [JOML](https://joml-ci.github.io/JOML) [1.9.3](https://github.com/JOML-CI/JOML/releases/tag/1.9.3), for linear algebra mathematics
- [Trove](https://bitbucket.org/trove4j/trove) [3.1a1](https://bitbucket.org/trove4j/trove/downloads/?tab=downloads), optimizing array lists of generic types
- [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) [2.6.1](https://github.com/bobbylight/RSyntaxTextArea/releases/tag/2.6.1), for the JavaScript editor panel

- [FFmpeg](http://ffmpeg.org) [3.4.2](http://ffmpeg.org/download.html#release_3.4), to encode and decode video and audio. You are free to replace the binaries provided though.

## Versioning
I use [SemVer](http://semver.org/) for versioning. For the versions available, see [the SourceForge project page](https://sourceforge.net/projects/urmusic5/files).

## Discussion
- On [SourceForge](https://sourceforge.net/p/urmusic5/discussion/)
- On my [Discord server](https://discord.gg/tugNkYT)

## License
[GPL-3](https://gitlab.com/nasso/urmusic5-core/blob/master/LICENSE.txt)
