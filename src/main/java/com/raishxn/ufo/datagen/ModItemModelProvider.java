package com.raishxn.ufo.datagen;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.fluid.ModFluids;
import com.raishxn.ufo.item.ModArmor;
import com.raishxn.ufo.item.ModCellItems;
import com.raishxn.ufo.item.ModCells;
import com.raishxn.ufo.item.ModItems;
import com.raishxn.ufo.item.ModTools;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraft.world.level.material.Fluid;
import java.util.function.Supplier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.BucketItem;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, UfoMod.MOD_ID, existingFileHelper);
    }

    private static final ResourceLocation UFO_LED_TEXTURE = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/storage_cell_led");
    private static final ResourceLocation GENERATED_PARENT = ResourceLocation.withDefaultNamespace("item/generated");
    @Override
        protected void registerModels() {
        basicItem(ModItems.BISMUTH.get());
        basicItem(ModItems.PHASE_SHIFT_COMPONENT_MATRIX.get());
        basicItem(ModItems.HYPER_DENSE_COMPONENT_MATRIX.get());
        basicItem(ModItems.TESSERACT_COMPONENT_MATRIX.get());
        basicItem(ModItems.EVENT_HORIZON_COMPONENT_MATRIX.get());
        basicItem(ModItems.COSMIC_STRING_COMPONENT_MATRIX.get());
        basicItem(ModItems.DIMENSIONAL_PROCESSOR_PRESS.get());
        basicItem(ModItems.DIMENSIONAL_PROCESSOR.get());
        basicItem(ModItems.PRINTED_DIMENSIONAL_PROCESSOR.get());
        basicItem(ModItems.WHITE_DWARF_FRAGMENT_INGOT.get());
        basicItem(ModItems.WHITE_DWARF_FRAGMENT_ROD.get());
        basicItem(ModItems.WHITE_DWARF_FRAGMENT_DUST.get());
        basicItem(ModItems.WHITE_DWARF_FRAGMENT_NUGGET.get());
        withExistingParent(ModItems.NEUTRON_STAR_FRAGMENT_INGOT.getId().getPath(), GENERATED_PARENT)
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/neutron_star_fragment_ingot"))
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/neutron_star_fragment_ingot_overlay"));
        withExistingParent(ModItems.NEUTRON_STAR_FRAGMENT_NUGGET.getId().getPath(), GENERATED_PARENT)
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/neutron_star_fragment_nugget"))
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/neutron_star_fragment_nugget_overlay"));
        withExistingParent(ModItems.NEUTRON_STAR_FRAGMENT_ROD.getId().getPath(), GENERATED_PARENT)
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/neutron_star_fragment_rod"))
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/neutron_star_fragment_rod_overlay"));
        withExistingParent(ModItems.NEUTRON_STAR_FRAGMENT_DUST.getId().getPath(), GENERATED_PARENT)
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/neutron_star_fragment_dust"))
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/neutron_star_fragment_dust_overlay"));
        basicItem(ModItems.PULSAR_FRAGMENT_INGOT.get());
        basicItem(ModItems.PULSAR_FRAGMENT_DUST.get());
        basicItem(ModItems.PULSAR_FRAGMENT_NUGGET.get());
        basicItem(ModArmor.THERMAL_RESISTOR_PLATING.get());

        // --- Infinity Cells ---
        basicItem(ModCells.INFINITY_WATER_CELL.get());
        basicItem(ModCells.INFINITY_COBBLESTONE_CELL.get());
        basicItem(ModCells.INFINITY_COBBLED_DEEPSLATE_CELL.get());
        basicItem(ModCells.INFINITY_END_STONE_CELL.get());
        basicItem(ModCells.INFINITY_NETHERRACK_CELL.get());
        basicItem(ModCells.INFINITY_SAND_CELL.get());
        basicItem(ModCells.INFINITY_LAVA_CELL.get());
        basicItem(ModCells.INFINITY_SKY_STONE_CELL.get());
        basicItem(ModCells.INFINITY_ANTIMATTER_PELLET_CELL.get());

        // Mekanism
        basicItem(ModCells.INFINITY_PLUTONIUM_PELLET_CELL.get());
        basicItem(ModCells.INFINITY_POLONIUM_PELLET_CELL.get());
        basicItem(ModCells.INFINITY_HDPE_PELLET_CELL.get());

        // Minecraft
        basicItem(ModCells.INFINITY_OBSIDIAN_CELL.get());
        basicItem(ModCells.INFINITY_GRAVEL_CELL.get());
        basicItem(ModCells.INFINITY_OAK_LOG_CELL.get());
        basicItem(ModCells.INFINITY_GLASS_CELL.get());
        basicItem(ModCells.INFINITY_AMETHYST_SHARD_CELL.get());

        // Dyes
        basicItem(ModCells.INFINITY_WHITE_DYE_CELL.get());
        basicItem(ModCells.INFINITY_ORANGE_DYE_CELL.get());
        basicItem(ModCells.INFINITY_MAGENTA_DYE_CELL.get());
        basicItem(ModCells.INFINITY_LIGHT_BLUE_DYE_CELL.get());
        basicItem(ModCells.INFINITY_YELLOW_DYE_CELL.get());
        basicItem(ModCells.INFINITY_LIME_DYE_CELL.get());
        basicItem(ModCells.INFINITY_PINK_DYE_CELL.get());
        basicItem(ModCells.INFINITY_GRAY_DYE_CELL.get());
        basicItem(ModCells.INFINITY_LIGHT_GRAY_DYE_CELL.get());
        basicItem(ModCells.INFINITY_CYAN_DYE_CELL.get());
        basicItem(ModCells.INFINITY_PURPLE_DYE_CELL.get());
        basicItem(ModCells.INFINITY_BLUE_DYE_CELL.get());
        basicItem(ModCells.INFINITY_BROWN_DYE_CELL.get());
        basicItem(ModCells.INFINITY_GREEN_DYE_CELL.get());
        basicItem(ModCells.INFINITY_RED_DYE_CELL.get());
        basicItem(ModCells.INFINITY_BLACK_DYE_CELL.get());
        basicItem(ModCells.INFINITY_GENESIS_CELL.get());

        customParentItem(ModTools.UFO_AXE, "item/ufoset/axe");
        customParentItem(ModTools.UFO_HOE, "item/ufoset/hoe");
       // handheldItem(ModTools.UFO_BOW);
        customParentItem(ModTools.UFO_GREATSWORD, "item/ufoset/greatsword");
        customParentItem(ModTools.UFO_HAMMER, "item/ufoset/hammer");
        customParentItem(ModTools.UFO_SHOVEL, "item/ufoset/shovel");
        customParentItem(ModTools.UFO_SWORD, "item/ufoset/sword");
        customParentItem(ModTools.UFO_PICKAXE, "item/ufoset/pickaxe");
        // Custom fishing rod models with overrides are authored in src/main/resources.
        customParentItem(ModTools.UFO_STAFF, "item/ufoset/staff");
        customParentItem(ModArmor.UFO_HELMET, "item/ufoset/helmet");
        basicItem(ModArmor.UFO_CHESTPLATE.get());
        basicItem(ModArmor.UFO_LEGGINGS.get());
        basicItem(ModArmor.UFO_BOOTS.get());
        basicItem(ModArmor.ASTRAL_NEXUS_HELMET.get());
        basicItem(ModArmor.THERMAL_RESISTOR_BOOTS.get());
        basicItem(ModArmor.THERMAL_RESISTOR_CHEST.get());
        basicItem(ModArmor.THERMAL_RESISTOR_MASK.get());
        basicItem(ModArmor.THERMAL_RESISTOR_PANTS.get());
        basicItem(ModItems.QUANTUM_ANOMALY);
        basicItem(ModItems.NUCLEAR_STAR);
        basicItem(ModItems.SCAR);
        basicItem(ModItems.SCRAP);
        basicItem(ModItems.SCRAP_BOX);

        // --- Esferas / Componentes Avançados ---
        basicItem(ModItems.NEUTRONIUM_SPHERE);
        basicItem(ModItems.ENRICHED_NEUTRONIUM_SPHERE);
        basicItem(ModItems.CHARGED_ENRICHED_NEUTRONIUM_SPHERE);

        // --- Stages de Matter ---
        basicItem(ModItems.PROTO_MATTER);
        basicItem(ModItems.CORPOREAL_MATTER);
        basicItem(ModItems.WHITE_DWARF_MATTER);
        basicItem(ModItems.NEUTRON_STAR_MATTER);
        basicItem(ModItems.PULSAR_MATTER);
        basicItem(ModItems.DARK_MATTER);
        basicItem(ModItems.UU_MATTER_CRYSTAL);

        // --- Catalyst ---
        basicItem(ModItems.CHRONO_CATALYST_T1);
        basicItem(ModItems.CHRONO_CATALYST_T2);
        basicItem(ModItems.CHRONO_CATALYST_T3);
        basicItem(ModItems.MATTERFLOW_CATALYST_T1);
        basicItem(ModItems.MATTERFLOW_CATALYST_T2);
        basicItem(ModItems.MATTERFLOW_CATALYST_T3);
        basicItem(ModItems.OVERFLUX_CATALYST_T1);
        basicItem(ModItems.OVERFLUX_CATALYST_T2);
        basicItem(ModItems.OVERFLUX_CATALYST_T3);
        basicItem(ModItems.QUANTUM_CATALYST_T1);
        basicItem(ModItems.QUANTUM_CATALYST_T2);
        basicItem(ModItems.QUANTUM_CATALYST_T3);
        basicItem(ModItems.DIMENSIONAL_CATALYST);

        // --- Estrutural / Utilitário ---
        basicItem(ModItems.OBSIDIAN_MATRIX);
        basicItem(ModItems.DUST_CRYOTHEUM);
        basicItem(ModItems.DUST_BLIZZ);
        basicItem(ModItems.UNSTABLE_WHITE_HOLE_MATTER);
        basicItem(ModItems.AETHER_CONTAINMENT_CAPSULE);
        basicItem(ModItems.SAFE_CONTAINMENT_MATTER);

            // Housings
            basicItem(ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING.getId());
            basicItem(ModCellItems.NEUTRON_FLUID_CELL_HOUSING.getId());
            basicItem(ModCellItems.PULSAR_CELL_HOUSING.getId());

            // Components
            basicItem(ModCellItems.CELL_COMPONENT_40M.getId());
            basicItem(ModCellItems.CELL_COMPONENT_100M.getId());
            basicItem(ModCellItems.CELL_COMPONENT_250M.getId());
            basicItem(ModCellItems.CELL_COMPONENT_750M.getId());
            basicItem(ModCellItems.CELL_COMPONENT_INFINITY.getId());

            // Item Cells
            cellModel(ModCellItems.ITEM_CELL_40M, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_40M);
            cellModel(ModCellItems.ITEM_CELL_100M, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_100M);
            cellModel(ModCellItems.ITEM_CELL_250M, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_250M);
            cellModel(ModCellItems.ITEM_CELL_750M, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_750M);
            cellModel(ModCellItems.ITEM_CELL_SINGULARITY, ModCellItems.WHITE_DWARF_ITEM_CELL_HOUSING, ModCellItems.CELL_COMPONENT_INFINITY);

            // Fluid Cells
            cellModel(ModCellItems.FLUID_CELL_40M, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_40M);
            cellModel(ModCellItems.FLUID_CELL_100M, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_100M);
            cellModel(ModCellItems.FLUID_CELL_250M, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_250M);
            cellModel(ModCellItems.FLUID_CELL_750M, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_750M);
            cellModel(ModCellItems.FLUID_CELL_SINGULARITY, ModCellItems.NEUTRON_FLUID_CELL_HOUSING, ModCellItems.CELL_COMPONENT_INFINITY);

            // Chemical Cells
            cellModel(ModCellItems.CHEMICAL_CELL_40M, ModCellItems.PULSAR_CELL_HOUSING, ModCellItems.CELL_COMPONENT_40M);
            cellModel(ModCellItems.CHEMICAL_CELL_100M, ModCellItems.PULSAR_CELL_HOUSING, ModCellItems.CELL_COMPONENT_100M);
            cellModel(ModCellItems.CHEMICAL_CELL_250M, ModCellItems.PULSAR_CELL_HOUSING, ModCellItems.CELL_COMPONENT_250M);
            cellModel(ModCellItems.CHEMICAL_CELL_750M, ModCellItems.PULSAR_CELL_HOUSING, ModCellItems.CELL_COMPONENT_750M);
            cellModel(ModCellItems.CHEMICAL_CELL_SINGULARITY, ModCellItems.PULSAR_CELL_HOUSING, ModCellItems.CELL_COMPONENT_INFINITY);


        dynamicBucketItem(ModItems.NEUTRON_STAR_FRAGMENT_BUCKET, ModFluids.SOURCE_NEUTRON_STAR_FRAGMENT_FLUID);
        dynamicBucketItem(ModItems.PULSAR_FRAGMENT_BUCKET, ModFluids.SOURCE_PULSAR_FRAGMENT_FLUID);
        dynamicBucketItem(ModItems.WHITE_DWARF_FRAGMENT_BUCKET, ModFluids.SOURCE_WHITE_DWARF_FRAGMENT_FLUID);
        dynamicBucketItem(ModItems.LIQUID_STARLIGHT_BUCKET, ModFluids.SOURCE_LIQUID_STARLIGHT_FLUID);
        dynamicBucketItem(ModItems.PRIMORDIAL_MATTER_BUCKET, ModFluids.SOURCE_PRIMORDIAL_MATTER_FLUID);
        dynamicBucketItem(ModItems.RAW_STAR_MATTER_PLASMA_BUCKET, ModFluids.SOURCE_RAW_STAR_MATTER_PLASMA_FLUID);
        dynamicBucketItem(ModItems.TRANSCENDING_MATTER_BUCKET, ModFluids.SOURCE_TRANSCENDING_MATTER_FLUID);
        dynamicBucketItem(ModItems.UU_MATTER_BUCKET, ModFluids.SOURCE_UU_MATTER_FLUID);
        dynamicBucketItem(ModItems.UU_AMPLIFIER_BUCKET, ModFluids.SOURCE_UU_AMPLIFIER_FLUID);
        dynamicBucketItem(ModItems.GELID_CRYOTHEUM_BUCKET, ModFluids.SOURCE_GELID_CRYOTHEUM);
        dynamicBucketItem(ModItems.STABLE_COOLANT_BUCKET, ModFluids.SOURCE_STABLE_COOLANT);
        dynamicBucketItem(ModItems.TEMPORAL_FLUID_BUCKET, ModFluids.SOURCE_TEMPORAL_FLUID);
        dynamicBucketItem(ModItems.SPATIAL_FLUID_BUCKET, ModFluids.SOURCE_SPATIAL_FLUID);

    }

    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder customParentItem(DeferredItem<?> item, String parent) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, parent));
    }

    private ItemModelBuilder basicItem(DeferredHolder<Item, ? extends Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/" + item.getId().getPath()));
    }

    private void dynamicBucketItem(DeferredItem<Item> bucket, Supplier<? extends Fluid> fluid) {
        withExistingParent(bucket.getId().getPath(), ResourceLocation.fromNamespaceAndPath("neoforge", "item/bucket"))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(fluid.get());
    }
    private void cellModel(DeferredHolder<Item, ? extends Item> cell, DeferredHolder<Item, ? extends Item> housing, DeferredHolder<Item, ? extends Item> component) {
        withExistingParent(cell.getId().getPath(), ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/" + housing.getId().getPath())) // Camada base: o housing
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/" + component.getId().getPath())) // Camada do meio: o "side"
                .texture("layer2", UFO_LED_TEXTURE);// Camada de cima: o LED do AE2
    }
    private void infinityResourceCellModel(DeferredHolder<Item, ? extends Item> cell) {
        withExistingParent(cell.getId().getPath(), ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/" + cell.getId().getPath())) // Camada base: A textura da própria célula
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "item/" + ModCellItems.CELL_COMPONENT_INFINITY.getId().getPath())) // Camada do meio: O componente infinity
                .texture("layer2", UFO_LED_TEXTURE); // Camada de cima: O LED
    }
}
