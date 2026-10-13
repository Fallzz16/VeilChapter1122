#!/usr/bin/env python3
"""Build preflight: check sounds.json, OGG containers, source roles, mono 3D readiness."""
import json, pathlib, subprocess, sys
BASE=pathlib.Path(__file__).resolve().parent.parent
ASSETS=BASE/'src/main/resources/assets/veilchapter'
sounds=json.loads((ASSETS/'sounds.json').read_text(encoding='utf-8'))
expected={'psst':5, 'hey':4, 'laugh':3, 'breath':2}
assert set(sounds)==set(expected), f'SoundEvent mismatch: {list(sounds)}'
count=0
for event,n in expected.items():
    entries=sounds[event]['sounds']
    assert len(entries)==n, f'{event}: expected {n} variants, got {len(entries)}'
    assert sounds[event]['category']=='ambient', event
    for item in entries:
        ident=item['name']
        assert ident.startswith('veilchapter:'), ident
        path=ASSETS/'sounds'/(ident.partition(':')[2]+'.ogg')
        assert path.is_file(), f'Missing OGG: {path}'
        assert path.read_bytes()[:4]==b'OggS', f'Not valid OGG signature: {path}'
        command=['ffprobe','-v','error','-select_streams','a:0',
                 '-show_entries','stream=channels,sample_rate,codec_name:format=duration',
                 '-of','json',str(path)]
        try:
            probe=subprocess.run(command,check=True,capture_output=True,text=True,timeout=15)
        except FileNotFoundError:
            raise RuntimeError('ffprobe is required by audio QA')
        data=json.loads(probe.stdout)
        assert data['streams'][0]['channels']==1, f'3D sound MUST be mono: {path}'
        assert data['streams'][0]['sample_rate']=='48000', f'Wrong sample rate: {path}'
        assert data['streams'][0]['codec_name']=='vorbis', f'Wrong codec: {path}'
        dur=float(data['format']['duration'])
        assert .3 <= dur <= 15, f'Unexpected length {dur}s for {path}'
        count+=1
assert count==14
print('AUDIO_QA_PASS:',count,'separate Vorbis OGG sound files, mono at 48000Hz, every sounds.json ref resolved.')
