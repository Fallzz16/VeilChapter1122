# Veil Chapter — Psychological Horror Mod

**Minecraft Java 1.12.2 / Forge 14.23.5.2859 / Java 8**

> **Latest: v0.2.6 TREE RITUAL alpha. Forge build and production-server geometry checks have been implemented. In-client visual, FPS, and full seven-day gameplay testing is still required; use a separate test world.**

## Latest downloads and build results

- [Latest automated Forge build and JAR artifact](https://github.com/Fallzz16/VeilChapter1122/actions/workflows/forge-1122-ci.yml)
- [Latest production Forge server startup + tree plan verification](https://github.com/Fallzz16/VeilChapter1122/actions/workflows/forge-production-smoke.yml)
- [Tree Ritual v0.2.6 — technical QA and playtest in Portuguese](TREE_RITUAL_v0.2.6_QA_PTBR.md)
- [Audio 3D controls, fades and source details](LEIA_PRIMEIRO_AUDIO.md)
- [Project overview in Portuguese](README_PTBR.md)

The compiled mod file is named `VeilChapter1122-0.2.6-tree-ritual.jar`. Unzip the GitHub Actions artifact and put only this JAR in a Minecraft 1.12.2 Forge `mods` folder, replacing the older version. Do not put source ZIPs there.

## Tree Ritual 2.0

The protected tree scene now lasts around 150 seconds, revealing roots, trunk and branches, the crown silhouette and details. Construction is capped at 24 block operations per tick. The real tree blueprint was validated in a production Forge 1.12.2 server: 36,620 unique blocks, 4-stage timing budget and unobstructed interior approach.

Three marked, nonviolent Watcher appearances and Portuguese actionbar messages accompany the ritual; scene sounds use the pre-existing 14 mono 3D OGG recordings.

## Test

After making a backup, enable `B:enableTreeSequence=true` in `config/veilchapter.cfg`, restart Minecraft, then in a throwaway world with cheats:

```text
/veiltest day 7
/veiltest tree
/veilstatus
```

Check growth, FPS, how much of the crown is visible from the ground, the three apparitions, chest/book return, and save/resume. Render distance can still clip the top of a 110-block tree; the new viewpoint helps but cannot override Minecraft's render-distance limits.

**Only the Forge server startup and plan calculation are automated. Client-side gameplay/rendering requires a real player test.**
