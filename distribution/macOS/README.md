# Digital for macOS

`Digital.app` is a regular macOS application which contains its own Java runtime,
so Java does not need to be installed. A double click on a `*.dig` file in the Finder
opens the circuit in Digital.

There are two disk images, one for each processor type:

| File                                 | Mac                          |
|--------------------------------------|------------------------------|
| `Digital-<version>-macOS-arm64.dmg`  | Apple Silicon (M1 and newer) |
| `Digital-<version>-macOS-x86_64.dmg` | Intel                        |

macOS 11 (Big Sur) or newer is required.

## Installation

1. Open the disk image and drag `Digital` onto `Applications`.
2. Start Digital from the Applications folder. The app is not notarized by Apple,
   so macOS refuses to open it the first time. Open *System Settings › Privacy & Security*,
   scroll down to the message about Digital and click *Open Anyway*. This is only
   necessary once. (Up to macOS 14 you can also right-click the app and choose *Open*.)
3. If another application is registered for `*.dig` files, select a `*.dig` file in the
   Finder, choose *File › Get Info*, select Digital under *Open with* and click *Change All…*.

## Command line

The app contains the command `digital`, which does the same as `java -jar Digital.jar`
and `java -cp Digital.jar CLI`, using the Java runtime of the app. `digital help` shows
the commands. To call it from any folder, link it once into a folder of the `PATH`:

    sudo mkdir -p /usr/local/bin
    sudo ln -s /Applications/Digital.app/Contents/app/bin/digital /usr/local/bin/digital

## Building

Requirements: macOS, git, [Maven](https://maven.apache.org/) and a JDK 17 or newer which
contains `jpackage`. The published disk images are built with Eclipse Temurin 21.

    distribution/macOS/build-dmg.sh

`target/macos/` then contains the disk image, the commit and tools used for the build,
and checksums. All timestamps are taken from the commit, so building the same commit with
the same JDK build on the same macOS version creates an identical `Digital.app`.

How the app is created:

* `jpackage` creates `Digital.app` from `target/Digital.jar` and a Java runtime which
  contains only the modules needed by Digital (as determined by `jdeps`), the EC crypto
  provider for https and the locale data of the languages supported by Digital.
  Custom components loaded from jar files can only use these modules; if one needs
  more, run `Digital.jar` with an installed Java instead.
* A second launcher `digital-cli` starts the command line interface, the script
  `digital` chooses between the two launchers.
* `resources/Info.plist` is used instead of the jpackage template. It declares the
  `*.dig` document type and the minimum macOS version.
* Double clicks in the Finder are not passed to the application as command line
  arguments but as an "open files" event. It is handled by
  `src/main/java/de/neemann/digital/gui/MacOSOpenFilesHandler.java`.
* `Digital.icns` and `digital-doc.icns` are the macOS app and document icons.
  Their sources are `Digital.svg` and `digital-doc.svg` in this folder.

## Credits

The macOS app goes back to Lorenzo Federici
([LoreFederici/Digital](https://github.com/LoreFederici/Digital)), who created the app
bundle with Finder support and the app and document icons and proposed the `digital`
command. Diego Marini reworked the build and the Finder support and implemented the
`digital` command.
