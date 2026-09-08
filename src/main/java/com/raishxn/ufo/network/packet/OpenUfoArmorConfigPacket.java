package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import com.raishxn.ufo.screen.UfoArmorConfigMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenUfoArmorConfigPacket() implements CustomPacketPayload {
    public static final Type<OpenUfoArmorConfigPacket> TYPE = new Type<>(UfoMod.id("open_ufo_armor_config"));
    public static final StreamCodec<ByteBuf, OpenUfoArmorConfigPacket> STREAM_CODEC = StreamCodec.unit(new OpenUfoArmorConfigPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenUfoArmorConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            boolean hasArmor = false;
            for (int index = 36; index < 40; index++) {
                if (UfoArmorItem.isUfoArmorPiece(player.getInventory().getItem(index))) {
                    hasArmor = true;
                    break;
                }
            }
            if (hasArmor) {
                player.openMenu(new SimpleMenuProvider(
                        (id, inventory, ignored) -> new UfoArmorConfigMenu(id, inventory),
                        Component.translatable("gui.ufo.armor_config")));
            }
        });
    }
}
