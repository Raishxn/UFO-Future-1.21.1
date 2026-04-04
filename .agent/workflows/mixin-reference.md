---
description: Mixin patterns and best practices for NeoForge 1.21.1 used in UFO Future
---

# Mixin Reference — UFO Future

This skill documents mixin patterns, conventions, and techniques used in UFO Future to extend AE2 and NeoForge behavior on Minecraft 1.21.1.

---

## Current Mixins in UFO Future

| Mixin Class | Target | Purpose |
|-------------|--------|---------|
| `AccessorBuiltInModelHooks` | AE2 model system | Access internal model registration hooks |
| `MixinCPUSelectionList` | AE2 crafting CPU GUI | Customize CPU selection list rendering |
| `MixinCraftingCPUCluster` | AE2 crafting CPU | Extend crafting CPU cluster behavior |
| `MixinModelBakery` | Minecraft ModelBakery | Inject custom baked models at load time |

---

## Mixin Configuration

### mixin config file (`src/main/resources/ufo.mixins.json`)

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.raishxn.ufo.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "MixinCraftingCPUCluster",
    "MixinCPUSelectionList"
  ],
  "client": [
    "MixinModelBakery",
    "AccessorBuiltInModelHooks"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

### Key Rules:
- **Server-side mixins** go in `"mixins"` array
- **Client-only mixins** go in `"client"` array
- Always set `"defaultRequire": 1` to fail fast on missing targets
- Use `compatibilityLevel: "JAVA_21"` for NeoForge 1.21.1

---

## Mixin Patterns

### 1. Accessor Mixin (Read private fields/methods)

Use `@Accessor` and `@Invoker` to expose private API:

```java
@Mixin(BuiltInModelHooks.class)
public interface AccessorBuiltInModelHooks {
    @Accessor("MODELS")
    static Map<ResourceLocation, UnbakedModel> getModels() {
        throw new AssertionError();
    }
}
```

**When to use:** Reading or calling private fields/methods without modifying behavior. Safest mixin type.

### 2. Inject Mixin (Add behavior at specific points)

```java
@Mixin(SomeTargetClass.class)
public class MixinSomeTarget {
    
    // Inject at method HEAD (before anything runs)
    @Inject(method = "targetMethod", at = @At("HEAD"), cancellable = true)
    private void ufo$onTargetMethod(CallbackInfo ci) {
        // Custom logic
        if (shouldCancel) {
            ci.cancel(); // Prevents original method from running
        }
    }
    
    // Inject at method RETURN (after method completes)
    @Inject(method = "targetMethod", at = @At("RETURN"))
    private void ufo$afterTargetMethod(CallbackInfo ci) {
        // Post-processing logic
    }
    
    // Inject at method RETURN with return value capture
    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void ufo$modifyReturn(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() * 2);
    }
}
```

### 3. Redirect Mixin (Replace a specific method call)

```java
@Mixin(TargetClass.class)
public class MixinTarget {
    @Redirect(
        method = "process",
        at = @At(value = "INVOKE", target = "Lsome/Class;someMethod()V")
    )
    private void ufo$redirectSomeMethod(SomeClass instance) {
        // Replace the original call with custom logic
        instance.customMethod();
    }
}
```

**When to use:** When you need to change what happens at a specific call site within a method.

### 4. ModifyArg (Change a single argument)

```java
@Mixin(TargetClass.class)
public class MixinTarget {
    @ModifyArg(
        method = "render",
        at = @At(value = "INVOKE", target = "Lsome/Class;draw(IIII)V"),
        index = 2  // Modify the 3rd argument (0-indexed)
    )
    private int ufo$modifyWidth(int originalWidth) {
        return originalWidth + 50;
    }
}
```

### 5. Overwrite (Nuclear option — replaces entire method)

```java
@Mixin(TargetClass.class)
public class MixinTarget {
    @Overwrite
    public void targetMethod() {
        // Completely replaces the original method
        // WARNING: Highly incompatible — use only as last resort
    }
}
```

**⚠️ AVOID unless absolutely necessary.** Overwrites break compatibility with other mods targeting the same method.

---

## Naming Convention

All injected methods MUST use the `ufo$` prefix to avoid naming collisions:

```java
// ✅ Correct
private void ufo$onProcessItem(CallbackInfo ci) { ... }

// ❌ Wrong — will collide with other mods
private void onProcessItem(CallbackInfo ci) { ... }
```

---

## Targeting AE2 Classes

When mixing into AE2 classes, use the full obfuscated-safe target:

```java
// Target AE2 classes by their full package path
@Mixin(targets = "appeng.crafting.CraftingCpuCluster")
// or
@Mixin(CraftingCpuCluster.class)  // If AE2 is on compile classpath
```

### Common AE2 Mixin Targets

| AE2 Class | Common Use Case |
|-----------|----------------|
| `CraftingCpuCluster` | Extend CPU behavior, add mega tier support |
| `CraftingCPUCycler` | Modify CPU selection GUI |
| `DriveModel` | Add custom cell render models |
| `StorageCells` | Extend cell registration |
| `AEBaseBlockEntity` | Add custom tick behavior to AE2 machines |
| `AEBaseScreen` | Modify AE2 GUI rendering |

---

## Debugging Mixins

1. **Enable mixin debug output** in VM args:
   ```
   -Dmixin.debug.export=true
   -Dmixin.debug.verbose=true
   ```
   This dumps transformed classes to `run/.mixin.out/`

2. **Check target method signatures** using AE2 source on GitHub before writing mixins

3. **Test with other AE2 addons** loaded to detect conflicts early

4. **Use `@Inject` over `@Overwrite`** whenever possible for compatibility

---

## Compatibility Checklist

Before adding a new mixin:

- [ ] Is there a public API or event that achieves the same goal? (Prefer APIs over mixins)
- [ ] Does this target a method that other AE2 addons might also target?
- [ ] Is the mixin in the correct array (`mixins` vs `client`) in the JSON config?
- [ ] Am I using the `ufo$` prefix on all injected methods?
- [ ] Have I tested with AdvancedAE, Mega Cells, and ExtendedAE loaded simultaneously?
