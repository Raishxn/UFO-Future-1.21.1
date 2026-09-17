#!/usr/bin/env python3
"""Reports the UFO Future asset provenance tracked in texture-provenance.json.

Fails while any all-rights-reserved asset is still present. Reports how many of the
matched resources are still byte-identical to the audited GTOCore copy.

Usage:
    python3 tools/check-texture-provenance.py
"""

import hashlib
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MANIFEST = os.path.join(ROOT, "docs", "credits", "texture-provenance.json")

def sha256(path):
    with open(path, "rb") as handle:
        return hashlib.sha256(handle.read()).hexdigest()

def main():
    with open(MANIFEST, encoding="utf-8") as handle:
        manifest = json.load(handle)
    resources = manifest["resources"]

    identical = changed = removed = missing = 0
    must_remove_present = []
    rows = []
    for entry in resources:
        path = os.path.join(ROOT, entry["path"])
        if not os.path.isfile(path):
            status = "REMOVED" if entry.get("status") == "REMOVED" else "MISSING"
            removed += 1
        else:
            current = sha256(path)
            if entry["action"] == "remove" and current == entry["sha256"]:
                must_remove_present.append(entry["path"])
            if current == entry["sha256"]:
                status = "IDENTICAL"
                identical += 1
            else:
                status = "CHANGED"
                changed += 1
        rows.append((entry["id"], status, entry["action"], entry["path"]))

    print("UFO Future texture provenance")
    print("  identical to audit : %d" % identical)
    print("  changed            : %d" % changed)
    print("  removed/missing    : %d" % removed)
    print()
    for number, status, action, path in rows:
        if status in ("IDENTICAL", "MISSING") or action == "remove":
            print("  %3d  %-9s %-22s %s" % (number, status, action, path))

    if must_remove_present:
        print()
        print("FAIL: all-rights-reserved assets still present:")
        for path in must_remove_present:
            print("  " + path)
        return 1
    print()
    print("OK: no all-rights-reserved asset remains.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
