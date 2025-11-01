package com.raishxn.ufo.item.custom.cell;

import appeng.api.stacks.AEKey;
import com.raishxn.ufo.init.OCDataComponents;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** 基于AEUniversalCellData的BigInteger版本，无其他逻辑更变，
 *  所有调试信息版本也仍使用AEUniversalCellData统一通知。
 *
 * @author Frostbite
 */
public class AEBigIntegerCellData extends SavedData
{
    /** 主容器子标签 */
    public static final String INV_SAVED_TAG = "inventory";

    /** 读取成功的条目列表名（在 INV_SAVED_TAG 里） */
    private static final String ENTRIES_TAG = "entries";

    /** 读取失败的条目列表名（在 INV_SAVED_TAG 里） */
    private static final String ERROR_ENTRIES_TAG = "error_entries";

    /** 单条目里的 key 子标签名 */
    private static final String ENTRY_KEY_TAG = "key";

    /** 单条目里的 amount 子标签名 */
    private static final String ENTRY_AMOUNT_TAG = "amount";

    /** 统一目录名（位于 world/data/ 下） */
    private static final String SAVED_FOLDER_NAME = "ae_universal_cell_data";

    /** 原始仓库存放在此处，后续使用此仓库的引用来构建 */
    private final Object2ObjectMap<AEKey, BigInteger> storage;

    /**
     * 上次反序列化失败而保留下来的“原始条目”队列。
     * - 每次 save() 都会把它写入到 INV_SAVED_TAG/ERROR_ENTRIES_TAG。
     * - 每次 load() 都会尝试“重读”；成功则并入 storage 并打印信息，失败则静默保留。
     */
    private final ObjectArrayList<CompoundTag> pendingReadErrors;

    public AEBigIntegerCellData(@NotNull Object2ObjectMap<AEKey, BigInteger> storage)
    {
        this(storage, new ObjectArrayList<>());
    }

    private AEBigIntegerCellData(@NotNull Object2ObjectMap<AEKey, BigInteger> storage,
                                @NotNull ObjectArrayList<CompoundTag> pendingReadErrors)
    {
        this.storage = storage;
        this.storage.defaultReturnValue(BigInteger.ZERO);
        this.pendingReadErrors = pendingReadErrors;
    }

    public static final SavedData.Factory<AEBigIntegerCellData> FACTORY =
            new SavedData.Factory<>(
                    () -> {
                        Object2ObjectOpenHashMap<AEKey, BigInteger> s = new Object2ObjectOpenHashMap<>();
                        s.defaultReturnValue(BigInteger.ZERO);
                        return new AEBigIntegerCellData(s, new ObjectArrayList<>());
                    },
                    AEBigIntegerCellData::load
            );

    /** 获取原始存储数据 */
    public @NotNull Object2ObjectMap<AEKey, BigInteger> getOriginalStorage()
    {
        return storage;
    }

    /** 根据 UUID 获取对应的数据（仅当磁盘上已有对应文件时返回） */
    public static @Nullable AEBigIntegerCellData getCellDataByUUID(@NotNull UUID uuid)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        ensureSaveDirExists(server);

        final String key = makeKey(uuid);
        return server.overworld().getDataStorage().get(FACTORY, key);
    }

    /**
     * 如果 ItemStack 存在 UUID 且对应数据文件存在，则加载；
     * 否则为其分配一个全新 UUID，创建并注册新的 SavedData，并把 UUID 写回该物品。
     */
    public static @Nullable AEBigIntegerCellData computeIfAbsentCellDataForItemStack(@NotNull ItemStack itemStack)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        ensureSaveDirExists(server);

        final var dataStorage = server.overworld().getDataStorage();

        // 1) 读取已有 UUID（从数据组件）
        UUID existing = itemStack.get(OCDataComponents.CELL_UUID.get());
        if (existing != null) {
            AEBigIntegerCellData data = getCellDataByUUID(existing);
            if (data != null) {
                return data;
            }
            // 组件里有 UUID 但文件不存在 -> 继续走创建流程
        }

        // 2) 分配一个全新的、与现有文件不冲突的 UUID
        UUID fresh;
        do {
            fresh = UUID.randomUUID();
        } while (getCellDataByUUID(fresh) != null); // 极小概率，防御一下

        // 3) 写回物品的 UUID（数据组件）
        itemStack.set(OCDataComponents.CELL_UUID.get(), fresh);

        // 4) 创建并注册新的 SavedData（注册到 DataStorage 后由世界存档生命周期负责持久化）
        Object2ObjectOpenHashMap<AEKey, BigInteger> s = new Object2ObjectOpenHashMap<>();
        s.defaultReturnValue(BigInteger.ZERO);
        AEBigIntegerCellData newData = new AEBigIntegerCellData(s);
        dataStorage.set(makeKey(fresh), newData);
        // 不为空数据标脏，直到有insert/extract操作后由它们标脏

        return newData;
    }

    /** 硬盘序列化（1.21.1 新签名，提供 registries） */
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        // 统一放在 INV_SAVED_TAG 里
        CompoundTag invTag = new CompoundTag();

        // 正常条目（结构与键名保持不变）
        ListTag entriesList = new ListTag();
        for (Object2ObjectMap.Entry<AEKey, BigInteger> e : storage.object2ObjectEntrySet())
        {
            AEKey key = e.getKey();
            BigInteger amount = e.getValue();

            if (key == null)
            {
                // 理论上不应出现，防御性略过
                System.err.println("[AEUniversalCellData] Skip null key during serialization.");
                continue;
            }

            try
            {
                CompoundTag entry = new CompoundTag();
                entry.put(ENTRY_KEY_TAG, key.toTagGeneric(registries));
                entry.putByteArray(ENTRY_AMOUNT_TAG, amount.toByteArray());
                entriesList.add(entry);
            }
            catch(Throwable ex)
            {
                // 序列化失败：无法可靠得到要保存的信息 -> 打印并略过
                System.err.println("[AEUniversalCellData] Failed to serialize entry: key=" + key
                        + ", amount=" + amount + " ; cause=" + ex);
            }
        }
        invTag.put(ENTRIES_TAG, entriesList);

        // 读取失败的条目（从上次 load() 残留，或重试后仍失败），原样写回
        ListTag errorList = new ListTag();
        for (CompoundTag bad : pendingReadErrors)
        {
            errorList.add(bad.copy());
        }
        invTag.put(ERROR_ENTRIES_TAG, errorList);

        tag.put(INV_SAVED_TAG, invTag);
        return tag;
    }

    /** 从硬盘反序列化（1.21.1 需要 registries） */
    public static AEBigIntegerCellData load(CompoundTag tag, HolderLookup.Provider registries)
    {
        Object2ObjectMap<AEKey, BigInteger> storage = new Object2ObjectOpenHashMap<>();
        storage.defaultReturnValue(BigInteger.ZERO);
        ObjectArrayList<CompoundTag> errorQueue = new ObjectArrayList<>();

        // 统一从 INV_SAVED_TAG 读取
        CompoundTag invTag = tag.getCompound(INV_SAVED_TAG);

        // 读取正常条目
        ListTag entries = invTag.getList(ENTRIES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++)
        {
            CompoundTag entry = entries.getCompound(i);
            try
            {
                CompoundTag keyTag = entry.getCompound(ENTRY_KEY_TAG);
                AEKey key = AEKey.fromTagGeneric(registries, keyTag); // 1.21.1 需传 registries
                if(key == null)
                {
                    // 解析失败 -> 放入错误队列，打印
                    errorQueue.add(entry.copy());
                    System.err.println("[AEUniversalCellData] Failed to deserialize entry (null key). Entry=" + entry);
                    continue;
                }
                BigInteger amount = new BigInteger(entry.getByteArray(ENTRY_AMOUNT_TAG));
                addTo(storage, key, amount);
            }
            catch(Throwable ex)
            {
                // 解析失败 -> 放入错误队列，打印
                errorQueue.add(entry.copy());
                System.err.println("[AEUniversalCellData] Failed to deserialize entry: " + entry + " ; cause=" + ex);
            }
        }

        // 尝试重读历史错误条目（上次保存时写入的 ERROR_ENTRIES_TAG）
        ListTag oldErrors = invTag.getList(ERROR_ENTRIES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < oldErrors.size(); i++)
        {
            CompoundTag badEntry = oldErrors.getCompound(i);
            boolean recovered = false;
            try
            {
                CompoundTag keyTag = badEntry.getCompound(ENTRY_KEY_TAG);
                AEKey key = AEKey.fromTagGeneric(registries, keyTag);
                if(key != null)
                {
                    BigInteger amount = new BigInteger(badEntry.getByteArray(ENTRY_AMOUNT_TAG));
                    addTo(storage, key, amount);
                    recovered = true;
                }
            }
            catch(Throwable ignored)
            {
                recovered = false;
            }

            if (recovered)
            {
                // 重读成功：打印信息，但不再保留到错误队列
                System.err.println("[AEUniversalCellData] Recovered previously failed entry: " + badEntry);
            }
            else
            {
                // 仍失败：静默保留，等待下次保存写回 下次加载再尝试
                errorQueue.add(badEntry.copy());
            }
        }
        return new AEBigIntegerCellData(storage, errorQueue);
    }

    // ---------------------------------- 辅助方法 ----------------------------------

    /** 生成 DataStorage 的路径（保持子路径：ae_universal_cell_data/<uuid>） */
    private static String makeKey(@NotNull UUID uuid)
    {
        return SAVED_FOLDER_NAME + "/" + uuid;
    }

    /** 确保 world/data/ae_universal_cell_data 目录存在 */
    private static void ensureSaveDirExists(@NotNull MinecraftServer server)
    {
        Path dir = server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve(SAVED_FOLDER_NAME);
        try
        {
            Files.createDirectories(dir); // 已存在则静默通过
        }
        catch(IOException e)
        {
            // 不中断，但留痕方便排查
            System.err.println("[AEUniversalCellData] Failed to create save directory: " + dir + " : " + e);
        }
    }

    /** 简单的工具类，用于保持操作与Long版语义相同 */
    private static void addTo(Object2ObjectMap<AEKey, BigInteger> map, AEKey key, BigInteger delta)
    {
        if (delta == null) return;
        if (delta.signum() <= 0) return;
        BigInteger prev = map.getOrDefault(key, BigInteger.ZERO);
        BigInteger now = prev.add(delta);
        if (now.signum() == 0)
        {
            map.remove(key);
        }
        else
        {
            map.put(key, now);
        }
    }

}
