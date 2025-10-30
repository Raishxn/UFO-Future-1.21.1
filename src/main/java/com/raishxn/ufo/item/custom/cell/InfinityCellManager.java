package com.raishxn.ufo.item.custom.cell;

import appeng.api.stacks.AEKey;
import com.google.common.collect.Maps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

public class InfinityCellManager extends SavedData {

    private final Map<UUID, Map<AEKey, BigInteger>> inventories = Maps.newHashMap();

    // Carrega os dados do disco
    public static InfinityCellManager load(CompoundTag tag, HolderLookup.Provider registries) {
        InfinityCellManager manager = new InfinityCellManager();
        ListTag inventoriesTag = tag.getList("inventories", Tag.TAG_COMPOUND);
        for (Tag invTagBase : inventoriesTag) {
            CompoundTag invTag = (CompoundTag) invTagBase;
            UUID uuid = invTag.getUUID("uuid");
            Map<AEKey, BigInteger> inventory = Maps.newHashMap();

            ListTag itemsTag = invTag.getList("items", Tag.TAG_COMPOUND);
            for (Tag itemTagBase : itemsTag) {
                CompoundTag itemTag = (CompoundTag) itemTagBase;
                // CORREÇÃO AQUI: Usa o método fromTagGeneric que existe na sua versão da API
                AEKey key = AEKey.fromTagGeneric(registries, itemTag.getCompound("key"));
                if (key != null) {
                    BigInteger amount = new BigInteger(itemTag.getString("amt"));
                    inventory.put(key, amount);
                }
            }
            manager.inventories.put(uuid, inventory);
        }
        return manager;
    }

    // Salva os dados no disco
    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        ListTag inventoriesTag = new ListTag();
        for (Map.Entry<UUID, Map<AEKey, BigInteger>> entry : inventories.entrySet()) {
            CompoundTag invTag = new CompoundTag();
            invTag.putUUID("uuid", entry.getKey());

            ListTag itemsTag = new ListTag();
            for (Map.Entry<AEKey, BigInteger> itemEntry : entry.getValue().entrySet()) {
                CompoundTag itemTag = new CompoundTag();
                // Usa o método toTagGeneric que também existe na sua versão
                itemTag.put("key", itemEntry.getKey().toTagGeneric(registries));
                itemTag.putString("amt", itemEntry.getValue().toString());
                itemsTag.add(itemTag);
            }
            invTag.put("items", itemsTag);
            inventoriesTag.add(invTag);
        }
        tag.put("inventories", inventoriesTag);
        return tag;
    }

    public Map<AEKey, BigInteger> getOrCreateInventory(UUID uuid) {
        return inventories.computeIfAbsent(uuid, u -> Maps.newHashMap());
    }

    public static InfinityCellManager get(Level level) {
        if (level.isClientSide) {
            throw new IllegalStateException("Cannot access InfinityCellManager on the client.");
        }
        ServerLevel serverLevel = (ServerLevel) level;
        DimensionDataStorage storage = serverLevel.getDataStorage();
        Factory<InfinityCellManager> factory = new Factory<>(
                InfinityCellManager::new,
                InfinityCellManager::load,
                DataFixTypes.LEVEL
        );
        return storage.computeIfAbsent(factory, "ufo_infinity_cells");
    }
}