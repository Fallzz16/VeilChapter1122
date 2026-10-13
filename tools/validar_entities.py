#!/usr/bin/env python3
"""Independent v0.3.0 asset and entity registration preflight (stdlib only)."""
from pathlib import Path
import hashlib,struct,sys,zipfile
root=Path(__file__).resolve().parents[1]
asset=root/"src/main/resources/assets/veilchapter"
tenant=asset/"textures/entity/tenant.png"
blob=tenant.read_bytes()
assert blob.startswith(b"\x89PNG\r\n\x1a\n")
assert struct.unpack(">II",blob[16:24])==(64,64)
assert hashlib.sha256(blob).hexdigest()=="caf308fc0b2e85442a9ec629ad9a80e5caed56e8ebf97909276169aec2bab931"
for loc,words in [("en_us",["The Watcher","The Peeker","The Tenant"]),
                   ("pt_br",["O Observador","A Fresta","O Inquilino"])]:
    text=(asset/"lang"/(loc+".lang")).read_text(encoding="utf-8")
    assert all(word in text for word in words),loc
mod=(root/"src/main/java/br/veilchapter/VeilMod.java").read_text()
assert '"watcher", 0, this' in mod
assert '"peeker", 1, this' in mod
assert '"tenant", 2, this' in mod
assert "TreeScene" not in mod
for name in ["EntityWatcher","EntityPeeker","EntityTenant"]:
    assert (root/"src/main/java/br/veilchapter/entity"/(name+".java")).exists(),name
for name in ["HorrorDirector","TenantEncounterSystem"]:
    assert (root/"src/main/java/br/veilchapter/system"/(name+".java")).exists(),name
if "--jar" in sys.argv:
    jar=next((root/"build/libs").glob("*.jar"))
    with zipfile.ZipFile(jar) as z:
        names=set(z.namelist())
        for entry in ["assets/veilchapter/textures/entity/tenant.png",
            "assets/veilchapter/lang/pt_br.lang","assets/veilchapter/lang/en_us.lang",
            "br/veilchapter/entity/EntityTenant.class",
            "br/veilchapter/client/ModelTenant.class",
            "br/veilchapter/client/ModelWatcher.class",
            "br/veilchapter/system/HorrorDirector.class"]:
            assert entry in names,entry
        assert z.read("assets/veilchapter/textures/entity/tenant.png")==blob
        assert z.testzip() is None
print("ENTITIES_ASSETS_QA_PASS: three registered IDs stable, original 64x64 skin, 2 locales, director and model classes")
