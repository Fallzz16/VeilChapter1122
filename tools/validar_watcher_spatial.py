#!/usr/bin/env python3
"""Validate five Watcher dialogue cues x three environment variations (Forge 1.12.2)."""
import hashlib
import json
import pathlib
import subprocess
import sys

ROOT=pathlib.Path(__file__).resolve().parent.parent
MANIFEST=ROOT/"WATCHER_SPATIAL_ASSETS_QA.json"
REFERENCE=json.loads(MANIFEST.read_text(encoding="utf-8"))
EXPECTED={f"watcher_voice_{i:02d}_{kind}" for i in range(1,6)
          for kind in ("open","muffled","indoor")}
assert len(REFERENCE["files"])==15
assert REFERENCE["cue_seconds"]==[12,34,59,95,137]
assert REFERENCE["music_in_game"] is False
sounds=json.loads((ROOT/"src/main/resources/assets/veilchapter/sounds.json").read_text(encoding="utf-8"))
found=set()
for entry in REFERENCE["files"]:
    path=ROOT/entry["file"]
    assert path.parent.name=="watcher",path
    name=path.stem
    assert name in EXPECTED and name not in found,name
    found.add(name)
    assert path.is_file(),f"MISSING_OGG: {path}"
    assert path.read_bytes()[:4]==b"OggS",f"Not OGG: {path}"
    assert hashlib.sha256(path.read_bytes()).hexdigest()==entry["sha256"],f"SHA mismatch: {path}"
    r=subprocess.run(["ffprobe","-v","error","-select_streams","a:0",
        "-show_entries","stream=codec_name,channels,sample_rate",
        "-of","json",str(path)],capture_output=True,text=True,check=True)
    media=json.loads(r.stdout)["streams"][0]
    assert media["codec_name"]=="vorbis",path
    assert int(media["channels"])==1,path
    assert int(media["sample_rate"])==48000,path
    definition=sounds[name]
    assert definition["category"]=="voice",name
    assert len(definition["sounds"])==1,name
    assert definition["sounds"][0]["name"]=="veilchapter:watcher/"+name,name
    assert definition["sounds"][0]["stream"] is True,name
assert found==EXPECTED,(found,EXPECTED)
assert len(sounds)==19
assert set(sounds)==EXPECTED|{"psst","hey","laugh","breath"}
assert all(k.lower()!="music" for k in sounds)
tree=(ROOT/"src/main/java/br/veilchapter/world/TreeScene.java").read_text(encoding="utf-8")
assert "VOICE_SECONDS = {12, 34, 59, 95, 137}" in tree
assert "VOICE_DURATION_TICKS = {92, 124, 280, 172, 98}" in tree
print("WATCHER_SPATIAL_SFX_PASS: 15 OGG Vorbis mono, 48kHz, 5 cues, sha256, no music")

if len(sys.argv)>1 and sys.argv[1]=="--jar":
    import zipfile
    jars=list((ROOT/"build/libs").glob("*.jar"))
    assert len(jars)==1,jars
    with zipfile.ZipFile(jars[0]) as z:
        names=set(z.namelist())
        for entry in REFERENCE["files"]:
            internal=entry["file"].removeprefix("src/main/resources/")
            assert internal in names,internal
            assert hashlib.sha256(z.read(internal)).hexdigest()==entry["sha256"],internal
        assert "br/veilchapter/sound/TreeRitualAudio.class" in names
        assert "br/veilchapter/world/TreeScene.class" in names
        packed=json.loads(z.read("assets/veilchapter/sounds.json"))
        assert set(packed)==set(sounds)
    print("WATCHER_SPATIAL_JAR_PASS: all 15 recorded assets present and intact")
