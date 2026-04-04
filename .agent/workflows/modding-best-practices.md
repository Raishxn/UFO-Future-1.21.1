---
description: NeoForge 1.21.1 modding best practices and coding standards for UFO Future
---

# Modding Best Practices — UFO Future

This skill documents coding standards, architectural patterns, and best practices for developing the UFO Future mod on NeoForge 1.21.1.

---

## Project Structure

```
src/main/java/com/raishxn/ufo/
├── UfoMod.java              # Main mod class, event bus registration
├── UfoModClient.java         # Client-side initialization
├── Config.java               # Server/common config
├── UFOConfig.java            # Mod-specific config values
├── block/                    # Block classes and block entities
├── client/                   # Client renderers, screens, HUD
├── compat/                   # Mod compatibility (JEI, AppFlux)
├── core/                     # Core enums and constants (tiers, etc.)
├── datagen/                  # Data generation providers
├── event/                    # Event handlers
├── fluid/                    # Custom fluid types and blocks
├── init/                     # Registration classes (items, blocks, fluids, menus)
├── item/                     # Item classes (tools, armor, cells, catalysts)
├── menu/                     # Container menus for GUIs
├── mixin/                    # Mixin classes
├── network/                  # Network packets (client↔server sync)
├── recipe/                   # Custom recipe types and serializers
├── registry/                 # Deferred register holders
└── util/                     # Utility classes
```

---

## Registration Best Practices

### Use DeferredRegister

```java
public class ModItems {
    public static final DeferredRegister.Items ITEMS = 
        DeferredRegister.createItems(UfoMod.MOD_ID);
    
    public static final DeferredItem<Item> OBSIDIAN_MATRIX = 
        ITEMS.registerSimpleItem("obsidian_matrix");
    
    // Register to mod bus in main mod class
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
```

### Registration Order
1. **Blocks** → 2. **Items** (including BlockItems) → 3. **Fluids** → 4. **Block Entities** → 5. **Menus** → 6. **Recipe Types** → 7. **Creative Tabs**

---

## Coding Standards

### Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Classes | PascalCase | `DimensionalMatterAssembler` |
| Methods | camelCase | `getHeatMultiplier()` |
| Constants | UPPER_SNAKE | `MAX_HEAT_CAPACITY` |
| Registry IDs | snake_case | `"dimensional_matter_assembler"` |
| Translation keys | dot.separated | `"block.ufo.dimensional_matter_assembler"` |
| Texture paths | snake_case | `textures/item/white_dwarf_fragment_ingot.png` |

### Code Organization Rules

1. **One class = one responsibility**. Don't put cell logic in tool classes.
2. **Constants in enums or dedicated classes** (like `MegaCraftingStorageTier`), not magic numbers.
3. **All user-facing strings must use translation keys** — never hardcode English text.
4. **Separate client and server logic** — use `@OnlyIn(Dist.CLIENT)` or put in `client/` package.
5. **Use `Component.translatable()` instead of `Component.literal()`** for any player-visible text.

---

## Data Generation

Always use datagen for:
- ✅ Recipes (shaped, shapeless, DMA)
- ✅ Loot tables
- ✅ Block/item models
- ✅ Blockstate definitions
- ✅ Tags (item, block, fluid)
- ✅ Language files (en_us.json base)

**Never** hand-write JSON that can be datagen'd.

```java
public class ModRecipeProvider extends RecipeProvider {
    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Use builder pattern
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OBSIDIAN_MATRIX)
            .pattern("OOO").pattern("OEO").pattern("OOO")
            .define('O', Items.OBSIDIAN)
            .define('E', Items.ENDER_EYE)
            .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
            .save(output);
    }
}
```

---

## Network Packets

### Server→Client Sync Pattern

```java
public record SyncHeatPacket(BlockPos pos, int heat, int maxHeat) 
    implements CustomPacketPayload {
    
    public static final Type<SyncHeatPacket> TYPE = 
        new Type<>(ResourceLocation.fromNamespaceAndPath("ufo", "sync_heat"));
    
    public static final StreamCodec<FriendlyByteBuf, SyncHeatPacket> CODEC = 
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncHeatPacket::pos,
            ByteBufCodecs.INT, SyncHeatPacket::heat,
            ByteBufCodecs.INT, SyncHeatPacket::maxHeat,
            SyncHeatPacket::new
        );
    
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
```

### Rules:
- **Always validate data on the server side** — never trust client packets
- **Minimize packet size** — send only changed values, not entire state
- **Use `StreamCodec`** for serialization (NeoForge 1.21.1 pattern)

---

## Performance

1. **Tick throttling**: Don't process every tick if not needed. Use `tickCounter % 20 == 0` for per-second logic.
2. **Lazy computation**: Cache recipe lookups, don't search every tick.
3. **Chunk loading**: Don't force-load chunks. Use `Level.isLoaded(pos)` before accessing.
4. **Client rendering**: Use `shouldRenderOffScreen()` wisely for block entity renderers.
5. **Recipe caching**: Store the current recipe match and only re-check when inventory changes.

---

## Error Handling

```java
// ✅ Good — graceful degradation
@Nullable
public DMARecipe findRecipe() {
    try {
        return level.getRecipeManager()
            .getRecipeFor(ModRecipeTypes.DMA_TYPE.get(), container, level)
            .orElse(null);
    } catch (Exception e) {
        LOGGER.warn("Failed to find DMA recipe", e);
        return null;
    }
}

// ❌ Bad — crashes the game
public DMARecipe findRecipe() {
    return level.getRecipeManager()
        .getRecipeFor(ModRecipeTypes.DMA_TYPE.get(), container, level)
        .orElseThrow(); // DON'T DO THIS
}
```

---

## Testing Checklist

Before any commit:
- [ ] `gradlew build` passes without errors
- [ ] Game launches in dev environment (`gradlew runClient`)
- [ ] New items/blocks appear in creative tab
- [ ] Recipes show in JEI
- [ ] Translations load correctly (check pt_BR and zh_CN)
- [ ] No `NullPointerException` in server log
- [ ] Works with AE2, AE2 Addon Lib, and Mekanism loaded
- [ ] Datagen runs cleanly (`gradlew runData`)
