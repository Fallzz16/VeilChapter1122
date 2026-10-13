# Veil Chapter — Psychological Horror Mod

**Minecraft Java 1.12.2 / Forge 14.23.5.2859 / Java 8**

> **Status: v0.2.5 AUDIO 3D ALPHA, compilation verified and a production Forge 1.12.2 server started successfully with the mod. Client-side spatial audio listening and seven-day gameplay are NOT yet tested. Use a throwaway test world.**

## Download latest compiled mod

- **[Veil Chapter v0.2.5 — GitHub Actions compiled JAR artifact](https://github.com/Fallzz16/VeilChapter1122/actions/runs/38326793703)** — choose `veilchapter-1122-v0.2.5-AUDIO3D-NOT-GAMEPLAY-TESTED` under Artifacts, unzip and put only the JAR in your Minecraft 1.12.2 Forge `mods` folder.
- [Audio 3D playtest instructions in Portuguese](LEIA_PRIMEIRO_AUDIO.md)
- [Original design and current limitations in Portuguese](README_PTBR.md)

## Evidence

- [14 decoded mono Vorbis OGG files verified by manifest SHA-256 + Forge build/reobfuscation + JAR content checks](https://github.com/Fallzz16/VeilChapter1122/actions/runs/38326793703)
- [Real production Forge server boot with v0.2.5 installed in mods](https://github.com/Fallzz16/VeilChapter1122/actions/runs/38326817375)

## Audio playback tests

Enable cheats in a new test world:

```text
/veiltest audio psst left
/veiltest audio psst right
/veiltest audio hey behind
/veiltest audio laugh front
/veiltest audio breath right
/veiltest psst
```

All 14 sounds are stored as mono OGG and are played by positional Minecraft world coordinates. Long clips have pre-rendered fades to avoid abrupt beginnings and endings. This is not a headphone stereo track; Minecraft's OpenAL performs spatialization.

Known limits: no acoustic wall occlusion simulation; true client perceived audio still requires an actual player's listening test. Do not enable experimental dimension, destructive fires, stasis or teleports on important worlds.

Dynamic Lights remains external; it is not bundled.
