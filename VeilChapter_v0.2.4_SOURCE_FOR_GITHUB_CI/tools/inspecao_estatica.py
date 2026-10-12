#!/usr/bin/env python3
"""Offline release preflight. No Minecraft/Forge dependencies required.
Verifies consistency, not compilation or gameplay correctness.
"""
import json
import struct
from pathlib import Path

root = Path(__file__).resolve().parent.parent
checks = 0

def require(condition, name):
    global checks
    checks += 1
    if not condition:
        raise AssertionError(name)

build = (root/'build.gradle').read_text(encoding='utf-8')
settings = (root/'settings.gradle').read_text(encoding='utf-8')
mod = (root/'src/main/java/br/veilchapter/VeilMod.java').read_text(encoding='utf-8')
events = (root/'src/main/java/br/veilchapter/GameEvents.java').read_text(encoding='utf-8')
common = (root/'src/main/java/br/veilchapter/CommonProxy.java').read_text(encoding='utf-8')
client = (root/'src/main/java/br/veilchapter/client/ClientProxy.java').read_text(encoding='utf-8')
handler = (root/'src/main/java/br/veilchapter/network/DayStateHandler.java').read_text(encoding='utf-8')
require("ForgeGradle:5.1.74" in build, 'pinned ForgeGradle version')
require('1.12.2-14.23.5.2859' in build, 'pinned Minecraft Forge version')
require("maven.minecraftforge.net" in build, 'official Forge Maven')
require('nexus.velocitypowered.com' not in build, 'no failing Velocity Nexus dependency')
require('3.0.140' not in build, 'no obsolete FG3 migration')
require('DayStateHandler.class' in mod, 'packet registration from common entry point')
require('registerMessage(' not in client, 'not registered on client side only')
require('Minecraft.getMinecraft()' not in common, 'common proxy must not load Minecraft client')
require('acceptDayState' in handler, 'packet dispatched through sided proxy')
require('dimensionUnavailable' in events, 'dimension ID collision guard')
require('anomalies.onPlayerBreak(ev.getPos()' in events, 'tracked anomalies removed after replacement')
meta = json.loads((root/'src/main/resources/mcmod.info').read_text(encoding='utf-8'))
require(isinstance(meta, list) and meta[0]['modid']=='veilchapter', 'mcmod.info modid')
sounds = json.loads((root/'src/main/resources/assets/veilchapter/sounds.json').read_text(encoding='utf-8'))
require('psst' in sounds, 'registered sound JSON')
png = (root/'src/main/resources/assets/veilchapter/textures/entity/watcher.png').read_bytes()
require(png[:8] == b'\x89PNG\r\n\x1a\n', 'PNG signature')
require(len(png)>=24 and struct.unpack('>II',png[16:24]) == (64,64), 'watcher PNG size 64 x 64')
ogg = (root/'src/main/resources/assets/veilchapter/sounds/psst.ogg').read_bytes()
require(ogg[:4] == b'OggS' and len(ogg)>100, 'Ogg sound file')
require((root/'.github/workflows/forge-1122-ci.yml').exists(), 'CI build workflow')
require((root/'tools/preparar_mdk.sh').exists(), 'official MDK bootstrapper')
javafiles=list((root/'src/main/java').rglob('*.java'))
require(len(javafiles)>=20, 'project source inventory')
print(f'PASS: {checks} static and resource checks (no Forge build performed)')
