package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.block.entity.StellarNexusControllerBE;
import com.raishxn.ufo.network.MachinePacketGuard;
import com.raishxn.ufo.recipe.StellarSimulationRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketChangeStellarRecipe(BlockPos pos, ResourceLocation recipeId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketChangeStellarRecipe> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("ufo", "change_stellar_recipe"));

    public static final StreamCodec<FriendlyByteBuf, PacketChangeStellarRecipe> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketChangeStellarRecipe::pos,
            ResourceLocation.STREAM_CODEC, PacketChangeStellarRecipe::recipeId,
            PacketChangeStellarRecipe::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            StellarNexusControllerBE controller = MachinePacketGuard.requireStellar(
                    context, pos, MachinePacketGuard.Action.CHANGE_RECIPE);
            if (controller == null || controller.getLevel() == null) {
                return;
            }

            var recipe = controller.getLevel().getRecipeManager().byKey(recipeId);
            if (recipe.isPresent() && recipe.get().value() instanceof StellarSimulationRecipe) {
                controller.setActiveRecipe(recipeId);
            }
        });
    }
}
