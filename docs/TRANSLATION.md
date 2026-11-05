# Translation engine

## Goals

MOM translation should follow the game's selected locale and fill missing mod translations without replacing a mod's existing, intentional wording.

## Resolution priority

1. Translation shipped by the mod for the active locale.
2. User-provided override.
3. MOM-maintained translation bundle.
4. Community translation bundle.
5. Optional automatic-translation provider, if explicitly enabled.
6. Original fallback language / translation key.

The exact ordering between user overrides and official mod strings may be configurable; default behavior should preserve official mod wording unless the user explicitly overrides it.

## Requirements

- Detect locale changes at runtime and refresh resolved catalogs without restarting when the host API permits it.
- Support Minecraft translation keys and JSON language resources.
- Preserve formatting codes, printf-style placeholders, named placeholders, plural/context metadata where available, and rich-text boundaries.
- Validate placeholder/token parity before accepting a translated value.
- Cache by mod id, mod version, source locale, target locale, and translation-bundle revision.
- Never send game text or user data to a remote service unless the user explicitly enables a provider.
- Make automatic translation optional and clearly label machine-generated entries.
- Keep source text and provenance so users can report or override poor translations.

## Current implementation

The first pure-Java slice provides:
- immutable locale catalogs;
- deterministic fallback resolution;
- a conservative validator for common formatting tokens and placeholders.

It does not yet hook Minecraft's language manager, parse mod JAR language assets, fetch translation packs, or perform machine translation. Those are separate integration tasks.
