package com.raishxn.ufo.compat.jade;

import com.raishxn.ufo.util.UfoText;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.block.MassiveOutputHatchBlock;
import com.raishxn.ufo.block.entity.MassiveOutputHatchBE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.text.NumberFormat;

/** Authoritative server-side coolant display for the hybrid ME Massive Fluid Hatch. */
@WailaPlugin
public final class UfoJadePlugin implements IWailaPlugin {
    private static final HatchProvider HATCH_PROVIDER = new HatchProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(HATCH_PROVIDER, MassiveOutputHatchBE.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(HATCH_PROVIDER, MassiveOutputHatchBlock.class);
    }

    private static final class HatchProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        private static final ResourceLocation UID = UfoMod.id("massive_fluid_hatch");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public int getDefaultPriority() {
            // Run after Jade's universal fluid provider (1000) so its stale line can be removed.
            return 1100;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof MassiveOutputHatchBE hatch)
                    || !hatch.supportsFluidInput()) return;
            FluidStack fluid = hatch.getStoredCoolant();
            data.putInt("ufoCoolantCapacity", MassiveOutputHatchBE.COOLANT_CAPACITY);
            data.putInt("ufoCoolantAmount", fluid.getAmount());
            if (!fluid.isEmpty()) {
                data.putString("ufoCoolant", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (!data.contains("ufoCoolantCapacity")) return;

            // Jade's generic capability snapshot can be stale for this hybrid hatch.
            // Replace it with the authoritative server payload instead of displaying "Empty".
            tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);
            int amount = data.getInt("ufoCoolantAmount");
            int capacity = data.getInt("ufoCoolantCapacity");
            if (amount <= 0 || !data.contains("ufoCoolant")) {
                tooltip.add(Component.translatable("gui.ufo.supply.no_coolant").withStyle(ChatFormatting.GRAY));
                return;
            }
            ResourceLocation id = ResourceLocation.tryParse(data.getString("ufoCoolant"));
            var fluid = id == null ? null : BuiltInRegistries.FLUID.get(id);
            Component name = fluid == null
                    ? Component.literal(data.getString("ufoCoolant"))
                    : new FluidStack(fluid, 1).getHoverName();
            NumberFormat numbers = NumberFormat.getIntegerInstance();
            tooltip.add(name.copy().withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(" " + numbers.format(amount) + " / "
                            + numbers.format(capacity) + " mB").withStyle(ChatFormatting.WHITE)));
        }
    }
}
