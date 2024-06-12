package by.dragonsurvivalteam.dragonsurvival.data;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DataDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public static TagKey<DamageType> DRAGON_BREATH = createKey("dragon_breath");
    public static TagKey<DamageType> NO_KNOCKBACK = createKey("no_knockback");

    public DataDamageTypeTagsProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookupProvider, final String modId, @Nullable final ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider.thenApply(provider -> DSRegistryProvider.BUILDER.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), provider)), modId, existingFileHelper);
    }


    // TODO: I don't believe these tags are actually getting properly applied at this moment
    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(DSDamageTypes.FOREST_DRAGON_DRAIN)
                .add(DSDamageTypes.CAVE_DRAGON_BURN)
                .add(DSDamageTypes.SPECTRAL_IMPACT);

        tag(DamageTypeTags.IS_FIRE)
                .add(DSDamageTypes.CAVE_DRAGON_BURN)
                .add(DSDamageTypes.CAVE_DRAGON_BREATH);

        tag(DamageTypeTags.IS_LIGHTNING)
                .add(DSDamageTypes.SEA_DRAGON_BREATH);

        tag(DRAGON_BREATH)
                .add(DSDamageTypes.DRAGON_BREATH)
                .add(DSDamageTypes.CAVE_DRAGON_BURN)
                .add(DSDamageTypes.FOREST_DRAGON_DRAIN)
                .add(DSDamageTypes.SEA_DRAGON_BREATH);

        tag(NO_KNOCKBACK)
                .add(DSDamageTypes.CAVE_DRAGON_BURN)
                .add(DSDamageTypes.FOREST_DRAGON_DRAIN)
                .add(DSDamageTypes.CRUSHED);
    }

    private static TagKey<DamageType> createKey(@NotNull final String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DragonSurvivalMod.MODID, name));
    }
}
