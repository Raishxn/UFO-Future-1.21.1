package com.raishxn.ufo.network.packet;

import com.raishxn.ufo.UFOConfig;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.armor.UfoArmorModule;
import com.raishxn.ufo.armor.UfoArmorSetting;
import com.raishxn.ufo.item.custom.UfoArmorItem;
import com.raishxn.ufo.network.MachinePacketGuard;
import com.raishxn.ufo.screen.UfoArmorConfigMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetUfoArmorModuleSettingPacket(int moduleOrdinal, int settingOrdinal, int value)
        implements CustomPacketPayload {
    public static final Type<SetUfoArmorModuleSettingPacket> TYPE =
            new Type<>(UfoMod.id("set_ufo_armor_module_setting"));
    public static final StreamCodec<FriendlyByteBuf, SetUfoArmorModuleSettingPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SetUfoArmorModuleSettingPacket::moduleOrdinal,
                    ByteBufCodecs.VAR_INT, SetUfoArmorModuleSettingPacket::settingOrdinal,
                    ByteBufCodecs.VAR_INT, SetUfoArmorModuleSettingPacket::value,
                    SetUfoArmorModuleSettingPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetUfoArmorModuleSettingPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!MachinePacketGuard.allow(context, MachinePacketGuard.Action.SET_ARMOR_SETTING)) return;
            if (!(context.player().containerMenu instanceof UfoArmorConfigMenu menu)) return;
            if (packet.moduleOrdinal < 0 || packet.moduleOrdinal >= UfoArmorModule.values().length
                    || packet.settingOrdinal < 0 || packet.settingOrdinal >= UfoArmorSetting.values().length) return;
            UfoArmorModule module = UfoArmorModule.values()[packet.moduleOrdinal];
            UfoArmorSetting setting = UfoArmorSetting.values()[packet.settingOrdinal];
            if (setting.module() != module) return;
            // The server grants the operator cap, not whatever the client asked for.
            int capped = UFOConfig.clampArmorSetting(setting, packet.value);
            if (UfoArmorItem.setModuleSetting(menu.selectedArmor(), setting, capped)) {
                menu.broadcastChanges();
            }
        });
    }
}
