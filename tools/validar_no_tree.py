#!/usr/bin/env python3
"""Verifies complete removal of active ritual, and keeps legacy recovery."""
import json, pathlib, sys, zipfile
root=pathlib.Path(__file__).resolve().parent.parent
src=root/"src/main/java/br/veilchapter"
read=lambda p:(src/p).read_text(encoding="utf-8")
mod=read("VeilMod.java"); ev=read("GameEvents.java"); old=read("state/StoryData.java")
def require(condition,msg):
    assert condition,msg
require("TreeScene" not in ev,"unexpected tree trigger")
require("enableTreeSequence" not in read("config/VeilConfig.java"),"legacy config still active")
require("legacyDimensionUnavailable" in ev,"migration collision check removed")
require("recovery.rescue" in ev,"older dimension needs player recovery")
require("EntityTenant.class" in mod,"tenant registered and available")
require("retireLegacyTree()" in old,"old checkpoint cleanup absent")
require("CLEA" not in read("client/ClientEvents.java"),"cinematic still manipulates client")
require("VEIL_NO_TREE_CHAPTER1_READY" in mod,"server smoke marker absent")
require("TreeRitualAudio" not in read("VeilDebugCommands.java"),"old voice command")
require("tree.request" not in ev and "tree.enter" not in ev,"unexpected ritual start")
sounds=json.loads((root/"src/main/resources/assets/veilchapter/sounds.json").read_text())
require(set(sounds)=={"psst","hey","laugh","breath"},"retired sounds still registered")
deleted=["world/TreeScene.java","core/TreeCinematicTimeline.java",
"core/TreeRitualPacing.java","core/TreeMath.java","sound/TreeRitualAudio.java",
"client/ClearingAmbientSound.java","client/ClientCinematicState.java",
"network/CinematicStatePacket.java","network/CinematicStateHandler.java"]
for p in deleted:require(not (src/p).exists(),"obsolete class remains: "+p)
if "--jar" in sys.argv:
    jar=next((root/"build/libs").glob("*.jar"))
    with zipfile.ZipFile(jar) as z:
        names=set(z.namelist())
        for p in ["br/veilchapter/world/TreeScene.class","br/veilchapter/network/CinematicStatePacket.class",
                  "br/veilchapter/core/TreeRitualPacing.class"]:
            require(p not in names,"obsolete compiled class: "+p)
        require("br/veilchapter/world/LegacyClearingRecovery.class" in names,"missing migration")
        require(set(json.loads(z.read("assets/veilchapter/sounds.json")))==set(sounds),"JAR audio events")
print("NO_TREE_CHAPTER1_QA_PASS: gameplay, sounds and compiled classes retired; legacy save recovery kept")
