---
description: Guidelines and architecture rules for developing the internal UFO API
---

# Internal API Usage & Creation — UFO Future

This skill instructs how to architect, develop, and use the internal API for the UFO Future mod. 

To avoid code duplication and future maintenance headaches, complex generalized systems (like Multiblock Scanners and BigInteger AE2 integrations) must be built inside a dedicated API package *before* being implemented in the content blocks.

---

## 1. Package Structure

The project should enforce strict separation of concerns using the following structure:

```
com.raishxn.ufo.api/          <-- THE INTERNAL API
  ├── multiblock/             (Interfaces for multiblock validation, core logic)
  ├── ae/                     (Generic helpers for AE2, BigInteger Hatch Interfaces)
  └── utils/                  (Mathematics and broad shared utilities)

com.raishxn.ufo.content/      <-- THE CONTENT (CONSUMER)
  ├── stellarnexus/           (Specific Blocks, BEs, and Screen implementations)
  └── ...
```

---

## 2. Core Architecture Rules

1. **The API is Blind**: Code inside `com.raishxn.ufo.api` MUST NOT reference any specific items, blocks, or block entities from the content or init packages (e.g., do not import `ModBlocks.STELLAR_NEXUS_CONTROLLER` inside the API).
2. **Interface Driven**: The API should provide interfaces (`IMultiblockController`, `IBigIntegerGridNode`) and abstract generic classes (`AbstractMultiblockScan`) that the content classes will `implements` or `extends`.
3. **AE2 Abstraction**: When dealing with extreme limits (BigInteger outputs for the Stellar Nexus), the API package handles the complex conversion and injection into the ME Grid. The content BlockEntity just passes the integers and asks the API to inject it.
4. **Export Ready**: If you ever decide to separate this API into a standalone `.jar` file, it should require almost zero refactoring because it naturally does not depend on UFO Future's assets.

---

## 3. How to Implement a New System Using the API

When creating a new colossal machine (e.g., the Stellar Nexus), follow this sequence:

### Step 1: Design the API Interfaces
Create the abstract blueprint. For example, if building a big output system:
- File: `api/ae/IBigIntegerOutputHatch.java`
- Method: `void injectMassiveItems(Item item, BigInteger amount);`

### Step 2: Implement into the Content
Create the actual BlockEntity in the content package:
- File: `content/stellarnexus/StellarNexusOutputHatchBE.java`
- The class will `implements IBigIntegerOutputHatch`.
- Now, couple the specific block properties, inventory slots, and textures here over the abstract layer.

### Step 3: Register everything normally
Register the contents using the standard `ModBlocks.java` registries. The API itself usually doesn't register blocks, it only defines the data structures. 

---

## 4. Open Interfaces Required (Roadmap)

For the **Stellar Nexus**, the following API infrastructures need to be built:
- `api/multiblock/MultiblockPattern.java` (To define 7x7x7 structures)
- `api/multiblock/IMultiblockPart.java` (For casings, hatches)
- `api/ae/MassiveNetworkHandler.java` (To interface millions of items into AE2 smoothly)
