---
description: Reference guide for AE2 ecosystem APIs (AE2, AdvancedAE, OmniCell, AE2AddonLib, ExtendedAE) used in UFO Future
---

# AE2 Ecosystem Reference — UFO Future

This skill provides patterns and API references for integrating with the Applied Energistics 2 ecosystem in the UFO Future mod on NeoForge 1.21.1.

---

## Dependencies Overview

| Mod | Purpose in UFO Future | Gradle ID |
|-----|----------------------|-----------|
| **AE2** | Core API — cells, network, power, GUI, recipes | `curse.maven:applied-energistics-2-223794` |
| **AE2 Addon Lib** | Helper library for addon registration, cell handlers | `curse.maven:ae2addonlib-1366135` |
| **AdvancedAE** | Reference for advanced machine patterns, multi-block | `curse.maven:advancedae-1084104` (localRuntime) |
| **Mega Cells** | Reference for BigInteger cells, custom cell handlers | `curse.maven:mega-cells-622112` |
| **Applied Flux** | Reference for RF↔AE energy bridge patterns | `curse.maven:applied-flux-965012` |
| **Ex Pattern Provider** | Reference for extended crafting patterns | `curse.maven:ex-pattern-provider-892005` |

---

## Key AE2 APIs and Patterns

### 1. Custom Storage Cells

When creating new storage cells (like White Dwarf / Neutron Star cells):

**Cell Handler Registration** (via AE2 Addon Lib):
```java
// In your mod initializer
StorageCells.addCellHandler(new MyCustomCellHandler());
```

**Custom Cell Handler Pattern:**
```java
public class UfoCellHandler implements ICellHandler {
    @Override
    public boolean isCell(ItemStack is) {
        return is.getItem() instanceof MyBasicStorageCell;
    }

    @Override
    @Nullable
    public StorageCell getCellInventory(ItemStack is, ISaveProvider host) {
        return new MyCustomCellInventory(is, host);
    }
}
```

**BigInteger Cell Inventory:**
- Override `getAvailableStacks()` to return BigInteger quantities
- Use `BigInteger` for internal byte counting instead of `long`
- Register cell models for ME Drive rendering via `DriveModel`

### 2. Custom Machine (AE2 Network Block Entity)

The DMA pattern for creating AE2-powered machines:

```java
public class DMABlockEntity extends AENetworkBlockEntity 
    implements IGridTickable, IConfigurableObject {
    
    // Power management
    private final IEnergyService energyService;
    
    // Inventory management
    private final AppEngInternalInventory inputInventory;
    private final GenericStackInv fluidInventory;
    
    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, !isWorking());
    }
    
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        // Process recipes, consume power, manage heat
        return TickRateModulation.SAME;
    }
}
```

### 3. AE2 Power Integration

```java
// Drawing power from ME network
double powerNeeded = recipe.getEnergy() / (double) recipe.getTime();
double extracted = energyService.extractAEPower(powerNeeded, Actionable.MODULATE, PowerMultiplier.ONE);

// Internal buffer management
private double internalBuffer = 0;
private double maxBuffer = 500_000; // Base 500K AE
```

### 4. JEI Recipe Category

```java
public class DMARecipeCategory extends AbstractRecipeCategory<DMARecipe> {
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DMARecipe recipe, IFocusGroup focuses) {
        // Map item inputs to slots
        for (int i = 0; i < recipe.getItemInputs().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .addIngredients(recipe.getItemInputs().get(i).ingredient());
        }
    }
}
```

### 5. ME Drive Cell Rendering

```java
// Register custom cell models for drive rendering
public static void registerCellModels() {
    DriveModel.registerBakedModel("ufo:white_dwarf_cell", 
        new ResourceLocation("ufo", "block/drive/cells/white_dwarf_cell"));
}
```

### 6. Upgrade Card System (Catalysts)

AE2-style upgrade cards for the DMA:
```java
public class CatalystItem extends AEBaseItem implements IUpgradeInventory {
    private final CatalystFamily family;
    private final int tier; // 1-3
    private final double speedMult;
    private final double energyMult;
    private final double heatMult;
}
```

---

## Common Pitfalls

1. **Cell registration timing**: Register cell handlers in `FMLCommonSetupEvent`, not in constructors
2. **Network thread safety**: AE2 grid operations run on the network thread — don't modify block state directly
3. **Power units**: AE2 uses its own AE unit; 1 AE ≈ 2 FE/RF. Use `PowerMultiplier` for conversion
4. **Inventory sync**: Use `IConfigManagerListener` to sync config between server and client
5. **Recipe registration**: Custom recipe types must be registered via `RegisterEvent` for `RecipeSerializer`
6. **ResourceLocation namespace**: Always use `"ufo"` namespace — never mix with `"ae2"` for your own assets
7. **Addon Lib version lock**: `ae2addonlib` versions are tied to specific AE2 versions — always match

---

## Source Code References

When implementing new AE2 features, check these files in the project:

| Feature | File Path |
|---------|-----------|
| Cell Handler | `src/main/java/com/raishxn/ufo/item/` |
| DMA Machine | `src/main/java/com/raishxn/ufo/block/` |
| DMA Menu/GUI | `src/main/java/com/raishxn/ufo/menu/` |
| JEI Compat | `src/main/java/com/raishxn/ufo/compat/jei/` |
| Recipes | `src/main/java/com/raishxn/ufo/recipe/` |
| Registration | `src/main/java/com/raishxn/ufo/init/` |
| Mega Storage Tiers | `src/main/java/com/raishxn/ufo/core/` |
| Mixins | `src/main/java/com/raishxn/ufo/mixin/` |

---

## Useful AE2 Source Repos for Reference

- **AE2**: `github.com/AppliedEnergistics/Applied-Energistics-2`
- **Mega Cells**: `github.com/62832/MEGACells`
- **AdvancedAE**: `github.com/pedroksl/AdvancedAE`
- **AE2 Addon Lib**: Check CurseForge or Modrinth for source
- **ExtendedAE**: `github.com/gri3229/ExtendedAE`
