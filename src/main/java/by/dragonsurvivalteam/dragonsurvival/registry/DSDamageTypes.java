package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.BreathAbility;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class DSDamageTypes {
    public static final ResourceKey<DamageType> WATER_BURN = createKey("water_burn");
    public static final ResourceKey<DamageType> RAIN_BURN = createKey("rain_burn");
    public static final ResourceKey<DamageType> DEHYDRATION = createKey("dehydration");
    /* FIXME 1.20 :: Use */ public static final ResourceKey<DamageType> SPECTRAL_IMPACT = createKey("spectral_impact");
    public static final ResourceKey<DamageType> DRAGON_BREATH = createKey("dragon_breath");
    public static final ResourceKey<DamageType> CAVE_DRAGON_BREATH = createKey("cave_dragon_breath");
    /* TODO :: Unused */ public static final ResourceKey<DamageType> FOREST_DRAGON_BREATH = createKey("forest_dragon_breath");
    public static final ResourceKey<DamageType> SEA_DRAGON_BREATH = createKey("sea_dragon_breath");
    public static final ResourceKey<DamageType> FOREST_DRAGON_DRAIN = createKey("forest_dragon_drain"); // TODO 1.20 :: tags -> setMagic()
    public static final ResourceKey<DamageType> CAVE_DRAGON_BURN = createKey("cave_dragon_burn");
    public static final ResourceKey<DamageType> DRAGON_BALL_LIGHTNING = createKey("dragon_ball_lightning");
    public static final ResourceKey<DamageType> CRUSHED = createKey("crushed");

    public static void bootstrap(final BootstapContext<DamageType> context) {
        context.register(WATER_BURN, new DamageType("dragonsurvival.water_burn", 0.1F));
        context.register(RAIN_BURN, new DamageType("dragonsurvival.rain_burn", 0.1F));
        context.register(DEHYDRATION, new DamageType("dragonsurvival.dehydration", 0.1F));
        context.register(SPECTRAL_IMPACT, new DamageType("dragonsurvival.spectral_impact", 0.1F));
        context.register(DRAGON_BREATH, new DamageType("dragonsurvival.dragon_breath", 0.1F));
        context.register(CAVE_DRAGON_BREATH, new DamageType("cave_dragon_breath", 0.1F));
        context.register(FOREST_DRAGON_BREATH, new DamageType("forest_dragon_breath", 0.1F));
        context.register(SEA_DRAGON_BREATH, new DamageType("sea_dragon_breath", 0.1F));
        context.register(FOREST_DRAGON_DRAIN, new DamageType("dragonsurvival.forest_dragon_drain", 0.1F));
        context.register(CAVE_DRAGON_BURN, new DamageType("dragonsurvival.cave_dragon_burn", 0.1F));
        context.register(DRAGON_BALL_LIGHTNING, new DamageType("dragonsurvival.dragon_ball_lightning", 0.1F));
        context.register(CRUSHED, new DamageType("dragonsurvival.crushed", 0.1F));
    }

    public static DamageSource damageSource(final Level level, final ResourceKey<DamageType> damageType) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageType));
    }

    public static DamageSource entityDamageSource(final Level level, final ResourceKey<DamageType> damageType, final Entity entity) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageType), entity);
    }

    public static DamageSource entityDamageSource(final Level level, final BreathAbility breathAbility, final Entity entity) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(getBreathDamageType(breathAbility)), entity);
    }

    public static ResourceKey<DamageType> getBreathDamageType(final BreathAbility breathAbility) {
        if (DragonUtils.isDragonType(breathAbility.getDragonType(), DragonTypes.CAVE)) {
            return CAVE_DRAGON_BREATH;
        } else if (DragonUtils.isDragonType(breathAbility.getDragonType(), DragonTypes.FOREST)) {
            return FOREST_DRAGON_BREATH;
        } else if (DragonUtils.isDragonType(breathAbility.getDragonType(), DragonTypes.SEA)) {
            return SEA_DRAGON_BREATH;
        }

        return DRAGON_BREATH;
    }

    private static ResourceKey<DamageType> createKey(final String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(DragonSurvivalMod.MODID, name));
    }
}