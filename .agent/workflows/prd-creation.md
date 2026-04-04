---
description: Template and guide for creating complete Product Requirement Documents (PRDs) for UFO Future features
---

# PRD Creation — UFO Future

This skill provides a structured process for creating complete Product Requirement Documents for new features in the UFO Future mod. Use it when the user wants to plan a new feature, machine, item system, or mechanic before implementation.

---

## When to Create a PRD

Create a PRD when:
- Adding a new machine or multi-block structure
- Creating a new item system (tool set, armor set, cell series)
- Implementing a new game mechanic (fluid system, world gen, etc.)
- Making changes that affect the progression tree
- Any feature that takes more than 2 hours to implement

---

## PRD Template

```markdown
# PRD: [Feature Name]

## 1. Overview

### 1.1 Summary
[One paragraph describing what this feature is and why it exists]

### 1.2 Goals
- [Goal 1: What player problem does this solve?]
- [Goal 2: What gameplay moment does this create?]
- [Goal 3: How does this improve the mod's progression?]

### 1.3 Non-Goals
- [What this feature explicitly does NOT try to do]

---

## 2. User Stories

### As a survival player...
- I want to [action] so that [benefit]
- I want to [action] so that [benefit]

### As a modpack developer...
- I want to [action] so that [benefit]

### As a server admin...
- I want to [action] so that [benefit]

---

## 3. Design

### 3.1 Progression Placement
- **Tier**: [Early / Mid / Late / Endgame]
- **Prerequisites**: [What must the player have before accessing this?]
- **Unlocks**: [What does completing this enable?]

### 3.2 Core Mechanics
[Detailed description of how the feature works]

### 3.3 Recipe Design
[Item inputs, fluid inputs, energy cost, processing time]
[Crafting requirements and material costs]

### 3.4 GUI Design (if applicable)
[Layout description, slot positions, displays, buttons]

### 3.5 Visual Design
[Textures, particles, animations, sounds]
[Model type: block model, item model, geckolib animated?]

---

## 4. Technical Design

### 4.1 New Classes Required
| Class | Type | Purpose |
|-------|------|---------|
| [ClassName] | BlockEntity | [What it does] |
| [ClassName] | Item | [What it does] |

### 4.2 AE2 Integration Points
- [ ] Network power draw
- [ ] Cell handler
- [ ] JEI category
- [ ] ME connectivity
- [ ] Crafting integration

### 4.3 Mixins Required
[List any AE2 or Minecraft classes that need mixin injection]

### 4.4 Network Packets
[Server→Client and Client→Server data that needs syncing]

### 4.5 Data Generation
[Recipes, loot tables, tags, models, blockstates]

---

## 5. Balance

### 5.1 Energy Costs
| Action | Cost | Justification |
|--------|------|---------------|
| [Action] | [AE amount] | [Why this value] |

### 5.2 Material Costs
[Why are these materials required? How does cost scale with tier?]

### 5.3 Time Investment
[How long should the player spend on this feature?]

### 5.4 Risk/Reward
[What are the failure conditions? What penalties exist?]

---

## 6. Compatibility

### 6.1 Required Mods
- AE2 (always required)
- [Other required mods]

### 6.2 Optional Integration
- Mekanism: [how the feature changes with Mekanism]
- KubeJS: [what can modpack devs customize?]

### 6.3 Known Conflicts
[Any mods that might conflict with this feature]

---

## 7. Localization

### 7.1 New Translation Keys
| Key | en_US | pt_BR |
|-----|-------|-------|
| `item.ufo.xxx` | [English] | [Portuguese] |

---

## 8. Testing Plan

### 8.1 Unit Tests
- [ ] [Test case 1]
- [ ] [Test case 2]

### 8.2 Integration Tests
- [ ] Works with AE2 network
- [ ] JEI recipes display correctly
- [ ] Recipes function in survival mode
- [ ] Works on dedicated server (no client-only code on server)

### 8.3 Balance Tests
- [ ] Resource cost feels fair at the intended progression stage
- [ ] Not exploitable for infinite resources
- [ ] Compatible with automation (hoppers, AE2 buses, etc.)

---

## 9. Open Questions

[List questions that need answers before implementation]

1. ❓ [Question about design decision]
2. ❓ [Question about balance]
3. ❓ [Question about scope]
```

---

## Smart Questions to Ask

When creating a PRD, always consider these critical questions organized by category:

### Progression & Balance
- Where does this feature sit in the progression? Is there a gap before/after it?
- What materials gate access? Are they available at the intended tier?
- Is the energy cost proportional to the output value?
- Does this make any existing content obsolete or redundant?
- Can this be exploited for infinite resources via automation loops?

### Technical Architecture
- Does this require a new BlockEntity, or can it extend an existing one?
- Does this need server↔client sync? What data must be synced?
- Are there AE2 APIs for this, or do we need mixins?
- Will this work on dedicated servers (no client-only imports on server)?
- Does this introduce new chunk-loading requirements?
- How does this interact with the existing DMA recipe system?

### Player Experience
- Is the mechanic intuitive or does it need a tutorial/guide item?
- What feedback does the player receive during the process? (particles, sounds, GUI updates)
- What happens when the player makes a mistake? (wrong items, insufficient power, overheating)
- Is there a satisfying "moment" when this feature completes? (visual effect, sound, achievement)

### Compatibility
- Does Mekanism integration add value here?
- Can modpack devs customize this via KubeJS or config?
- Will this conflict with AdvancedAE, Mega Cells, or ExtendedAE features?
- Does this work with AE2's autocrafting system?

### Maintenance
- How many new textures/models does this need?
- Does this need translation into all 4 wiki languages?
- Will AE2 API updates likely break this feature?
- How much datagen is required?

---

## Example: Quick PRD for a Small Feature

For smaller features, use this condensed format:

```markdown
# Mini PRD: [Feature Name]

**What**: [One sentence]
**Why**: [One sentence]
**Tier**: [Early/Mid/Late/Endgame]
**Inputs**: [Materials needed]
**Output**: [What the player gets]
**Energy**: [AE cost]
**Time**: [Processing time]
**Dependencies**: [AE2 APIs, mixins, etc.]
**Open Questions**: 
1. [Question]
2. [Question]
```
