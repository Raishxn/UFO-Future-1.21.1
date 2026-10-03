# UFO Future 3.0.0-beta.7

## Fixed

- Fixed resources disappearing from ME terminals when Infinity or Infinity Genesis Cells shared the same resource with external storage, such as drawers or a transmutation interface. Cells now advertise the same quantity as AE2 creative cells (2,147,483,647 per resource), preventing count overflow when sources are combined. Extraction remains infinite.
- DMA recipes now render in the in-game GuideME guide, including item quantities, fluid requirements and outputs, energy, and processing time.
- Fixed the translated name of Infinity Cells without a valid resource.
- Added the missing creative auto-build tooltip for the Structure Scanner.
- Fixed guide navigation validation to check duplicate item links separately for each language.

## Added

- Added 21 Simplified Chinese guide pages and localized item links. Thanks to **yongaishide** for contributing PR #24!
- Added regression coverage for mixed Infinity/external storage, duplicate infinite sources, priorities, cell removal/reinsertion, and infinite extraction.

## Maintenance

- Updated Gradle Actions from 6.3.0 to 6.4.0 (PR #23).

## Requirements

- Minecraft **1.21.1**, NeoForge **21.1.x**, Applied Energistics 2 **19.2.17 or compatible 19.x**, and RaishxCore **0.2 or compatible 0.x** (`[0.2, 1.0)`).
- This remains a **beta** release. No recipe or progression changes from beta.6.
