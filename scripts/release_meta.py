#!/usr/bin/env python3
"""Derive a release's name, pre-release flag and notes from a tag.

Tags on the release-candidate track are v0.<major>.<minor>[.<rc>], where a missing
fourth component is RC1, .1 is RC2, and so on: v0.1.0 is "1.0 RC1", v0.1.0.1 is
"1.0 RC2", v0.1.1.1 is "1.1 RC2". Anything not starting at 0 is a final release.

The tag and the CHANGELOG heading share this scheme; VERSION carries the display
name, which is checked against the tag so the two cannot drift apart.
"""
import argparse
import json
import re
import sys
from pathlib import Path

PRODUCT = 'Cherrygram Next'
RC_TAG = re.compile(r'^v?0\.(\d+)\.(\d+)(?:\.(\d+))?$')
FINAL_TAG = re.compile(r'^v?(\d+)\.(\d+)(?:\.(\d+))?$')
HEADING = re.compile(r'^##\s+(\S+)')


def describe(tag: str):
    """Return (display name, is_prerelease) for a tag."""
    m = RC_TAG.match(tag)
    if m:
        major, minor, rc = m.group(1), m.group(2), int(m.group(3) or 0) + 1
        return f'{PRODUCT} v{major}.{minor} RC{rc}', True
    m = FINAL_TAG.match(tag)
    if m:
        major, minor, patch = m.group(1), m.group(2), m.group(3)
        ver = f'{major}.{minor}' + (f'.{patch}' if patch and patch != '0' else '')
        return f'{PRODUCT} v{ver}', False
    raise SystemExit(f'Tag "{tag}" does not match v0.<major>.<minor>[.<rc>] or v<major>.<minor>[.<patch>].')


def notes(changelog: str, tag: str) -> str:
    want = tag.lstrip('v')
    lines, start = changelog.splitlines(), None
    for i, line in enumerate(lines):
        m = HEADING.match(line)
        if not m:
            continue
        if start is None:
            if m.group(1).lstrip('v') == want:
                start = i + 1
        else:
            return '\n'.join(lines[start:i]).strip()
    if start is None:
        raise SystemExit(f'No "## {tag}" section in the changelog.')
    return '\n'.join(lines[start:]).strip()


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument('tag')
    p.add_argument('--changelog', default='CHANGELOG.md')
    p.add_argument('--version-file', default='VERSION')
    p.add_argument('--notes-out')
    p.add_argument('--gradle-properties', default='gradle.properties')
    args = p.parse_args()

    name, prerelease = describe(args.tag)

    declared = Path(args.version_file).read_text(encoding='utf-8').strip()
    if declared != name:
        raise SystemExit(
            f'VERSION says "{declared}" but tag {args.tag} means "{name}". '
            'Update one of them so the release cannot be mislabelled.'
        )

    # The APK carries this as its own version, and the updater compares it, so a
    # release whose tag and build disagree would break update detection.
    want_build = args.tag.lstrip('v')
    props = Path(args.gradle_properties).read_text(encoding='utf-8')
    for line in props.splitlines():
        if line.startswith('APP_VERSION_NAME_CHERRY='):
            have = line.split('=', 1)[1].strip()
            if have != want_build:
                raise SystemExit(
                    f'APP_VERSION_NAME_CHERRY is "{have}" but tag {args.tag} needs "{want_build}".'
                )
            break
    else:
        raise SystemExit('APP_VERSION_NAME_CHERRY is missing from gradle.properties.')

    body = notes(Path(args.changelog).read_text(encoding='utf-8'), args.tag)
    if not body:
        raise SystemExit(f'The "{args.tag}" changelog section is empty.')
    if args.notes_out:
        Path(args.notes_out).write_text(body + '\n', encoding='utf-8')

    json.dump({'name': name, 'prerelease': prerelease, 'tag': args.tag}, sys.stdout)
    sys.stdout.write('\n')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
