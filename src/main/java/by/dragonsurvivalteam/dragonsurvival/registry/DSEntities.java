package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.*;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.BallLightningEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.DragonSpikeEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.FireBallEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.VillagerRelationsHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( "rawtypes,unchecked" )
@Mod.EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class DSEntities {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DragonSurvivalMod.MODID);

	// Player related
	public static RegistryObject<EntityType<DragonEntity>> DRAGON = ENTITY_TYPES.register("dummy_dragon", () -> new EntityType<>(DragonEntity::new, MobCategory.MISC, true, false, false, false, ImmutableSet.of(), EntityDimensions.fixed(0.9f, 1.9f), 0, 0, FeatureFlagSet.of(FeatureFlags.VANILLA)));
	public static RegistryObject<EntityType<DragonEntity>> DRAGON_ARMOR = ENTITY_TYPES.register("dragon_armor", () -> new EntityType<>(DragonEntity::new, MobCategory.MISC, true, false, false, false, ImmutableSet.of(), EntityDimensions.fixed(0.9f, 1.9f), 0, 0, FeatureFlagSet.of(FeatureFlags.VANILLA)));

	// Fake entities
	public static RegistryObject<EntityType<Bolas>> BOLAS_ENTITY = ENTITY_TYPES.register("bolas", () -> EntityType.Builder.<Bolas>of((entity, level) -> new Bolas(level), MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("bolas"));
	public static RegistryObject<EntityType<DragonSpikeEntity>> DRAGON_SPIKE = ENTITY_TYPES.register("dragon_spike", () -> EntityType.Builder.<DragonSpikeEntity>of(DragonSpikeEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(1).build("dragon_spike"));
	public static RegistryObject<EntityType<BallLightningEntity>> BALL_LIGHTNING = ENTITY_TYPES.register("ball_lightning", () -> EntityType.Builder.<BallLightningEntity>of(BallLightningEntity::new, MobCategory.MISC).sized(1F, 1F).clientTrackingRange(4).updateInterval(1).build("ball_lightning"));
	public static RegistryObject<EntityType<FireBallEntity>> FIREBALL = ENTITY_TYPES.register("fireball", () -> EntityType.Builder.<FireBallEntity>of(FireBallEntity::new, MobCategory.MISC).sized(1F, 1F).clientTrackingRange(4).updateInterval(1).build("fireball"));

	// Entities
	public static RegistryObject<EntityType<HunterHoundEntity>> HUNTER_HOUND = ENTITY_TYPES.register("hunter_hound", () -> EntityType.Builder.of(HunterHoundEntity::new, MobCategory.MONSTER).sized(0.6F, 0.85F).clientTrackingRange(64).updateInterval(1).build("hunter_hound"));
	public static RegistryObject<EntityType<Shooter>> SHOOTER_HUNTER = ENTITY_TYPES.register("shooter", () -> EntityType.Builder.of(Shooter::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(64).updateInterval(1).build("shooter"));
	public static RegistryObject<EntityType<SquireEntity>> SQUIRE_HUNTER = ENTITY_TYPES.register("squire", () -> EntityType.Builder.of(SquireEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(64).updateInterval(1).build("squire"));
	public static RegistryObject<EntityType<Princess>> PRINCESS = ENTITY_TYPES.register("princess_entity", () -> EntityType.Builder.<Princess>of(Princess::new, MobCategory.MONSTER).sized(0.6F, 1.9F).clientTrackingRange(64).updateInterval(1).build("princess_entity"));
	public static RegistryObject<EntityType<KnightEntity>> KNIGHT = ENTITY_TYPES.register("knight", () -> EntityType.Builder.of(KnightEntity::new, MobCategory.MONSTER).sized(0.8f, 2.5f).clientTrackingRange(64).updateInterval(1).build("knight"));
	public static RegistryObject<EntityType<PrincesHorseEntity>> PRINCESS_ON_HORSE = ENTITY_TYPES.register("princess", () -> EntityType.Builder.of(PrincesHorseEntity::new, MobCategory.MONSTER).sized(0.8f, 2.5f).clientTrackingRange(64).updateInterval(1).build("princess"));
	public static RegistryObject<EntityType<PrinceHorseEntity>> PRINCE_ON_HORSE = ENTITY_TYPES.register("prince", () -> EntityType.Builder.of(PrinceHorseEntity::new, MobCategory.MONSTER).sized(0.8f, 2.5f).clientTrackingRange(64).updateInterval(1).build("prince"));

	// Professions
	public static VillagerProfession PRINCESS_PROFESSION, PRINCE_PROFESSION;

	@SubscribeEvent
	public static void register(final RegisterEvent event) {
		ResourceKey<? extends Registry<?>> key = event.getRegistryKey();

		if (key.equals(ForgeRegistries.Keys.VILLAGER_PROFESSIONS)) {
			registerVillageTypes(event);
		} else if (key.equals(ForgeRegistries.Keys.ITEMS)) {
			registerSpawnEggs(event);
		}
	}

	@SubscribeEvent
	public static void attributeCreationEvent(final EntityAttributeCreationEvent event) {
		event.put(DRAGON.get(), DragonEntity.createLivingAttributes().build());
		event.put(DRAGON_ARMOR.get(), DragonEntity.createLivingAttributes().build());
		event.put(HUNTER_HOUND.get(), Wolf.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.houndSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.houndDamage).add(Attributes.MAX_HEALTH, ServerConfig.houndHealth).build());
		event.put(SHOOTER_HUNTER.get(), Pillager.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.hunterSpeed).add(Attributes.MAX_HEALTH, ServerConfig.hunterHealth).add(Attributes.ARMOR, ServerConfig.hunterArmor).add(Attributes.ATTACK_DAMAGE, ServerConfig.hunterDamage).build());
		event.put(SQUIRE_HUNTER.get(), Vindicator.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.squireSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.squireDamage).add(Attributes.ARMOR, ServerConfig.squireArmor).add(Attributes.MAX_HEALTH, ServerConfig.squireHealth).build());
		event.put(KNIGHT.get(), KnightEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.knightSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.knightDamage).add(Attributes.ARMOR, ServerConfig.knightArmor).add(Attributes.MAX_HEALTH, ServerConfig.knightHealth).build());
		event.put(PRINCESS.get(), Villager.createAttributes().add(Attributes.MAX_HEALTH, ServerConfig.princessHealth).add(Attributes.ARMOR, ServerConfig.princessArmor).add(Attributes.MOVEMENT_SPEED, ServerConfig.princessSpeed).build());
		event.put(PRINCESS_ON_HORSE.get(), Villager.createAttributes().add(Attributes.MAX_HEALTH, ServerConfig.princessHealth).add(Attributes.ARMOR, ServerConfig.princessArmor).add(Attributes.MOVEMENT_SPEED, ServerConfig.princessSpeed).build());
		event.put(PRINCE_ON_HORSE.get(), Villager.createAttributes().add(Attributes.ATTACK_DAMAGE, ServerConfig.princeDamage).add(Attributes.MAX_HEALTH, ServerConfig.princeHealth).add(Attributes.ARMOR, ServerConfig.princeArmor).add(Attributes.MOVEMENT_SPEED, ServerConfig.princeSpeed).build());
	}

	@SubscribeEvent
	public static void registerSpawn(final SpawnPlacementRegisterEvent event) {
		SpawnPlacements.SpawnPredicate predicate = (pEntityType, serverWorld, mobSpawnType, pPos, random) -> serverWorld.getBlockState(pPos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && serverWorld.canSeeSky(pPos);
		SpawnPlacements.SpawnPredicate princeSpawn = (pEntityType, serverWorld, mobSpawnType, pPos, random) -> predicate.test(pEntityType, serverWorld, mobSpawnType, pPos, random) && serverWorld.getLevel().isVillage(pPos);

		event.register(SHOOTER_HUNTER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(SQUIRE_HUNTER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(KNIGHT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(PRINCE_ON_HORSE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, princeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(PRINCESS_ON_HORSE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, princeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
	}

	static {
		VillagerRelationsHandler.dragonHunters = new ArrayList<>(4);

		if (ServerConfig.spawnHound) {
			VillagerRelationsHandler.dragonHunters.add(castToPathFinder(HUNTER_HOUND));
		}

		if (ServerConfig.spawnSquire) {
			VillagerRelationsHandler.dragonHunters.add(castToPathFinder(SQUIRE_HUNTER));
		}

		if (ServerConfig.spawnHunter) {
			VillagerRelationsHandler.dragonHunters.add(castToPathFinder(SHOOTER_HUNTER));
		}

		if (ServerConfig.spawnKnight) {
			VillagerRelationsHandler.dragonHunters.add(castToPathFinder(KNIGHT));
		}
	}

	public static <T extends RegistryObject<EntityType<? extends PathfinderMob>>> T castToPathFinder(final RegistryObject entityType) {
		return (T) entityType;
	}

	public static <T extends RegistryObject<EntityType<? extends Mob>>> T castToMob(final RegistryObject entityType) {
		return (T) entityType;
	}

	public static void registerVillageTypes(RegisterEvent ignored) {
		PRINCESS_PROFESSION = new VillagerProfession("princess", PoiType.NONE, PoiType.NONE, ImmutableSet.of(), ImmutableSet.of(), null);
		PRINCE_PROFESSION = new VillagerProfession("prince", PoiType.NONE, PoiType.NONE, ImmutableSet.of(), ImmutableSet.of(), null);
	}

	public static List<Item> SPAWN_EGGS = new ArrayList<>();

	public static void registerSpawnEggs(RegisterEvent event){
		registerSpawnEgg(event, castToMob(HUNTER_HOUND), 10510648, 8934192);
		registerSpawnEgg(event, castToMob(SHOOTER_HUNTER), 12486764, 2690565);
		registerSpawnEgg(event, castToMob(SQUIRE_HUNTER), 12486764, 5318420);
		registerSpawnEgg(event, castToMob(KNIGHT),  -15526631, -8750470);
		registerSpawnEgg(event, castToMob(PRINCE_ON_HORSE), -14210026, -9571315);
		registerSpawnEgg(event, castToMob(PRINCESS_ON_HORSE), -14804205, -14047);
	}

	private static void registerSpawnEgg(final RegisterEvent event, final RegistryObject<EntityType<? extends Mob>> entity, int eggPrimary, int eggSecondary) {
		Item spawnEgg = new ForgeSpawnEggItem(entity, eggPrimary, eggSecondary, new Item.Properties()/*.tab(DragonSurvivalMod.items)*/);
		event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(DragonSurvivalMod.MODID, entity.getKey().location().getPath() + "_spawn_egg"), () -> spawnEgg);
		SPAWN_EGGS.add(spawnEgg);
	}
}