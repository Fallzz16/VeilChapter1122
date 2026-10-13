#!/usr/bin/env python3
"""GeckoLib 3.0.31 resource integrity: bone hierarchies, timelines, UV and PNG decoding.
This checks static assets, not client-side visual quality or rendered motion.
"""
from pathlib import Path
import json, math, struct, zlib, sys, zipfile
ROOT=Path(__file__).resolve().parent.parent
ASSETS=ROOT/"src/main/resources/assets/veilchapter"
KINDS={"watcher":(14,3),"peeker":(8,3),"tenant":(17,3),"dragged":(17,4)}
assert "geckolib-forge-1.12.2:3.0.31" in (ROOT/"build.gradle").read_text()
def require(x,message):
    if not x:raise AssertionError(message)
def png(path):
    data=path.read_bytes()
    require(data[:8]==b"\x89PNG\r\n\x1a\n",f"PNG header {path}")
    offset=8; idat=[]; dim=None; count=0
    while offset<len(data):
        length=struct.unpack_from(">I",data,offset)[0]
        kind=data[offset+4:offset+8]
        blob=data[offset+8:offset+8+length]
        crc=struct.unpack_from(">I",data,offset+8+length)[0]
        require(zlib.crc32(kind+blob)&0xffffffff==crc,f"CRC: {path}")
        if kind==b"IHDR":
            dim=struct.unpack_from(">II",blob,0)
            require(dim==(128,128),f"texture 128x128: {path}")
            require(blob[8:10]==b"\x08\x06",f"8-bit RGBA: {path}")
        if kind==b"IDAT": idat.append(blob)
        offset+=12+length
        count+=1
        if kind==b"IEND":break
    decoded=zlib.decompress(b"".join(idat))
    require(len(decoded)==128*(1+128*4),f"decoded RGBA rows: {path}")
    return count
for name,(min_bones,min_clips) in KINDS.items():
    model_path=ASSETS/"geo"/(name+".geo.json")
    anim_path=ASSETS/"animations"/(name+".animation.json")
    texture=ASSETS/"textures/entity"/(name+"_geo.png")
    structure=json.loads(model_path.read_text())
    groups=structure.get("minecraft:geometry",[])
    require(len(groups)==1,f"geometry inventory: {name}")
    info=groups[0]
    require(info["description"]["texture_width"]==128 and info["description"]["texture_height"]==128,name)
    bones=info["bones"]
    require(len(bones)>=min_bones,f"bone count: {name}")
    names={bone["name"] for bone in bones}
    require(len(names)==len(bones),f"duplicate bones: {name}")
    require("root" in names,f"missing root: {name}")
    for bone in bones:
        require(bone.get("parent") in (None, *names),f"orphan parent: {name}/{bone['name']}")
        for cube in bone.get("cubes",[]):
            require(len(cube["origin"])==3 and len(cube["size"])==3,name)
            require(all(math.isfinite(float(v)) and float(v)>0 for v in cube["size"]),name)
            require(len(cube["uv"])==2 and all(0<=v<128 for v in cube["uv"]),name)
    for bone in bones:
        seen=set(); node=bone
        while node.get("parent"):
            require(node["name"] not in seen,f"bone hierarchy cycle: {name}")
            seen.add(node["name"])
            node=next(b for b in bones if b["name"]==node["parent"])
    clips=json.loads(anim_path.read_text())["animations"]
    require(len(clips)>=min_clips,f"animation count: {name}")
    for title,clip in clips.items():
        require(title.startswith("animation."+name+"."),f"unexpected clip id {title}")
        duration=clip["animation_length"]
        require(0<duration<=10,f"clip duration {title}")
        require(clip["bones"],f"empty animation {title}")
        for bone,tracks in clip["bones"].items():
            require(bone in names,f"animation bone missing {title}/{bone}")
            for prop,frames in tracks.items():
                require(prop in {"position","rotation","scale"},f"unsupported animation track {prop}")
                keys=[float(t) for t in frames.keys()]
                require(min(keys)>=0 and max(keys)<=duration+.01,f"frame out of range {title}")
                require(all(len(v)==3 and all(math.isfinite(float(t)) for t in v) for v in frames.values()),f"malformed keyframe {title}")
    png(texture)
# One more geometry variant: eyes only; the invisible skull pivot is important to GeckoLib track resolution.
eyes=json.loads((ASSETS/"geo/peeker_eyes.geo.json").read_text())["minecraft:geometry"][0]["bones"]
assert {b["name"] for b in eyes}>= {"root","skull","left_eye","right_eye","left_pupil","right_pupil"}
if "--jar" in sys.argv:
    jar=next((ROOT/"build/libs").glob("*.jar"))
    with zipfile.ZipFile(jar) as z:
        for name in KINDS:
            for relative in [f"geo/{name}.geo.json",f"animations/{name}.animation.json",f"textures/entity/{name}_geo.png"]:
                member="assets/veilchapter/"+relative
                require(member in z.namelist(),f"JAR asset missing {member}")
                require(z.read(member)==(ASSETS/relative).read_bytes(),f"JAR asset mismatch {member}")
        require("assets/veilchapter/geo/peeker_eyes.geo.json" in z.namelist(),"eyes-only variant missing")
        require("br/veilchapter/client/GeoWatcherRenderer.class" in z.namelist(),"Watcher Geo renderer absent")
        require("br/veilchapter/client/GeoPeekerRenderer.class" in z.namelist(),"Peeker Geo renderer absent")
        require("br/veilchapter/client/GeoTenantRenderer.class" in z.namelist(),"Tenant Geo renderer absent")
        require("br/veilchapter/client/GeoDraggedRenderer.class" in z.namelist(),"Dragged Geo renderer absent")
print("GECKO_RIGS_QA_PASS: 4 custom 3D rigs, 128px decoded RGBA skins, keyframes, parents, JAR resources")
