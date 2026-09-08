package com.raishxn.ufo.wireless;

import net.minecraft.nbt.CompoundTag;

public final class WirelessBonusNbt {
    public static CompoundTag save(WirelessBonus bonus) {
        var tag = new CompoundTag();
        tag.putDouble("speed", bonus.speed()); tag.putDouble("energy", bonus.energy()); tag.putDouble("heat", bonus.heat());
        return tag;
    }
    public static WirelessBonus load(CompoundTag tag) {
        try { return new WirelessBonus(tag.getDouble("speed"), tag.getDouble("energy"), tag.getDouble("heat")); }
        catch (IllegalArgumentException ex) { return WirelessBonus.NONE; }
    }
}
