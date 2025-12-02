package com.raishxn.ufo.compat.kjs;

import com.raishxn.ufo.UfoMod;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.recipe.component.*; // Importa SizedFluidIngredientComponent
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class UfoKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "dimensional_assembly"), new RecipeSchema(
                // 1. Item Inputs
                IngredientComponent.INGREDIENT.asList().inputKey("item_inputs"),

                // 2. Fluid Input (CORRIGIDO: Usando NESTED)
                SizedFluidIngredientComponent.NESTED.inputKey("fluid_input"),

                // 3. Coolant Input (CORRIGIDO: Usando NESTED)
                SizedFluidIngredientComponent.NESTED.inputKey("coolant_input"),

                // 4. Outputs
                ItemStackComponent.ITEM_STACK.asList().outputKey("outputs"),

                // 5. Energy
                NumberComponent.INT.otherKey("energy").optional(10000),

                // 6. Process Time
                NumberComponent.INT.otherKey("process_time").optional(200),

                // 7. Chances
                NumberComponent.FLOAT.asList().otherKey("chances").optional(List.of(1.0f))
        ));
    }
}