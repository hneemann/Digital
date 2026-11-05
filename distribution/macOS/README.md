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
2. Start Digital from the Applications folder. The app has an ad-hoc signature but is
   not notarized by Apple, so macOS refuses to open it the first time. Open
   *System Settings › Privacy & Security*, scroll down to the message about Digital
   and click *Open Anyway*. This is only necessary once.
   (Up to macOS 14 it is also possible to right-click the app and choose *Open*.)
3. From now on `*.dig` files open in Digital by double click. If another application
   is registered for `*.dig` files, select a `*.dig` file in the Finder, choose
   *File › Get Info*, select Digital under *Open with* and click *Change All…*.

If a circuit is opened by the Finder while the main window still shows the unchanged
circuit Digital loaded by itself at startup (the most recently used one), this circuit
is replaced. Otherwise, e.g. if the main window shows a circuit you opened, a new
window is opened.

## Command line

The app contains the command `digital`, which does the same as `java -jar Digital.jar`
and `java -cp Digital.jar CLI`, using the Java runtime of the app:

    digital                          # starts the simulator
    digital circuit.dig              # starts the simulator and opens circuit.dig
    digital test -circ circuit.dig   # runs the tests of circuit.dig
    digital svg -dig circuit.dig     # exports circuit.dig as SVG
    digital help                     # shows all commands

Installing the command is optional. After moving Digital to `/Applications`,
link it into a folder of the `PATH` once to use it from any folder:

    sudo mkdir -p /usr/local/bin
    sudo ln -s /Applications/Digital.app/Contents/app/bin/digital /usr/local/bin/digital

The link points into the app, so it keeps working when the app is updated.
If `/usr/local/bin/digital` already exists, `ln` refuses to replace it; check what
is installed there before making changes. Digital does not install the link automatically.
The commands of the command line interface run without a window or dock icon.

## Building

Requirements: macOS, git, [Maven](https://maven.apache.org/) and a JDK 17 or newer which
contains `jpackage`. The published disk images are built with Eclipse Temurin 21.

    distribution/macOS/build-dmg.sh

The result is written to `target/macos/`:

| File               | Content                                                   |
|--------------------|-----------------------------------------------------------|
| `*.dmg`            | the disk image                                            |
| `*.manifest.txt`   | SHA-256 hash and permissions of every file in Digital.app |
| `*.build-info.txt` | commit, JDK, Maven and macOS used for the build           |
| `*.sha256`         | checksums of the three files above                        |

Only committed sources are built, because only these can be verified by others
(`ALLOW_DIRTY=1` overrides this check). `RUN_TESTS=1` runs the unit tests as well.

`test-app.sh <dmg> <folder>` starts the app from a disk image: it runs `digital test`
and `digital svg` and opens a circuit in the simulator with the same event as a double
click in  the Finder. Unlike `verify-dmg.sh` it runs code from the disk image.
The GitHub workflow runs  it for both disk images on the processor type they were built
for and keeps a screenshot.

How the app is created:

* `jpackage` creates `Digital.app` from `target/Digital.jar` and a Java runtime which
  contains only the modules needed by Digital (as determined by `jdeps`), the EC crypto
  provider for https and the locale data of the languages supported by Digital.
  A second launcher `digital-cli` starts the command line interface, the script
  `digital` chooses between the two launchers.
  Custom components loaded from jar files can only use these modules; if one needs
  more, run `Digital.jar` with an installed Java instead.
* `resources/Info.plist` is used instead of the jpackage template. It declares the
  `*.dig` document type (`de.neemann.digital.dig`) and the minimum macOS version.
  The name of the macOS document type is "Digital Circuit".
  Since jpackage signs the bundle after writing this file, the bundle is never modified
  after signing and the signature stays valid.
* Double clicks in the Finder are not passed to the application as command line
  arguments but as an "open files" event. It is handled by
  `src/main/java/de/neemann/digital/gui/MacOSOpenFilesHandler.java`.
* `Digital.icns` and `digital-doc.icns` are Lorenzo Federici's macOS app and
  document icons. Their original sources are `src/main/svg/digital.svg` and
  `src/main/svg/digital-doc.svg`. The app icon is used only for the macOS bundle.

Credits: the macOS app goes back to Lorenzo Federici
([LoreFederici/Digital](https://github.com/LoreFederici/Digital)), who created the
app bundle with Finder support and the app and document icons and proposed the `digital`
command. Diego Marini reworked the build to be reproducible and verifiable and
implemented the `digital` command.

## Verifying a disk image

The checks do not run any code from the disk image.

**1. Content and signature**

    distribution/macOS/verify-dmg.sh Digital-<version>-macOS-<arch>.dmg

checks that the disk image contains only `Digital.app` and a link to `/Applications`,
and that the code signature of the app is valid, i.e. no file was modified or added
after signing. The signature is ad hoc and does not show who signed the app; the
origin is shown by steps 2 and 3.

**2. Origin** — the disk images published by the GitHub workflow
`.github/workflows/macos-app.yml` come with a signed build provenance attestation:

    gh attestation verify Digital-<version>-macOS-<arch>.dmg --repo hneemann/Digital \
        --signer-workflow hneemann/Digital/.github/workflows/macos-app.yml \
        --source-ref refs/tags/v<version> --deny-self-hosted-runners

shows the repository, workflow and commit the disk image was built from. The options
make sure that it was built by this workflow on a GitHub runner from the release tag,
and not e.g. by a manually started run on another branch.

The checks only show that the disk image corresponds to the sources. As with every
program, only open `*.dig` files from sources you trust.

**3. Rebuild** — the build is reproducible: building the same commit with the same JDK
build on the same processor type and macOS version creates an identical `Digital.app`.
Check out the commit and use the JDK and macOS version listed in the `build-info.txt`
of the disk image (e.g. by running the workflow in a fork), then:

    distribution/macOS/build-dmg.sh
    distribution/macOS/verify-dmg.sh Digital-<version>-macOS-<arch>.dmg target/macos/Digital-<version>-macOS-<arch>.manifest.txt

The second command compares every file of the downloaded app with your own build.
The disk image files themselves differ between builds because `hdiutil` stores
timestamps, therefore their content is compared.

On another macOS version, the app differs in the code signatures of the native files
of the launcher and the Java runtime, because `codesign` uses other defaults, and the
Java runtime can also differ in a few classes generated by `jlink`. `Digital.jar` and
the other files of the app are still identical: compare the `Digital.jar sha256`
shown by `verify-dmg.sh` with `shasum -a 256 target/Digital.jar` of your build.
