package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.NBTInterface;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;

public class SkinPreset implements INBTSerializable<CompoundTag> {
    public HashMap<DragonLevel, Lazy<SkinAgeGroup>> skinAges = new HashMap<>();

    public SkinPreset() {
        for (DragonLevel level : DragonLevel.values()) {
            skinAges.computeIfAbsent(level, (_level) -> Lazy.of(() -> new SkinAgeGroup(_level)));
        }
    }

    public void initDefaults(DragonStateHandler handler) {
        initDefaults(handler.getType());
    }

    public void initDefaults(AbstractDragonType type) {
        if (type == null) {
            return;
        }

        for (DragonLevel level : DragonLevel.values()) {
            skinAges.put(level, Lazy.of(() -> new SkinAgeGroup(level, type)));
        }
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();

        for (DragonLevel level : DragonLevel.values()) {
            nbt.put(level.name, skinAges.getOrDefault(level, Lazy.of(() -> new SkinAgeGroup(level))).get().writeNBT());
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag base) {
        for (DragonLevel level : DragonLevel.values()) {
            skinAges.put(level,
                    Lazy.of(() -> {
                        SkinAgeGroup ageGroup = new SkinAgeGroup(level);
                        CompoundTag nbt = base.getCompound(level.name);
                        ageGroup.readNBT(nbt);
                        return ageGroup;
                    })
            );
        }
    }

    public static class SkinAgeGroup implements NBTInterface {
        public DragonLevel level;
        public HashMap<EnumSkinLayer, Lazy<LayerSettings>> layerSettings = new HashMap<>();

        public boolean wings = true;
        public boolean defaultSkin = false;

        public SkinAgeGroup(DragonLevel level, AbstractDragonType type) {
            this(level);
            for (EnumSkinLayer layer : EnumSkinLayer.values()) {
                String part = DragonEditorRegistry.getDefaultPart(type, level, layer);
                EnumSkinLayer trueLayer = EnumSkinLayer.valueOf(layer.getNameUpperCase());
                HashMap<EnumSkinLayer, DragonEditorObject.DragonTextureMetadata[]> hm = DragonEditorRegistry.CUSTOMIZATIONS.get(type.getTypeNameUpperCase());
                if (hm != null) {
                    DragonEditorObject.DragonTextureMetadata[] texts = hm.get(trueLayer);
                    if (texts != null) {
                        for (DragonEditorObject.DragonTextureMetadata text : texts) {
                            if (text.key.equals(part)) {
                                layerSettings.put(layer, Lazy.of(() -> new LayerSettings(part, text.average_hue)));
                                break;
                            }
                        }
                    } else {
                        layerSettings.put(layer, Lazy.of(() -> new LayerSettings(part, 0.5f)));
                    }
                } else {
                    layerSettings.put(layer, Lazy.of(() -> new LayerSettings(part, 0.5f)));
                }
            }
        }

        public SkinAgeGroup(DragonLevel level) {
            this.level = level;

            for (EnumSkinLayer layer : EnumSkinLayer.values()) {
                layerSettings.computeIfAbsent(layer, s -> Lazy.of(LayerSettings::new));
            }
        }

        @Override
        public CompoundTag writeNBT() {
            CompoundTag nbt = new CompoundTag();

            nbt.putBoolean("wings", wings);
            nbt.putBoolean("defaultSkin", defaultSkin);

            for (EnumSkinLayer layer : EnumSkinLayer.values()) {
                nbt.put(layer.name(), layerSettings.getOrDefault(layer, Lazy.of(LayerSettings::new)).get().writeNBT());
            }

            return nbt;
        }

        @Override
        public void readNBT(CompoundTag base) {
            wings = base.getBoolean("wings");
            defaultSkin = base.getBoolean("defaultSkin");

            for (EnumSkinLayer layer : EnumSkinLayer.values()) {
                layerSettings.put(layer, Lazy.of(() -> {
                    LayerSettings ageGroup = new LayerSettings();
                    CompoundTag nbt = base.getCompound(layer.name());
                    ageGroup.readNBT(nbt);
                    return ageGroup;
                }));
            }
        }
    }
}