package com.raishxn.ufo.fluid;

import com.raishxn.ufo.UfoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, UfoMod.MOD_ID);

    /**
     * Método auxiliar para registrar tipos de fluido básicos.
     * @param name Nome de registro do fluido
     * @param textureName Nome do arquivo de textura
     * @param density Densidade (água = 1000, lava = 3000)
     * @param temp Temperatura (água = 300, lava = 1300)
     * @param lightLevel Nível de luz emitido (0 a 15)
     */
    private static Supplier<FluidType> registerBasic(String name, String textureName, int density, int temp, int lightLevel) {
        ResourceLocation textureRL = ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "block/fluid/" + textureName);
        return FLUID_TYPES.register(name, () -> new BaseFluidType(textureRL, textureRL, null,
                0xFFFFFFFF,
                new Vector3f(1f, 1f, 1f),
                FluidType.Properties.create()
                        .lightLevel(lightLevel)
                        .density(density)
                        .viscosity(density * 2)
                        .temperature(temp)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
        ));
    }

    // --- REGISTROS DOS TIPOS DE FLUIDO ---

    public static final Supplier<FluidType> NEUTRON_STAR_FRAGMENT_FLUID_TYPE = registerBasic(
            "neutron_star_fragment_fluid", "neutron_star_fragment", 20000, 5000, 15);

    public static final Supplier<FluidType> PULSAR_FRAGMENT_FLUID_TYPE = registerBasic(
            "pulsar_fragment_fluid", "pulsar_fragment", 25000, 6000, 15);

    public static final Supplier<FluidType> WHITE_DWARF_FRAGMENT_FLUID_TYPE = registerBasic(
            "white_dwarf_fragment_fluid", "white_dwarf_fragment", 10000, 3000, 12);

    public static final Supplier<FluidType> LIQUID_STARLIGHT_FLUID_TYPE = registerBasic(
            "liquid_starlight_fluid", "liquid_starlight", 500, 100, 15);

    public static final Supplier<FluidType> PRIMORDIAL_MATTER_FLUID_TYPE = registerBasic(
            "primordial_matter_fluid", "primordialmatter", 5000, 1000, 8);

    public static final Supplier<FluidType> RAW_STAR_MATTER_PLASMA_FLUID_TYPE = registerBasic(
            "raw_star_matter_plasma_fluid", "raw_star_matter_plasma", 3000, 10000, 15);

    public static final Supplier<FluidType> TRANSCENDING_MATTER_FLUID_TYPE = registerBasic(
            "transcending_matter_fluid", "transcending_matter", 1000, 300, 10);

    public static final Supplier<FluidType> UU_MATTER_FLUID_TYPE = registerBasic(
            "uu_matter_fluid", "uu_matter", 3000, 300, 5);

    public static final Supplier<FluidType> UU_AMPLIFIER_FLUID_TYPE = registerBasic(
            "uu_amplifier_fluid", "uuamplifier", 2000, 300, 3);

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}