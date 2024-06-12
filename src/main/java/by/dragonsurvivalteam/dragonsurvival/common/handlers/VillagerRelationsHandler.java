package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.VillageRelationShips;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.DragonHunter;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.KnightEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.PrinceHorseEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.PrincesHorseEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowMobGoal;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import by.dragonsurvivalteam.dragonsurvival.util.SpawningUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class VillagerRelationsHandler{
	public static List<Supplier<EntityType<? extends PathfinderMob>>> dragonHunters;

	private static int timeLeft = Functions.minutesToTicks(ServerConfig.royalSpawnDelay) + Functions.minutesToTicks(ThreadLocalRandom.current().nextInt(30));

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent deathEvent){
		LivingEntity livingEntity = deathEvent.getEntity();
		Entity killer = deathEvent.getSource().getEntity();
		if(killer instanceof Player playerEntity){
			if(livingEntity instanceof AbstractVillager){
				Level world = killer.level();
				if(!(livingEntity instanceof PrincesHorseEntity)){

					if(DragonUtils.isDragon(killer)){
						AbstractVillager villagerEntity = (AbstractVillager)livingEntity;

						MerchantOffers merchantOffers = villagerEntity.getOffers();

						if(villagerEntity instanceof Villager villager){
							int level = villager.getVillagerData().getLevel();

							if(world.random.nextInt(100) < 30){
								Optional<MerchantOffer> offer = merchantOffers.stream().filter(merchantOffer -> merchantOffer.getResult().getItem() != Items.EMERALD).toList().stream().findAny();

								offer.ifPresent(merchantOffer -> world.addFreshEntity(new ItemEntity(world, villager.getX(), villager.getY(), villager.getZ(), merchantOffer.getResult())));
							}

							if(!world.isClientSide()){
								playerEntity.giveExperiencePoints(level * ServerConfig.xpGain);
								//                                applyEvilMarker(playerEntity);
							}
						}else if(villagerEntity instanceof WanderingTrader wanderingTrader){
							if(!world.isClientSide()){
								playerEntity.giveExperiencePoints(2 * ServerConfig.xpGain);
								if(world.random.nextInt(100) < 30){
									ItemStack itemStack = wanderingTrader.getOffers().stream().filter(merchantOffer -> merchantOffer.getResult().getItem() != Items.EMERALD).toList().get(wanderingTrader.getRandom().nextInt(wanderingTrader.getOffers().size())).getResult();
									world.addFreshEntity(new ItemEntity(world, wanderingTrader.getX(), wanderingTrader.getY(), wanderingTrader.getZ(), itemStack));
								}
								//                                applyEvilMarker(playerEntity);
							}
						}
					}
				}
			}
			String typeName = ResourceHelper.getKey(livingEntity).toString();
			if(DragonUtils.isDragon(playerEntity) && ServerConfig.royalChaseStatusGivers.contains(typeName)){
				applyEvilMarker(playerEntity);
			}
		}
	}

	public static void applyEvilMarker(Player playerEntity){
		DragonStateProvider.getCap(playerEntity).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				MobEffectInstance effectInstance = playerEntity.getEffect(DragonEffects.ROYAL_CHASE);
				if(effectInstance == null){
					playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(1)));
				}else{
					int duration = effectInstance.getDuration();
					if(duration <= Functions.minutesToTicks(1)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(5), 1));
					}else if(duration <= Functions.minutesToTicks(5)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(10), 2));
					}else if(duration <= Functions.minutesToTicks(10)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(15), 3));
					}else if(duration <= Functions.minutesToTicks(15)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(20), 4));
					}else if(duration <= Functions.minutesToTicks(20)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(25), 5));
					}else if(duration <= Functions.minutesToTicks(25)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(30), 6));
					}else if(duration <= Functions.minutesToTicks(30)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(60), 7));
					}else if(duration <= Functions.minutesToTicks(60)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(120), 8));
					}else if(duration <= Functions.minutesToTicks(120)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(240), 9));
					}else if(duration <= Functions.minutesToTicks(240)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(400), 10));
					}else if(duration <= Functions.minutesToTicks(400)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(600), 11));
					}else if(duration <= Functions.minutesToTicks(600)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(800), 12));
					}else if(duration <= Functions.minutesToTicks(800)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(1000), 13));
					}else if(duration <= Functions.minutesToTicks(1000)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(1200), 14));
					}else if(duration <= Functions.minutesToTicks(1200)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(1500), 15));
					}else if(duration <= Functions.minutesToTicks(1500)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(2000), 16));
					}else if(duration <= Functions.minutesToTicks(2000)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(3000), 17));
					}else if(duration <= Functions.minutesToTicks(3000)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(4000), 18));
					}else if(duration <= Functions.minutesToTicks(4000)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(5000), 19));
					}else if(duration <= Functions.minutesToTicks(5000)){
						playerEntity.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.minutesToTicks(6000), 20));
					}
				}
			}
		});
	}


	@SubscribeEvent
	public static void voidEvilStatus(MobEffectEvent.Added potionAddedEvent){
		MobEffectInstance effectInstance = potionAddedEvent.getEffectInstance();
		LivingEntity livingEntity = potionAddedEvent.getEntity();
		if(effectInstance.getEffect() == MobEffects.HERO_OF_THE_VILLAGE){
			livingEntity.removeEffect(DragonEffects.ROYAL_CHASE);
		}
	}

	@SubscribeEvent
	public static void specialTasks(EntityJoinLevelEvent joinWorldEvent){
		Level world = joinWorldEvent.getLevel();
		Entity entity = joinWorldEvent.getEntity();
		if(entity instanceof IronGolem golemEntity){
			golemEntity.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(golemEntity, Player.class, 0, true, false, livingEntity -> livingEntity.hasEffect(DragonEffects.ROYAL_CHASE)));
		}

		if(entity instanceof AbstractVillager abstractVillager && !(entity instanceof PrinceHorseEntity)){
			abstractVillager.goalSelector.addGoal(10, new AvoidEntityGoal<>(abstractVillager, Player.class, livingEntity -> livingEntity.hasEffect(DragonEffects.ROYAL_CHASE), 16.0F, 1.0D, 1.0D, pMob -> true));
		}
	}

	@SubscribeEvent
	public static void interactions(PlayerInteractEvent.EntityInteract event){
		Player playerEntity = event.getEntity();
		Entity livingEntity = event.getTarget();
		if(livingEntity instanceof AbstractVillager){
			if(playerEntity.hasEffect(DragonEffects.ROYAL_CHASE)){
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void hurtEntity(LivingDamageEvent attackEntityEvent){
		Entity attacked = attackEntityEvent.getEntity();
		Player attacker = attackEntityEvent.getSource().getEntity() instanceof Player ? (Player)attackEntityEvent.getSource().getEntity() : null;

		if(attacker == null){
			return;
		}

		if(attacked instanceof AbstractVillager || attacked instanceof DragonHunter){
			{
				if(attacker.hasEffect(DragonEffects.ROYAL_CHASE)){
					int duration = attacker.getEffect(DragonEffects.ROYAL_CHASE).getDuration();
					int amplifier = attacker.getEffect(DragonEffects.ROYAL_CHASE).getAmplifier();
					attacker.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, duration + Functions.secondsToTicks(5), amplifier));
				}else{
					attacker.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, Functions.secondsToTicks(5)));
				}
			}
		}
	}

	@SubscribeEvent
	public static void spawnHunters(TickEvent.PlayerTickEvent playerTickEvent){
		if(!dragonHunters.isEmpty() && playerTickEvent.phase == TickEvent.Phase.END){
			Player player = playerTickEvent.player;
			if(player.level() instanceof ServerLevel serverLevel && !player.isCreative() && !player.isSpectator() && player.isAlive() && player.hasEffect(DragonEffects.ROYAL_CHASE) && DragonUtils.isDragon(player)){
				if(serverLevel.dimension() == Level.OVERWORLD){
					VillageRelationShips villageRelationShips = DragonUtils.getHandler(player).getVillageRelationShips();
						if(villageRelationShips.hunterSpawnDelay == 0){
							BlockPos spawnPosition = SpawningUtils.findRandomSpawnPosition(player, 1, 4, 14.0F);
							if(spawnPosition != null && spawnPosition.getY() >= ServerConfig.riderSpawnLowerBound && spawnPosition.getY() <= ServerConfig.riderSpawnUpperBound){
								if (serverLevel.getBiome(spawnPosition).is(Tags.Biomes.IS_WATER)) {
									return;
								}
								int levelOfEvil = computeLevelOfEvil(player);
								for(int i = 0; i < levelOfEvil; i++){
									SpawningUtils.spawn(Objects.requireNonNull(dragonHunters.get(serverLevel.random.nextInt(dragonHunters.size())).get().create(serverLevel)), spawnPosition, serverLevel);
								}
								if(serverLevel.isCloseToVillage(player.blockPosition(), 3)){
									villageRelationShips.hunterSpawnDelay = Functions.minutesToTicks(ServerConfig.hunterSpawnDelay / 3) + Functions.minutesToTicks(serverLevel.random.nextInt(ServerConfig.hunterSpawnDelay / 6));
								}else{
									villageRelationShips.hunterSpawnDelay = Functions.minutesToTicks(ServerConfig.hunterSpawnDelay) + Functions.minutesToTicks(serverLevel.random.nextInt(ServerConfig.hunterSpawnDelay / 3));
								}
							}
						}else{
							villageRelationShips.hunterSpawnDelay--;
						}
				}
			}
		}
	}

	public static int computeLevelOfEvil(Player playerEntity){
		if(DragonUtils.isDragon(playerEntity) && playerEntity.hasEffect(DragonEffects.ROYAL_CHASE)){
			MobEffectInstance effectInstance = playerEntity.getEffect(DragonEffects.ROYAL_CHASE);
			assert effectInstance != null;
			int timeLeft = effectInstance.getDuration();
			if(timeLeft >= Functions.minutesToTicks(5000)){
				return 20;
			}
			if(timeLeft >= Functions.minutesToTicks(4000)){
				return 19;
			}
			if(timeLeft >= Functions.minutesToTicks(3000)){
				return 18;
			}
			if(timeLeft >= Functions.minutesToTicks(2000)){
				return 17;
			}
			if(timeLeft >= Functions.minutesToTicks(1500)){
				return 16;
			}
			if(timeLeft >= Functions.minutesToTicks(1200)){
				return 15;
			}
			if(timeLeft >= Functions.minutesToTicks(1000)){
				return 14;
			}
			if(timeLeft >= Functions.minutesToTicks(800)){
				return 13;
			}
			if(timeLeft >= Functions.minutesToTicks(600)){
				return 12;
			}
			if(timeLeft >= Functions.minutesToTicks(400)){
				return 11;
			}
			if(timeLeft >= Functions.minutesToTicks(240)){
				return 10;
			}
			if(timeLeft >= Functions.minutesToTicks(120)){
				return 9;
			}
			if(timeLeft >= Functions.minutesToTicks(60)){
				return 8;
			}
			if(timeLeft >= Functions.minutesToTicks(30)){
				return 7;
			}
			if(timeLeft >= Functions.minutesToTicks(25)){
				return 6;
			}
			if(timeLeft >= Functions.minutesToTicks(20)){
				return 5;
			}
			if(timeLeft >= Functions.minutesToTicks(15)){
				return 4;
			}
			if(timeLeft >= Functions.minutesToTicks(10)){
				return 3;
			}
			if(timeLeft >= Functions.minutesToTicks(5)){
				return 2;
			}
			if(timeLeft >= Functions.minutesToTicks(1)){
				return 1;
			}
		}
		return 0;
	}

	@SubscribeEvent
	public static void spawnPrinceOrPrincess(TickEvent.LevelTickEvent serverTickEvent){
		if(ServerConfig.spawnPrinceAndPrincess){
			Level world = serverTickEvent.level;
			if(world instanceof ServerLevel serverWorld){
				if(!serverWorld.players().isEmpty() && serverWorld.dimension() == Level.OVERWORLD){
					if(timeLeft == 0){
						ServerPlayer player = serverWorld.getRandomPlayer();
						if(player != null && player.isAlive() && !player.isCreative() && !player.isSpectator()){
							BlockPos blockPos = SpawningUtils.findRandomSpawnPosition(player, 1, 2, 20.0F);
							if(blockPos != null && blockPos.getY() >= ServerConfig.riderSpawnLowerBound && blockPos.getY() <= ServerConfig.riderSpawnUpperBound && serverWorld.isVillage(blockPos)){
								if (serverWorld.getBiome(blockPos).is(Tags.Biomes.IS_WATER)) {
									return;
								}
								EntityType<? extends PrincesHorseEntity> entityType = world.random.nextBoolean() ? DSEntities.PRINCESS_ON_HORSE.get() : DSEntities.PRINCE_ON_HORSE.get();
								PrincesHorseEntity princessEntity = entityType.create(world);
								princessEntity.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
								princessEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.NATURAL, null, null);
								serverWorld.addFreshEntity(princessEntity);

								int knights = world.random.nextInt(3) + 3;
								for(int i = 0; i < knights; i++){
									KnightEntity knightHunter = DSEntities.KNIGHT.get().create(serverWorld);
									knightHunter.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
									knightHunter.goalSelector.addGoal(5, new FollowMobGoal(PrincesHorseEntity.class, knightHunter, 8));
									knightHunter.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(player.blockPosition()), MobSpawnType.NATURAL, null, null);
									serverWorld.addFreshEntity(knightHunter);
								}

								timeLeft = Functions.minutesToTicks(ServerConfig.royalSpawnDelay) + Functions.minutesToTicks(world.random.nextInt(ServerConfig.royalSpawnDelay / 2));
							}
						}
					}else{
						timeLeft--;
					}
				}
			}
		}
	}

	/**
	 * Save duration of 'evil dragon'
	 */
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent playerTickEvent){
		if(playerTickEvent.phase == TickEvent.Phase.END){
			Player playerEntity = playerTickEvent.player;
			if(!playerEntity.level().isClientSide()){
				if(playerEntity.hasEffect(DragonEffects.ROYAL_CHASE)){
					DragonUtils.getHandler(playerEntity).getVillageRelationShips().evilStatusDuration = playerEntity.getEffect(DragonEffects.ROYAL_CHASE).getDuration();
				}
			}
		}
	}
}