---
description: Brainstorming framework for new features and unique mechanics in UFO Future
---

# Brainstorming — UFO Future

This skill provides a structured framework for generating new ideas and unique mechanics for the UFO Future mod. Use it when the user asks for new feature ideas, mechanic concepts, or expansion brainstorms.

---

## Brainstorming Process

### Step 1: Context Check
Before generating ideas, always review the current mod state:
- Read `docs/en/progression.md` for the current crafting tree
- Read `docs/en/dma.md` for machine capabilities
- Check `src/main/java/com/raishxn/ufo/init/` for existing registrations
- Check `src/main/resources/assets/ufo/lang/en_us.json` for all registered items

### Step 2: Identify Gaps
Look for gaps in the mod's progression and gameplay:
- Is there a dead zone between tiers where players have nothing to do?
- Are there mechanics that exist but aren't deeply developed?
- What AE2 systems are NOT yet extended by UFO Future?
- What player fantasies are left unfulfilled?

### Step 3: Generate Ideas Using These Categories

---

## Idea Categories

### 🔧 Machine Expansions
Ideas for new machines or DMA upgrades:

- **DMA Mk II / Mk III**: Upgraded versions with more slots, auto-coolant management, or multi-block structure
- **Stellar Forge**: A separate high-tier machine for processing raw stellar materials with unique mechanics (requires maintaining a "star core" with specific coolant/fuel balance)
- **Entropy Assembler**: The multi-block machine using the existing casing blocks (entropy_assembler_core_casing, entropy_singularity_casing, etc.) — could be a late-game crafting structure that handles recipes too complex for the DMA
- **Matter Replicator**: UU-Matter based item replication system integrated with AE2 autocrafting

### 💾 Storage & Network
Ideas extending AE2's system:

- **Dimensional Storage Network**: Cross-dimension ME network access using spatial technology
- **Quantum Link Cells**: Storage cells that sync contents between two physical locations
- **Auto-Crafting Optimizer**: A block that analyzes crafting chains and suggests optimal catalyst configurations
- **Energy Storage Cells**: Like item/fluid cells but for AE power — store massive AE energy in ME drives
- **ME Teleportation Pad**: Uses spatial IO concepts from AE2 to teleport players between linked pads

### ⚔️ Combat & Survival
Ideas for tools, armor, and combat:

- **Dimensional Rift Sword**: Creates temporary portals on hit that teleport enemies
- **Gravity Gauntlet**: Push/pull entities and blocks using graviton technology
- **Stellar Shield**: An energy barrier that absorbs damage and converts it to AE power
- **UFO Trident**: Transformable tool extension — water/aerial combat with lightning channeling
- **Nanite Swarm**: Deployable entities that auto-mine, auto-farm, or patrol an area
- **Personal Teleporter**: Endgame item that lets you waypoint-teleport using AE power

### 🌍 World Generation & Structures
Ideas for world content:

- **Stellar Fragment Ore**: Rare ores that spawn in the End or deep underground
- **Abandoned DMA Lab**: Structure with loot containing early-game UFO items
- **Neutron Star Biome**: Custom End biome with unique blocks and mob spawns
- **Dimensional Rift Events**: Periodic world events that spawn temporary portals with challenges and rewards

### 🧪 Fluid & Chemistry
Ideas extending the fluid system:

- **Fluid Mixing Chamber**: A machine that creates new fluids by mixing existing ones with specific ratios and temperatures
- **Reactive Fluids**: Fluids that interact with each other when mixed (e.g., Temporal + Spatial = Dimensional Rift fluid)
- **Fluid Infusion**: Apply fluid effects to items (coat a sword in Temporal Fluid for slow-on-hit)
- **Pipeline System**: Visual fluid transport with pressure mechanics

### 🔮 Unique Mechanics
Ideas for systems that no other mod has:

- **Dimensional Instability**: A world mechanic where heavy DMA usage in an area causes dimensional instability, spawning rifts and anomalies — risk vs reward
- **Stellar Evolution System**: Materials that "age" over time — White Dwarf → Neutron Star → Pulsar → Black Hole (time-gated progression with real-time mechanics)
- **Catalyst Overclocking**: Run catalysts beyond their rated tier for massive bonuses but with a chance of permanent destruction
- **Matter Compression**: Compress any block into increasingly dense forms (8 Cobble → Compressed → Double Compressed → ... → Singularity), each tier usable in recipes
- **DMA Network**: Link multiple DMAs together to share catalysts, coolant, and work on recipes cooperatively
- **Blueprint System**: Save DMA recipe configurations as blueprints that can be shared/traded

---

## Evaluation Criteria

Rate each idea on:

| Criterion | Weight | Question |
|-----------|--------|----------|
| **Fun Factor** | 30% | Will players enjoy interacting with this? |
| **Progression Fit** | 25% | Does it fill a gap in the existing crafting tree? |
| **Uniqueness** | 20% | Does any other AE2 addon already do this? |
| **Technical Feasibility** | 15% | Can this be built with current AE2 APIs and NeoForge? |
| **Maintenance Cost** | 10% | How much ongoing work will this create? |

### Red Flags (Reject if):
- ❌ Another popular mod already does this better (e.g., don't recreate Refined Storage)
- ❌ Requires modifying Minecraft core in fragile ways
- ❌ Would make existing content obsolete rather than complementing it
- ❌ No clear connection to the UFO Future theme (stellar/dimensional/matter)
- ❌ Would break balance for survival servers

---

## Output Format

When presenting brainstorm results, use this structure:

```markdown
## 💡 [Feature Name]

**Concept**: One-line description of what this is.

**How it works**: 2-3 sentences explaining the mechanic.

**Tier**: Early/Mid/Late/Endgame

**Dependencies**: What existing UFO Future systems does it interact with?

**Unique Value**: Why is this interesting and why doesn't it exist yet?

**Estimated Complexity**: Simple (1-2 days) / Medium (1 week) / Complex (2+ weeks)
```
