# Zephra Client

- `mod/` Fabric mod (1.21.4): FPS, CPS, Coordinates, Keystrokes, Armor, Zoom (hold Z), Fullbright, Toggle sprint. Menu: Right Shift.
- `launcher/` Electron launcher (black & white), Microsoft login, auto-installs Fabric + Sodium, Lithium, FerriteCore, ImmediatelyFast, EntityCulling, Fabric API.

## Vývoj
    cd mod && JAVA_HOME=$(/usr/libexec/java_home -v 21) gradle build
    cp build/libs/zephra-client-0.1.0.jar ../launcher/resources/zephra-client.jar
    cd ../launcher && npm start

## Aplikace pro ostatní
Lokálně: `cd launcher && npm run dist` (instalátor je v launcher/dist).
Pro všechny systémy: nahraj složku na GitHub a pushni tag `git tag v0.1.0 && git push --tags`, GitHub Actions postaví .dmg, .exe a .AppImage do Releases.

## Licenses
The Zephra font is Poppins (SIL Open Font License 1.1), see `THIRD_PARTY_LICENSES/Poppins-OFL.txt`.
