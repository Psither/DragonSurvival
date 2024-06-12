package by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.active;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.LargeFireParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.SmallFireParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.FireBreathSound;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.SoundRegistry;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigType;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.BurnAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.BreathAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;

import java.util.ArrayList;
import java.util.List;

@RegisterDragonAbility
public class NetherBreathAbility extends BreathAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreath", comment = "Whether the firebreath ability should be enabled" )
	public static Boolean fireBreath = true;

	@ConfigRange( min = 0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathDamage", comment = "The amount of damage the firebreath ability deals. This value is multiplied by the skill level." )
	public static Double fireBreathDamage = 3.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathInitialMana", comment = "The mana cost for starting the firebreath ability" )
	public static Integer fireBreathInitialMana = 2;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathOvertimeMana", comment = "The mana cost of sustaining the firebreath ability" )
	public static Integer fireBreathOvertimeMana = 1;

	@ConfigRange( min = 0.5, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathManaTicks", comment = "How often in seconds, mana is consumed while using fire breath" )
	public static Double fireBreathManaTicks = 2.0;

	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathSpreadsFire", comment = "Whether the fire breath actually spreads fire when used" )
	public static Boolean fireBreathSpreadsFire = true;

	@ConfigType(Block.class)
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathBlockBreaks", comment = "Blocks that have a chance to be broken by fire breath. Formatting: block/modid:id" )
	public static List<String> fireBreathBlockBreaks = List.of("minecraft:impermeable", "minecraft:crops", "minecraft:flowers", "minecraft:replaceable_plants", "minecraft:cobweb");

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathCooldown", comment = "The cooldown in seconds of the fire breath ability" )
	public static Double fireBreathCooldown = 5.0;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathCasttime", comment = "The cast time in seconds of the fire breath ability" )
	public static Double fireBreathCasttime = 1.0;

	@Override
	public String getName(){
		return "nether_breath";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.CAVE;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/nether_breath_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/nether_breath_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/nether_breath_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/nether_breath_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/nether_breath_4.png")};
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(Component.translatable("ds.skill.damage", "+" + fireBreathDamage));
		return list;
	}

	@Override
	public int getMaxLevel(){
		return 4;
	}

	@Override
	public int getMinLevel(){
		return 1;
	}

	@Override
	public boolean isDisabled(){
		return super.isDisabled() || !fireBreath;
	}

	@Override
	public int getManaCost(){
		return fireBreathOvertimeMana;
	}

	@Override
	public Integer[] getRequiredLevels(){
		return new Integer[]{0, 10, 30, 50};
	}

	@Override
	public int getSkillCooldown(){
		return Functions.secondsToTicks(fireBreathCooldown);
	}

	@Override
	public void onBlock(final BlockPos blockPosition, final BlockState blockState, final Direction direction) {
		if (!player.level().isClientSide) {
			if (fireBreathSpreadsFire) {
				BlockPos firePosition = blockPosition.relative(direction);

				if (FireBlock.canBePlacedAt(player.level(), firePosition, direction)) {
					boolean allowPlacement = ForgeEventFactory.getMobGriefingEvent(player.level(), player);

					if (allowPlacement) {
						if (player.getRandom().nextInt(100) < 50) {
							BlockState fireBlockState = FireBlock.getState(player.level(), firePosition);
							player.level().setBlock(firePosition, fireBlockState, Block.UPDATE_ALL_IMMEDIATE);

							blockState.onCaughtFire(player.level(), blockPosition, direction, player);

							if (blockState.getBlock() == Blocks.TNT) {
								player.level().setBlock(blockPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
							}
						}
					}
				}
			}

			BurnAbility burnAbility = DragonAbilities.getSelfAbility(player, BurnAbility.class);

			if (player.getRandom().nextInt(100) < burnAbility.level * 15) {
				BlockState blockAbove = player.level().getBlockState(blockPosition.above());

				if (blockAbove.getBlock() == Blocks.AIR) {
					AreaEffectCloud entity = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, player.level());
					entity.setWaitTime(0);
					entity.setPos(blockPosition.above().getX(), blockPosition.above().getY(), blockPosition.above().getZ());
					entity.setPotion(new Potion(new MobEffectInstance(DragonEffects.BURN, Functions.secondsToTicks(10) * 4))); //Effect duration is divided by 4 normaly
					entity.setDuration(Functions.secondsToTicks(2));
					entity.setRadius(1);
					entity.setParticle(new SmallFireParticleData(37, false));
					entity.setOwner(player);
					player.level().addFreshEntity(entity);
				}
			}
		} else {
			for (int i = 0; i < 4; i++) {
				if (player.getRandom().nextInt(100) < 20) {
					player.level().addParticle(ParticleTypes.LAVA, blockPosition.above().getX(), blockPosition.above().getY(), blockPosition.above().getZ(), 0, 0.05, 0);
				}
			}

			if (blockState.getBlock() == Blocks.WATER) {
				for (int i = 0; i < 4; i++) {
					if (player.getRandom().nextInt(100) < 90) {
						player.level().addParticle(ParticleTypes.BUBBLE_COLUMN_UP, blockPosition.above().getX(), blockPosition.above().getY(), blockPosition.above().getZ(), 0, 0.05, 0);
					}
				}
			}
		}
	}

	@Override
	public void onChanneling(Player player, int castDuration){
		super.onChanneling(player, castDuration);

		if(player.isInWaterRainOrBubble() || player.level().isRainingAt(player.blockPosition())){
			if(player.level().isClientSide()){
				if(player.tickCount % 10 == 0){
					player.playSound(SoundEvents.LAVA_EXTINGUISH, 0.25F, 1F);
				}

				for(int i = 0; i < 12; i++){
					double xSpeed = speed * 1f * xComp;
					double ySpeed = speed * 1f * yComp;
					double zSpeed = speed * 1f * zComp;
					player.level().addParticle(ParticleTypes.SMOKE, dx, dy, dz, xSpeed, ySpeed, zSpeed);
				}
			}
			return;
		}

		if(player.level().isClientSide() && castDuration <= 0){
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> (SafeRunnable)this::sound);
		}

		if(player.level().isClientSide()){
			for(int i = 0; i < calculateNumberOfParticles(DragonUtils.getHandler(player).getSize()); i++){
				double xSpeed = speed * 1f * xComp;
				double ySpeed = speed * 1f * yComp;
				double zSpeed = speed * 1f * zComp;
				player.level().addParticle(new SmallFireParticleData(37, true), dx, dy, dz, xSpeed, ySpeed, zSpeed);
			}

			for(int i = 0; i < calculateNumberOfParticles(DragonUtils.getHandler(player).getSize()) / 2; i++){
				double xSpeed = speed * xComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - xComp * xComp);
				double ySpeed = speed * yComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - yComp * yComp);
				double zSpeed = speed * zComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - zComp * zComp);
				player.level().addParticle(new LargeFireParticleData(37, false), dx, dy, dz, xSpeed, ySpeed, zSpeed);
			}
		}

		hitEntities();

		if(player.tickCount % 10 == 0){
			hitBlocks();
		}
	}

	@OnlyIn( Dist.CLIENT )
	public  void sound(){
		Vec3 pos = player.getEyePosition(1.0F);
		SimpleSoundInstance startingSound = new SimpleSoundInstance(
				SoundRegistry.fireBreathStart,
				SoundSource.PLAYERS,
				1.0F,1.0F,
				SoundInstance.createUnseededRandom(),
				pos.x, pos.y, pos.z
		);
		Minecraft.getInstance().getSoundManager().playDelayed(startingSound, 0);
		Minecraft.getInstance().getSoundManager().stop(new ResourceLocation(DragonSurvivalMod.MODID, "fire_breath_loop"), SoundSource.PLAYERS);
		Minecraft.getInstance().getSoundManager().queueTickingSound(new FireBreathSound(this));
	}

	@OnlyIn( Dist.CLIENT )
	public  void stopSound(){
		if(SoundRegistry.fireBreathEnd != null){
			Vec3 pos = player.getEyePosition(1.0F);
			SimpleSoundInstance endSound = new SimpleSoundInstance(
				SoundRegistry.fireBreathEnd,
				SoundSource.PLAYERS,
				1.0F,1.0F,
				SoundInstance.createUnseededRandom(),
				pos.x, pos.y, pos.z
			);

			Minecraft.getInstance().getSoundManager().playDelayed(endSound, 0);
		}

		Minecraft.getInstance().getSoundManager().stop(new ResourceLocation(DragonSurvivalMod.MODID, "fire_breath_loop"), SoundSource.PLAYERS);
	}

	@Override
	public boolean canHitEntity(LivingEntity entity){
		return (!(entity instanceof Player) || player.canHarmPlayer((Player)entity)) && !entity.fireImmune();
	}

	@Override
	public void onEntityHit(LivingEntity entityHit){
		if(!entityHit.isOnFire()){
			// Short enough fire duration to not cause fire damage but still drop cooked items
			entityHit.setRemainingFireTicks(1);
		}

		super.onEntityHit(entityHit);

		if(!entityHit.level().isClientSide()){
			BurnAbility burnAbility = DragonAbilities.getSelfAbility(player, BurnAbility.class);

			if(entityHit.getRandom().nextInt(100) < burnAbility.level * 15){
				DragonUtils.getEntityHandler(entityHit).lastAfflicted = player != null ? player.getId() : -1;
				entityHit.addEffect(new MobEffectInstance(DragonEffects.BURN, Functions.secondsToTicks(10), 0, false, true));
			}
		}
	}

	@Override
	public void onDamage(LivingEntity entity){
		entity.setSecondsOnFire(30);
	}

	public static float getDamage(int level){
		return (float)(fireBreathDamage * level);
	}

	@Override
	public float getDamage(){
		return getDamage(getLevel());
	}

	@Override
	public int getSkillChargeTime(){
		return Functions.secondsToTicks(fireBreathCasttime);
	}

	@Override
	public int getContinuousManaCostTime() {
		return Functions.secondsToTicks(fireBreathManaTicks);
	}

	@Override
	public int getInitManaCost(){
		return fireBreathInitialMana;
	}

	@Override
	public void castComplete(Player player){
		if(player.level().isClientSide()){
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> (SafeRunnable)this::stopSound);
		}
	}

	@Override
	public boolean requiresStationaryCasting(){
		return false;
	}
}