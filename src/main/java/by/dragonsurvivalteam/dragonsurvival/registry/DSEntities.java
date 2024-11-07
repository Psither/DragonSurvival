package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.*;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.BallLightningEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.BlizzardSpikeEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.DragonSpikeEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.FireBallEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DSEntities {
    public static final DeferredRegister<EntityType<?>> DS_ENTITY_TYPES = DeferredRegister.create(
            BuiltInRegistries.ENTITY_TYPE,
            DragonSurvival.MODID
    );

    // Player related
    public static DeferredHolder<EntityType<?>, EntityType<DragonEntity>> DRAGON = DS_ENTITY_TYPES.register(
            "dummy_dragon",
            () -> new EntityType<>(DragonEntity::new, MobCategory.MISC, true, false, false, false, ImmutableSet.of(), EntityDimensions.fixed(0.9f, 1.9f), 1.0f, 0, 0, FeatureFlagSet.of(FeatureFlags.VANILLA)));

    // Fake entities
    public static DeferredHolder<EntityType<?>, EntityType<Bolas>> BOLAS_ENTITY = DS_ENTITY_TYPES.register(
            "bolas",
            () -> EntityType.Builder.<Bolas>of((entity, level) ->
                            new Bolas(level), MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("bolas"));
    public static DeferredHolder<EntityType<?>, EntityType<DragonSpikeEntity>> DRAGON_SPIKE = DS_ENTITY_TYPES.register(
            "dragon_spike",
            () -> EntityType.Builder.<DragonSpikeEntity>of(DragonSpikeEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("dragon_spike"));

    public static DeferredHolder<EntityType<?>, EntityType<BlizzardSpikeEntity>> BLIZZARD_SPIKE = DS_ENTITY_TYPES.register(
        "blizzard_spike",
        () -> EntityType.Builder.<BlizzardSpikeEntity>of(BlizzardSpikeEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(4)
                .updateInterval(1)
                .build("blizzard_spike"));
    public static DeferredHolder<EntityType<?>, EntityType<BallLightningEntity>> BALL_LIGHTNING = DS_ENTITY_TYPES.register(
            "ball_lightning",
            () -> EntityType.Builder.<BallLightningEntity>of(BallLightningEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("ball_lightning"));
    public static DeferredHolder<EntityType<?>, EntityType<FireBallEntity>> FIREBALL = DS_ENTITY_TYPES.register(
            "fireball",
            () -> EntityType.Builder.<FireBallEntity>of(FireBallEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("fireball"));

    // Entities
    public static DeferredHolder<EntityType<?>, EntityType<HoundEntity>> HUNTER_HOUND = DS_ENTITY_TYPES.register(
            "hunter_hound",
            () -> EntityType.Builder.of(HoundEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_hound"));

    public static DeferredHolder<EntityType<?>, EntityType<GriffinEntity>> HUNTER_GRIFFIN = DS_ENTITY_TYPES.register(
            "hunter_griffin",
            () -> EntityType.Builder.of(GriffinEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_griffin"));

    public static DeferredHolder<EntityType<?>, EntityType<SpearmanEntity>> HUNTER_SPEARMAN = DS_ENTITY_TYPES.register(
            "hunter_spearman",
            () -> EntityType.Builder.of(SpearmanEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_spearman"));

    public static DeferredHolder<EntityType<?>, EntityType<KnightEntity>> HUNTER_KNIGHT = DS_ENTITY_TYPES.register(
            "hunter_knight", () -> EntityType.Builder.of(KnightEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 3f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_knight"));

    public static DeferredHolder<EntityType<?>, EntityType<AmbusherEntity>> HUNTER_AMBUSHER = DS_ENTITY_TYPES.register(
            "hunter_ambusher", () -> EntityType.Builder.of(AmbusherEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_ambusher"));

    public static DeferredHolder<EntityType<?>, EntityType<LeaderEntity>> HUNTER_LEADER = DS_ENTITY_TYPES.register(
            "hunter_leader",
            () -> EntityType.Builder.of(LeaderEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_leader"));

    @SubscribeEvent
    public static void attributeCreationEvent(final EntityAttributeCreationEvent event) {
        event.put(DRAGON.get(), DragonEntity.createLivingAttributes().build());
        event.put(HUNTER_HOUND.get(), Wolf.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.houndSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.houndDamage).add(Attributes.MAX_HEALTH, ServerConfig.houndHealth).build());
        event.put(HUNTER_SPEARMAN.get(), Vindicator.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.spearmanSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.spearmanDamage).add(Attributes.ARMOR, ServerConfig.spearmanArmor).add(Attributes.MAX_HEALTH, ServerConfig.spearmanHealth).build());
        event.put(HUNTER_KNIGHT.get(), KnightEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.knightSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.knightDamage).add(Attributes.ARMOR, ServerConfig.knightArmor).add(Attributes.MAX_HEALTH, ServerConfig.knightHealth).build());
        event.put(HUNTER_AMBUSHER.get(), AmbusherEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.ambusherSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.ambusherDamage).add(Attributes.ARMOR, ServerConfig.ambusherArmor).add(Attributes.MAX_HEALTH, ServerConfig.ambusherHealth).build());
        event.put(HUNTER_GRIFFIN.get(), GriffinEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.griffinSpeed).add(Attributes.FLYING_SPEED, ServerConfig.griffinSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.griffinDamage).add(Attributes.MAX_HEALTH, ServerConfig.griffinHealth).build());
        event.put(HUNTER_LEADER.get(), LeaderEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.leaderSpeed).add(Attributes.MAX_HEALTH, ServerConfig.leaderHealth).build());
    }

    @SubscribeEvent
    public static void registerSpawn(final RegisterSpawnPlacementsEvent event) {
        SpawnPlacements.SpawnPredicate predicate = (pEntityType, serverWorld, mobSpawnType, pPos, random) -> serverWorld.canSeeSky(pPos);

        event.register(HUNTER_SPEARMAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HUNTER_KNIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HUNTER_AMBUSHER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HUNTER_HOUND.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HUNTER_GRIFFIN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static <T extends DeferredHolder<EntityType<?>, EntityType<? extends PathfinderMob>>> T castToPathFinder(final DeferredHolder entityType) {
        return (T) entityType;
    }

    public static <T extends DeferredHolder<EntityType<?>, EntityType<? extends Mob>>> T castToMob(final DeferredHolder entityType) {
        return (T) entityType;
    }
}