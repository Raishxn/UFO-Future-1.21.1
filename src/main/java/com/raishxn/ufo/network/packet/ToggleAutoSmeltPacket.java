package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.datagen.ModDataComponents;
import com.raishxn.ufo.item.custom.HammerItem;
import com.raishxn.ufo.item.custom.UfoEnergyPickaxeItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleAutoSmeltPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleAutoSmeltPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(UfoMod.MOD_ID, "toggle_auto_smelt"));

    // Codec vazio pois não precisamos enviar dados extras, apenas o "sinal"
    public static final StreamCodec<ByteBuf, ToggleAutoSmeltPacket> STREAM_CODEC =
            StreamCodec.unit(new ToggleAutoSmeltPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleAutoSmeltPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack stack = player.getMainHandItem();

                // Verifica se é uma Picareta UFO ou um Martelo UFO
                if (stack.getItem() instanceof UfoEnergyPickaxeItem || stack.getItem() instanceof HammerItem) {
                    boolean currentStatus = stack.getOrDefault(ModDataComponents.AUTO_SMELT.get(), false);
                    boolean newStatus = !currentStatus;

                    // Salva o novo estado
                    stack.set(ModDataComponents.AUTO_SMELT.get(), newStatus);

                    // Envia mensagem para o jogador
                    String statusText = newStatus ? "ON" : "OFF";
                    ChatFormatting color = newStatus ? ChatFormatting.GREEN : ChatFormatting.RED;

                    player.sendSystemMessage(Component.literal("Auto-Smelt: " + statusText).withStyle(color));
                }
            }
        });
    }
}