package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class DragonSizeHandler{
	// TODO :: Add timestamp and clear cache
	private static final ConcurrentHashMap<String, Boolean> WAS_DRAGON = new ConcurrentHashMap<>(20);
	private static final ConcurrentHashMap<String, Double> LAST_SIZE = new ConcurrentHashMap<>(20);

	@SubscribeEvent
	public static void getDragonSize(EntityEvent.Size event){
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (!handler.isDragon()) {
			return;
		}

		double size = handler.getSize();
		// Calculate base values
		double height = calculateDragonHeight(size, ServerConfig.hitboxGrowsPastHuman);
		double width = calculateDragonWidth(size, ServerConfig.hitboxGrowsPastHuman);
		double eyeHeight = calculateDragonEyeHeight(size, ServerConfig.hitboxGrowsPastHuman);
		boolean squish = false;
		if (handler.getBody() != null) {
			squish = handler.getBody().isSquish();
			height *= handler.getBody().getHeightMult();
			eyeHeight *= handler.getBody().getEyeHeightMult();
		}
		// Handle Pose stuff
		if(ServerConfig.sizeChangesHitbox){
			Pose overridePose = overridePose(player);
			height = calculateModifiedHeight(height, overridePose, true, squish);
			eyeHeight = calculateModifiedEyeHeight(eyeHeight, overridePose, squish);
			// Apply changes
			event.setNewEyeHeight((float)eyeHeight);
			// Rounding solves floating point issues that caused the dragon to get stuck inside a block at times.
			event.setNewSize(calculateDimensions(width, height));
		}
	}
	
	public static double getDragonHeight(Player player) {
		DragonStateHandler handler = DragonUtils.getHandler(player);
		double height = calculateDragonHeight(handler.getSize(), ServerConfig.hitboxGrowsPastHuman);
		boolean squish = false;
		if (handler.getBody() != null) {
			height *= handler.getBody().getHeightMult();
			squish = handler.getBody().isSquish();
		}
		Pose overridePose = overridePose(player);
		return calculateModifiedHeight(height, overridePose, true, squish);
	}

	public static double calculateDragonHeight(double size, boolean growsPastHuman){
		double height = (size + 4.0D) / 20.0D; // 0.9 -> Config Dragon Max
		if(!growsPastHuman){
			height = 0.9D + 0.9D * (size - 14.0D) / (ServerConfig.maxGrowthSize - 14.0D); // 0.9 -> 1.8 (Min to Human Max)
		}
		return height;
	}

	public static double calculateDragonWidth(double size, boolean growsPastHuman){
		double width = (3.0D * size + 62.0D) / 260.0D; // 0.4 -> Config Dragon Max
		if(!growsPastHuman){
			width = 0.4D + 0.2D * (size - 14.0D) / (ServerConfig.maxGrowthSize - 14.0D); // 0.4 -> 0.6 (Min to Human Max)
		}
		return width;
	}

	public static double calculateDragonEyeHeight(double size, boolean growsPastHuman){
		double eyeHeight = (11.0D * size + 54.0D) / 260.0D; // 0.8 -> Config Dragon Max
		if(!growsPastHuman){
			eyeHeight = 0.8D + 0.8D * (size - 14.0D) / (ServerConfig.maxGrowthSize - 14.0D); // 0.8 -> 1.6 (Min to Human Max)
		}
		return eyeHeight;
	}

	public static double calculateModifiedEyeHeight(double eyeHeight, Pose pose, boolean squish){
		if(pose == Pose.CROUCHING && !squish){
			eyeHeight *= 5.0D / 6.0D;
		}else if(pose == Pose.CROUCHING) {
			eyeHeight *= 3.0D / 6.0D;
		}else if(pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK){
			eyeHeight *= 7.0D / 12.0D;
		}
		return eyeHeight;
	}

	public static EntityDimensions calculateDimensions(double width, double height)
	{
		return new EntityDimensions((float)(Math.round(width * 100.0D) / 100.0D), (float)(Math.round(height * 100.0D) / 100.0D), false);
	}

	public static Pose overridePose(final Player player) {
		if (player == null) {
			return Pose.STANDING;
		}

		Pose overridePose = getOverridePose(player);

		if (player.getForcedPose() != overridePose) {
			player.setForcedPose(overridePose);

			if (player.level().isClientSide() && Minecraft.getInstance().getCameraEntity() != player) {
				player.refreshDimensions();
			}
		}

		return overridePose;
	}

	public static Pose getOverridePose(LivingEntity player){
		if(player != null){
			boolean swimming = (player.isInWaterOrBubble() || player.isInLava() && ServerConfig.bonuses && ServerConfig.caveLavaSwimming && DragonUtils.isDragonType(player, DragonTypes.CAVE)) && player.isSprinting() && !player.isPassenger();
			boolean flying = ServerFlightHandler.isFlying(player);
			boolean spinning = player.isAutoSpinAttack();
			boolean crouching = player.isShiftKeyDown();
			if(flying && !player.isSleeping()){
				return Pose.FALL_FLYING;
			}else if(swimming || (player.isInWaterOrBubble() || player.isInLava()) && !canPoseFit(player, Pose.STANDING) && canPoseFit(player, Pose.SWIMMING)){
				return Pose.SWIMMING;
			}else if(spinning){
				return Pose.SPIN_ATTACK;
			}else if(crouching || !canPoseFit(player, Pose.STANDING) && canPoseFit(player, Pose.CROUCHING)){
				return Pose.CROUCHING;
			}
		}
		return Pose.STANDING;
	}

	public static boolean canPoseFit(LivingEntity player, Pose pose){
		LazyOptional<DragonStateHandler> capability = DragonStateProvider.getCap(player);

		if (!capability.isPresent()){
			return false;
		}
		
		double size = capability.orElseThrow(() -> new IllegalStateException("Dragon State was not valid")).getSize();
		boolean squish = DragonUtils.getDragonBody(player) != null ? DragonUtils.getDragonBody(player).isSquish() : false;
		double height = calculateModifiedHeight(calculateDragonHeight((float)size, ServerConfig.hitboxGrowsPastHuman), pose, ServerConfig.sizeChangesHitbox, squish);
		double width = calculateDragonWidth((float)size, ServerConfig.hitboxGrowsPastHuman);
		return player.level().noCollision(calculateDimensions(width,height).makeBoundingBox(player.position()));
	}

	public static double calculateModifiedHeight(double height, Pose pose, boolean sizeChangesHitbox, boolean squish){
		if(pose == Pose.CROUCHING){
			if(sizeChangesHitbox && !squish){
				height *= 5.0D / 6.0D;
			}else if (sizeChangesHitbox) {
				height *= 3.0D / 6.0D;
			}else{
				height = 1.5D;
			}
		}else if(pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK){
			if(sizeChangesHitbox){
				height *= 7.0D / 12.0D;
			}else{
				height = 0.6D;
			}
		}
		return height;
	}

	@SubscribeEvent
	public static void playerTick(final TickEvent.PlayerTickEvent event) {
		Player player = event.player;

		if (player == null || event.phase == TickEvent.Phase.END || !ServerConfig.sizeChangesHitbox) {
			return;
		}

		// In cases where client and server runs on the same machine
		// Only using the player id results in one side not refreshing the dimensions
		String playerIdSide = player.getId() + event.side.name();

		DragonStateProvider.getCap(player).ifPresent(handler -> {
			if (handler.isDragon()) {
				overridePose(player);

				if (!WAS_DRAGON.getOrDefault(playerIdSide, false)) {
					player.refreshDimensions();
					WAS_DRAGON.put(playerIdSide, true);
				} else if (LAST_SIZE.getOrDefault(playerIdSide, 20.0) != handler.getSize()) {
					player.refreshDimensions();
					LAST_SIZE.put(playerIdSide, handler.getSize());
				}
			} else if (WAS_DRAGON.getOrDefault(playerIdSide, false)) {
				player.setForcedPose(null);
				player.refreshDimensions();
				WAS_DRAGON.put(playerIdSide, false);
			}
		});
	}
}