package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;


import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.Animation.LoopType;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.misc.NotNull;

import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;


public abstract class DragonBallEntity extends Fireball implements GeoEntity {
	public static final EntityDataAccessor<Integer> SKILL_LEVEL = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Float> MOVE_DISTANCE = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.FLOAT);
	public static final float DRAGON_BALL_DISTANCE = 32.f;
	public static final int MAX_LIFESPAN = 1200; // 60 seconds
	private boolean hasExploded = false;
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public DragonBallEntity(EntityType<? extends Fireball> p_i50168_1_, LivingEntity p_i50168_2_, double p_i50168_3_, double p_i50168_5_, double p_i50168_7_, Level p_i50168_9_){
		super(p_i50168_1_, p_i50168_2_, p_i50168_3_, p_i50168_5_, p_i50168_7_, p_i50168_9_);
	}

	public DragonBallEntity(EntityType<? extends Fireball> p_i50166_1_, Level p_i50166_2_){
		super(p_i50166_1_, p_i50166_2_);
	}

	public int getSkillLevel() {
		return entityData.get(SKILL_LEVEL);
	}

	public float getExplosivePower(){
		return getSkillLevel();
	}

	public void setLevel(int level){
		entityData.set(SKILL_LEVEL, level);
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		entityData.define(SKILL_LEVEL, 1);
		entityData.define(MOVE_DISTANCE, 0f);
		entityData.define(LIFESPAN, 0);
	}

	@Override
	public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected boolean canHitEntity(Entity p_230298_1_){
		return true;
	}

	protected boolean canSelfDamage(){
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		if(!this.level().isClientSide || (getOwner() == null || !getOwner().isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
			entityData.set(MOVE_DISTANCE, entityData.get(MOVE_DISTANCE) + (float)getDeltaMovement().length());
			entityData.set(LIFESPAN, entityData.get(LIFESPAN) + 1);
		}
		if (entityData.get(MOVE_DISTANCE) > DRAGON_BALL_DISTANCE || entityData.get(LIFESPAN) > MAX_LIFESPAN) {
			this.onHit(ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity));
		}
	}

	protected DamageSource getDamageSource(Fireball pFireball, @Nullable Entity pIndirectEntity){
		return pFireball.damageSources().fireball(this, pIndirectEntity);
		//return DamageSource.fireball(pFireball, pIndirectEntity);
	}


	public PlayState predicate(final AnimationState<DragonBallEntity> state) {
		state.getController().setAnimation(IDLE);
		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(final AnimatableManager.ControllerRegistrar registrar){
		registrar.add(new AnimationController<>(this, "everything", this::predicate));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	private static final RawAnimation EXPLOSION = RawAnimation.begin().thenLoop("explosion");
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
}