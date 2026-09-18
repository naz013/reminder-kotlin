#!/usr/bin/env python3
"""Finds (and optionally removes) unused string/plurals/string-array resources of a Gradle module.

Usage:
    python scripts/unused_resources.py ui/ui-common          # dry run, lists unused resources
    python scripts/unused_resources.py ui/ui-common --apply  # deletes them from every values*/ locale

A resource counts as used when any file in the repo references it (R.string.x, CommonR.string.x,
@string/x, ...), or when a used array/plurals references it. Definitions inside the module's own
values*/{strings,plurals,arrays}.xml never count as usage on their own.

Static analysis only: names built at runtime (getIdentifier and friends) are not detected, so run
a full build after --apply.
"""
import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFINITION_FILES = ("strings.xml", "plurals.xml", "arrays.xml")
SEARCHED_EXTENSIONS = {".kt", ".java", ".xml", ".kts", ".gradle", ".pro", ".json", ".properties", ".md", ".txt", ".yml", ".yaml"}
SKIPPED_DIRS = {".git", ".gradle", ".idea", ".kotlin", "node_modules", "captures"}

KIND_BY_TAG = {"string": "string", "plurals": "plurals", "string-array": "array", "array": "array", "integer-array": "array"}
TAG_BY_KIND = {"string": "string", "plurals": "plurals", "array": "string-array"}
USAGE_PATTERNS = {
    "string": re.compile(r"(?:\bR\.string\.|@string/|@android:string/|\bstring\.)([A-Za-z0-9_]+)"),
    "plurals": re.compile(r"(?:\bR\.plurals\.|@plurals/|\bplurals\.)([A-Za-z0-9_]+)"),
    "array": re.compile(r"(?:\bR\.array\.|@array/|\barray\.)([A-Za-z0-9_]+)"),
}
XML_REFERENCE = re.compile(r"@(string|plurals|array)/([A-Za-z0-9_]+)")


def definition_paths(res_dir):
    for values_dir in sorted(os.listdir(res_dir)):
        if not values_dir.startswith("values"):
            continue
        for file_name in DEFINITION_FILES:
            path = os.path.join(res_dir, values_dir, file_name)
            if os.path.exists(path):
                yield path


def resource_elements(path):
    for element in ET.parse(path).getroot():
        kind = KIND_BY_TAG.get(element.tag)
        if kind and element.get("name"):
            yield (kind, element.get("name")), element


def repo_text_files():
    for dir_path, dir_names, file_names in os.walk(REPO_ROOT):
        # Only Gradle output is skipped: this repo also has a Kotlin package literally named "build".
        is_gradle_module = any(os.path.exists(os.path.join(dir_path, f)) for f in ("build.gradle.kts", "build.gradle"))
        dir_names[:] = [d for d in dir_names if d not in SKIPPED_DIRS and not (d == "build" and is_gradle_module)]
        for file_name in file_names:
            if os.path.splitext(file_name)[1].lower() in SEARCHED_EXTENSIONS:
                yield os.path.join(dir_path, file_name)


def find_unused(res_dir):
    definitions = set()
    references = {}
    own_definition_files = set()
    for path in definition_paths(res_dir):
        own_definition_files.add(os.path.normcase(path))
        for key, element in resource_elements(path):
            definitions.add(key)
            for match in XML_REFERENCE.finditer(ET.tostring(element, encoding="unicode")):
                target = (match.group(1), match.group(2))
                if target != key:
                    references.setdefault(key, set()).add(target)

    live = set()
    for path in repo_text_files():
        if os.path.normcase(path) in own_definition_files:
            continue
        with open(path, encoding="utf-8", errors="ignore") as file:
            text = file.read()
        for kind, pattern in USAGE_PATTERNS.items():
            live.update(key for key in ((kind, m.group(1)) for m in pattern.finditer(text)) if key in definitions)

    pending = list(live)
    while pending:
        for target in references.get(pending.pop(), ()):
            if target in definitions and target not in live:
                live.add(target)
                pending.append(target)

    return sorted(definitions - live)


def remove_resources(res_dir, unused):
    removed = 0
    for path in definition_paths(res_dir):
        with open(path, "rb") as file:
            text = file.read().decode("utf-8")
        present = {(KIND_BY_TAG[e.tag], e.get("name")) for e in ET.fromstring(text.encode("utf-8")) if e.tag in KIND_BY_TAG}
        for kind, name in unused:
            if (kind, name) not in present:
                continue
            tag = TAG_BY_KIND[kind]
            element = re.compile(
                r"[ \t]*<%s\s+name=\"%s\"(?:\s[^>]*?)?(?:/>|>.*?</%s>)[ \t]*\r?\n(?:[ \t]*\r?\n)?" % (tag, re.escape(name), tag),
                re.S,
            )
            text, count = element.subn("", text)
            if count != 1:
                sys.exit(f"Could not cleanly remove {kind} '{name}' from {path}")
            removed += 1
        with open(path, "wb") as file:
            file.write(text.encode("utf-8"))
    return removed


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("module", help="module path relative to the repo root, e.g. ui/ui-common")
    parser.add_argument("--apply", action="store_true", help="delete the unused resources instead of only listing them")
    args = parser.parse_args()

    res_dir = os.path.join(REPO_ROOT, args.module, "src", "main", "res")
    if not os.path.isdir(res_dir):
        sys.exit(f"No res directory at {res_dir}")

    unused = find_unused(res_dir)
    for kind, name in unused:
        print(f"{kind:8s} {name}")
    print(f"\n{len(unused)} unused resource(s) in {args.module}")

    if args.apply and unused:
        print(f"Removed {remove_resources(res_dir, unused)} element(s) across all locales")


if __name__ == "__main__":
    main()
