#!/usr/bin/env python3
"""Static checks only; NOT a replacement for an Android build."""
from pathlib import Path
import xml.etree.ElementTree as ET
import json,re,sys
root=Path(__file__).resolve().parents[1]
files=list((root/'app/src/main/java').rglob('*.java'))
assert len(files)>=10, 'Missing Java sources'
ET.parse(root/'app/src/main/AndroidManifest.xml')
ET.parse(root/'app/src/main/res/values/styles.xml')
s=json.loads((root/'app/src/main/assets/source_template.json').read_text())
assert s['baseUrl'].startswith('https://')
assert s['pageSelector'] and s['itemSelector']
for f in files:
    text=f.read_text()
    assert text.startswith('package com.qari.kamel;'),f
    assert re.search(r'\bclass\s+\w+',text),f
    # Delimiter balance after removing line comments, strings, and block comments.
    stripped=re.sub(r'/\*.*?\*/|//[^\n]*|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'','',text,flags=re.S)
    for op,cl in ('{}','()','[]'):
        assert stripped.count(op)==stripped.count(cl),(f,op,cl)
print('Static OK:',len(files),'Java files, manifest, styles, JSON. Gradle build NOT verified.')
