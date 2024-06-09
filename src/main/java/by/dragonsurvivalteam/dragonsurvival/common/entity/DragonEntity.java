package by.dragonsurvivalteam.dragonsurvival.common.entity;

import by.dragonsurvivalteam.dragonsurvival.api.DragonFood;
import by.dragonsurvivalteam.dragonsurvival.client.emotes.Emote;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTimer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.CommonTraits;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.EmoteCap;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.common.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.ISecondAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.controller.AnimationController.IAnimationPredicate;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DragonEntity extends LivingEntity implements IAnimatable, CommonTraits{
	public final ArrayList<Double> bodyYawAverage = new ArrayList<>();
	public final ArrayList<Double> headYawAverage = new ArrayList<>();
	public final ArrayList<Double> headPitchAverage = new ArrayList<>();
	public final ArrayList<Double> tailSideAverage = new ArrayList<>();
	public final ArrayList<Double> tailUpAverage = new ArrayList<>();
	/** This reference must be updated whenever player is remade, for example, when changing dimensions */
	public volatile Integer playerId; // TODO :: Use string uuid?
	public boolean neckLocked = false;
	public boolean tailLocked = false;
	public float prevZRot;
	public float prevXRot;

	//Molang queries
	public double tailMotionSide;
	public double tailMotionUp;
	public double body_yaw_change = 0;
	public double head_yaw_change = 0;
	public double head_pitch_change = 0;
	public double tail_motion_up = 0;
	public double tail_motion_side = 0;
	AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
	ActiveDragonAbility lastCast = null;
	boolean started, ended;
	AnimationTimer animationTimer = new AnimationTimer();
	Emote lastEmote;
	public double seekTime; // Copied from DragonModel.java in order to calculate the correct tickOffset when speed is adjusted for an animation.

	public DragonEntity(EntityType<? extends LivingEntity> type, Level worldIn){
		super(type, worldIn);
	}

	public Vec3 getPseudoDeltaMovement() {
		Entity entity = level.getEntity(playerId);

		if (entity instanceof Player player) {
			return getPseudoDeltaMovement(player);
		}

		return new Vec3(0, 0, 0);
	}

	// TODO :: Not really needed while SyncFlightSpeed packet is constantly being synced
	public Vec3 getPseudoDeltaMovement(final Player player) {
		if (player == null) {
			return new Vec3(0, 0, 0);
		}

		if (true/*player == Minecraft.getInstance().player*/) {
			return player.getDeltaMovement();
		}

		return new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
	}

	@Override
	public void registerControllers(AnimationData animationData){

		for(int i = 0; i < EmoteCap.MAX_EMOTES; i++){
			int finalI = i;
			IAnimationPredicate<DragonEntity> predicate = s -> emotePredicate(finalI, s);
			animationData.addAnimationController(new AnimationController(this, "2_" + i, 0, predicate));
		}

		animationData.addAnimationController(new AnimationController(this, "3", 2, this::predicate));
		animationData.addAnimationController(new AnimationController(this, "4", 0, this::bitePredicate));
		animationData.addAnimationController(new AnimationController(this, "5", 0, this::tailPredicate));
		animationData.addAnimationController(new AnimationController(this, "1", 0, this::headPredicate));
	}

	private <E extends IAnimatable> PlayState tailPredicate(AnimationEvent<E> animationEvent){
		if(!tailLocked || !ClientConfig.enableTailPhysics){
			animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("tail_turn", EDefaultLoopTypes.LOOP));
			return PlayState.CONTINUE;
		}else{
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}
	}

	private <E extends IAnimatable> PlayState headPredicate(AnimationEvent<E> animationEvent){
		if(!neckLocked){
			animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("head_turn", EDefaultLoopTypes.LOOP));
			return PlayState.CONTINUE;
		}else{
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}
	}

	private <E extends IAnimatable> PlayState bitePredicate(AnimationEvent<E> animationEvent){
		Player player = getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);
		AnimationBuilder builder = new AnimationBuilder();

		ActiveDragonAbility curCast = handler.getMagicData().getCurrentlyCasting();

		if(curCast instanceof ISecondAnimation || lastCast instanceof ISecondAnimation)
			renderAbility(builder, curCast);

		if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item") && (player.isUsingItem() || (handler.getMovementData().bite || handler.getMovementData().dig) && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()))){
			builder.addAnimation("use_item", EDefaultLoopTypes.LOOP);
			handler.getMovementData().bite = false;
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("eat_item_right") && player.isUsingItem() && DragonFood.isEdible(this.getItemInHand(InteractionHand.MAIN_HAND).getItem(), player) || animationTimer.getDuration("eat_item_right") > 0){
			if(animationTimer.getDuration("eat_item_right") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("eat_item_right", 0.32 * 20, builder);
			}

			builder.addAnimation("eat_item_right", EDefaultLoopTypes.LOOP);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("eat_item_left") && player.isUsingItem() && DragonFood.isEdible(this.getItemInHand(InteractionHand.OFF_HAND).getItem(), player) || animationTimer.getDuration("eat_item_right") > 0){
			if(animationTimer.getDuration("eat_item_left") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("eat_item_left", 0.32 * 20, builder);
			}

			builder.addAnimation("eat_item_left", EDefaultLoopTypes.LOOP);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item_right") && !player.getMainHandItem().isEmpty() && handler.getMovementData().bite && player.getMainArm() == HumanoidArm.RIGHT || animationTimer.getDuration("use_item_right") > 0){
			if(animationTimer.getDuration("use_item_right") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("use_item_right", 0.32 * 20, builder);
			}

			builder.addAnimation("use_item_right", EDefaultLoopTypes.LOOP);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item_left") && !player.getOffhandItem().isEmpty() && handler.getMovementData().bite && player.getMainArm() == HumanoidArm.LEFT || animationTimer.getDuration("use_item_left") > 0){
			if(animationTimer.getDuration("use_item_left") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("use_item_left", 0.32 * 20, builder);
			}

			builder.addAnimation("use_item_left", EDefaultLoopTypes.LOOP);
		}else if(handler.getMovementData().bite && !handler.getMovementData().dig || animationTimer.getDuration("bite") > 0){
			builder.addAnimation("bite", EDefaultLoopTypes.LOOP);
			if(animationTimer.getDuration("bite") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("bite", 0.44 * 20, builder);
			}
		}

		if(builder.getRawAnimationList().size() > 0){
			animationEvent.getController().setAnimation(builder);
			return PlayState.CONTINUE;
		}

		return PlayState.STOP;
	}

	public static boolean animationExists(String key){
		Animation animation = GeckoLibCache.getInstance().getAnimations().get(ClientDragonRender.dragonModel.getAnimationResource(ClientDragonRender.dragonArmor)).getAnimation(key);

		return animation != null;
	}

	private <E extends IAnimatable> PlayState emotePredicate(int num, AnimationEvent<E> animationEvent){
		final Player player = getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if(handler.getEmoteData().currentEmotes[num] != null){
			Emote emote = handler.getEmoteData().currentEmotes[num];

			neckLocked = false;
			tailLocked = false;

			animationEvent.getController().animationSpeed = emote.speed;

			if(emote.animation != null && !emote.animation.isEmpty()){
				animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation(emote.animation, emote.loops ? EDefaultLoopTypes.LOOP : EDefaultLoopTypes.PLAY_ONCE));
				lastEmote = emote;
				return PlayState.CONTINUE;
			}
		}

		return PlayState.STOP;
	}

	public @Nullable Player getPlayer(){
		return (Player) level.getEntity(playerId);
	}

	@Override
	public AnimationFactory getFactory(){
		return animationFactory;
	}

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> animationEvent){
		animationEvent.getAnimationTick();
		Player player = getPlayer();
		AnimationController animationController = animationEvent.getController();
		DragonStateHandler playerStateHandler = DragonUtils.getHandler(player);

		AnimationBuilder builder = new AnimationBuilder();

		double animationSpeed = 1;

		if(player == null || Stream.of(playerStateHandler.getEmoteData().currentEmotes).anyMatch(s -> s != null && !s.blend && s.animation != null && !s.animation.isBlank())){
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}

		neckLocked = false;
		tailLocked = false;

		Vec3 deltaMovement = getPseudoDeltaMovement(player);
		ActiveDragonAbility curCast = playerStateHandler.getMagicData().getCurrentlyCasting();

		if(!(curCast instanceof ISecondAnimation) && !(lastCast instanceof ISecondAnimation)){
			renderAbility(builder, curCast);
		}

		Vec3 motion = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
		boolean isMovingHorizontal = Math.sqrt(Math.pow(motion.x, 2) + Math.pow(motion.z, 2)) > 0.005;

		if(playerStateHandler.getMagicData().onMagicSource){
			neckLocked = false;
			tailLocked = false;
			builder.addAnimation("sit_on_magic_source", EDefaultLoopTypes.LOOP);
		}else if(player.isSleeping() || playerStateHandler.treasureResting){
			neckLocked = false;
			tailLocked = false;
			builder.addAnimation("sleep", EDefaultLoopTypes.LOOP);
		}else if(player.isPassenger()){
			neckLocked = false;
			tailLocked = false;
			builder.addAnimation("sit", EDefaultLoopTypes.LOOP);
		}else if(player.getAbilities().flying || ServerFlightHandler.isFlying(player)){
			if(ServerFlightHandler.isGliding(player) || playerStateHandler.isDiving()){
				neckLocked = false;
				tailLocked = false;
				if(ServerFlightHandler.isSpin(player)){
					neckLocked = false;
					tailLocked = false;
					animationSpeed = 2;
					RenderingUtils.addAnimation(builder, "fly_spin", EDefaultLoopTypes.LOOP, 2, animationController);
				}else if(deltaMovement.y < -1){
					RenderingUtils.addAnimation(builder, "fly_dive_alt", EDefaultLoopTypes.LOOP, 4, animationController);
				}else if(deltaMovement.y < -0.25){
					RenderingUtils.addAnimation(builder, "fly_dive", EDefaultLoopTypes.LOOP, 4, animationController);
				}else if(deltaMovement.y > 0.5){
					animationSpeed = 1.5;
					RenderingUtils.addAnimation(builder, "fly", EDefaultLoopTypes.LOOP, 2, animationController);
				}else{
					RenderingUtils.addAnimation(builder, "fly_soaring", EDefaultLoopTypes.LOOP, 4, animationController);
				}
			}else{
				if(player.isCrouching() && deltaMovement.y < 0 && ServerFlightHandler.distanceFromGround(player) < 10 && deltaMovement.length() < 4){
					neckLocked = false;
					tailLocked = false;
					RenderingUtils.addAnimation(builder, "fly_land", EDefaultLoopTypes.LOOP, 2, animationController);
				} else if(ServerFlightHandler.isSpin(player)){
					neckLocked = false;
					tailLocked = false;
					RenderingUtils.addAnimation(builder, "fly_spin", EDefaultLoopTypes.LOOP, 2, animationController);
				}else{
					neckLocked = false;
					tailLocked = false;
					if(deltaMovement.y > 0) {
						animationSpeed = 2;
					} else {
						animationSpeed = 1;
					}
					RenderingUtils.addAnimation(builder, "fly", EDefaultLoopTypes.LOOP, 2, animationController);
				}
			}
		}else if(player.getPose() == Pose.SWIMMING){
			if(ServerFlightHandler.isSpin(player)){
				neckLocked = false;
				tailLocked = false;
				animationSpeed = 1;
				RenderingUtils.addAnimation(builder, "fly_spin", EDefaultLoopTypes.LOOP, 2, animationController);
			}else{
				animationSpeed = 1 + deltaMovement.horizontalDistance() / 10;
				RenderingUtils.addAnimation(builder, "swim_fast", EDefaultLoopTypes.LOOP, 2, animationController);
			}
		}else if((player.isInLava() || player.isInWaterOrBubble()) && !player.isOnGround()){
			if(ServerFlightHandler.isSpin(player)){
				neckLocked = false;
				tailLocked = false;
				animationSpeed = 2;
				RenderingUtils.addAnimation(builder, "fly_spin", EDefaultLoopTypes.LOOP, 2, animationController);
			}else{
				animationSpeed = 1 + deltaMovement.horizontalDistance() / 10;
				RenderingUtils.addAnimation(builder, "swim", EDefaultLoopTypes.LOOP, 2, animationController);
			}
		}else if(animationController.getCurrentAnimation() != null && (Objects.equals(animationController.getCurrentAnimation().animationName, "fly_land"))) {
			RenderingUtils.addAnimation(builder, "fly_land_end", EDefaultLoopTypes.PLAY_ONCE, 2, animationController);
		} else if(animationController.getCurrentAnimation() != null && Objects.equals(animationController.getCurrentAnimation().animationName, "fly_land_end")) {
			// Don't add any animation
		}else if(ClientEvents.dragonsJumpingTicks.getOrDefault(this.playerId, 0) > 0){
			RenderingUtils.addAnimation(builder, "jump", EDefaultLoopTypes.PLAY_ONCE, 2, animationController);
		}else if(!player.isOnGround() ) {
			RenderingUtils.addAnimation(builder, "fall_loop", EDefaultLoopTypes.LOOP, 2, animationController);
		} else if(player.isShiftKeyDown() || !DragonSizeHandler.canPoseFit(player, Pose.STANDING) && DragonSizeHandler.canPoseFit(player, Pose.CROUCHING)){
			// Player is Sneaking
			if(isMovingHorizontal && player.animationSpeed != 0f){
				RenderingUtils.addAnimation(builder, "sneak_walk", EDefaultLoopTypes.LOOP, 5, animationController);
			}else if(playerStateHandler.getMovementData().dig){
				RenderingUtils.addAnimation(builder, "dig_sneak", EDefaultLoopTypes.LOOP, 5, animationController);
			}else{
				RenderingUtils.addAnimation(builder, "sneak", EDefaultLoopTypes.LOOP, 5, animationController);
			}
		}else if(player.isSprinting()){
			RenderingUtils.setAnimationSpeed(1 + deltaMovement.horizontalDistance() / 10, seekTime, animationController);
			RenderingUtils.addAnimation(builder, "run", EDefaultLoopTypes.LOOP, 2, animationController);
		}else if(isMovingHorizontal && player.animationSpeed != 0f){
			RenderingUtils.setAnimationSpeed(1 + deltaMovement.horizontalDistance() / 10, seekTime, animationController);
			RenderingUtils.addAnimation(builder, "walk", EDefaultLoopTypes.LOOP, 2, animationController);
		}else if(playerStateHandler.getMovementData().dig){
			RenderingUtils.addAnimation(builder, "dig", EDefaultLoopTypes.LOOP, 2, animationController);
		} else {
			RenderingUtils.addAnimation(builder, "idle", EDefaultLoopTypes.LOOP, 2, animationController);
		}

		if(animationController.getAnimationState() == AnimationState.Stopped) {
			RenderingUtils.addAnimation(builder, "idle", EDefaultLoopTypes.LOOP, 2, animationController);
		}

		animationController.setAnimation(builder);
		if(animationController.getAnimationState() != AnimationState.Transitioning) {
			RenderingUtils.setAnimationSpeed(animationSpeed, animationEvent.getAnimationTick(), animationController);
		}

		return PlayState.CONTINUE;
	}

	private void renderAbility(AnimationBuilder builder, ActiveDragonAbility curCast){
		if(curCast != null && lastCast == null){
			if(curCast.getStartingAnimation() != null){
				AbilityAnimation starAni = curCast.getStartingAnimation();
				neckLocked = starAni.locksNeck;
				tailLocked = starAni.locksTail;

				if(!started){
					animationTimer.putAnimation(starAni.animationKey, starAni.duration, builder);
					started = true;
				}

				builder.addAnimation(starAni.animationKey);

				if(animationTimer.getDuration(starAni.animationKey) <= 0){
					lastCast = curCast;
					started = false;
				}
			}else if(curCast.getLoopingAnimation() != null){
				AbilityAnimation loopingAni = curCast.getLoopingAnimation();
				neckLocked = loopingAni.locksNeck;
				tailLocked = loopingAni.locksTail;

				lastCast = curCast;
				builder.addAnimation(loopingAni.animationKey, EDefaultLoopTypes.LOOP);
			}
		}else if(curCast != null){
			lastCast = curCast;

			if(curCast.getLoopingAnimation() != null){
				AbilityAnimation loopingAni = curCast.getLoopingAnimation();
				neckLocked = loopingAni.locksNeck;
				tailLocked = loopingAni.locksTail;

				builder.addAnimation(loopingAni.animationKey, EDefaultLoopTypes.LOOP);
			}
		}else if(lastCast != null){
			if(lastCast.getStoppingAnimation() != null){
				AbilityAnimation stopAni = lastCast.getStoppingAnimation();
				neckLocked = stopAni.locksNeck;
				tailLocked = stopAni.locksTail;

				if(!ended){
					animationTimer.putAnimation(stopAni.animationKey, stopAni.duration, builder);
					ended = true;
				}

				builder.addAnimation(stopAni.animationKey);

				if(animationTimer.getDuration(stopAni.animationKey) <= 0){
					lastCast = null;
					ended = false;
				}
			}else{
				lastCast = null;
			}
		}
	}

	@Override
	public Iterable<ItemStack> getArmorSlots(){
		return playerId != null ? getPlayer().getArmorSlots() : List.of();
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slotIn){
		return playerId != null ? getPlayer().getItemBySlot(slotIn) : ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlot slotIn, ItemStack stack){
		if(playerId != null){
			getPlayer().setItemSlot(slotIn, stack);
		}
	}

	@Override
	public HumanoidArm getMainArm(){
		return playerId != null ? getPlayer().getMainArm() : HumanoidArm.LEFT;
	}
}