package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class FireBallEntity extends DragonBallEntity{
	public FireBallEntity(Level p_i50168_9_, LivingEntity p_i50168_2_, double p_i50168_3_, double p_i50168_5_, double p_i50168_7_){
		super(DSEntities.FIREBALL.get(), p_i50168_2_, p_i50168_3_, p_i50168_5_, p_i50168_7_, p_i50168_9_);
	}

	public FireBallEntity(EntityType<? extends Fireball> p_i50166_1_, Level p_i50166_2_){
		super(p_i50166_1_, p_i50166_2_);
	}

	@Override
	protected ParticleOptions getTrailParticle(){
		return ParticleTypes.LARGE_SMOKE;
	}

	@Override
	protected boolean shouldBurn(){
		return false;
	}

	@Override
	protected boolean canSelfDamage(){
		return false;
		// This config would only work if I added a way to ignore the fire resistance of the cave dragon
		// that shot the fireball. Since the default would be to not damage them anyways, I'm not
		// going to bother to implement this for now.
		//return ServerConfig.allowSelfDamageFromFireball;
	}

	@Override
	protected void onHit(HitResult pResult){
		super.onHit(pResult);
		if(!this.level().isClientSide || (getOwner() == null || !getOwner().isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
			this.discard();
		}
	}

	private void onCommonHit() {
		if (!level().isClientSide) {
			float explosivePower = getExplosivePower();
			DamageSource damagesource;
			if(getOwner() == null){
				damagesource = getDamageSource(this, this);
			} else {
				damagesource = getDamageSource(this, getOwner());
			}
			Entity attacker = canSelfDamage() ? this : getOwner();
			level().explode(attacker, damagesource, null, getX(), getY(), getZ(), explosivePower, true, Level.ExplosionInteraction.BLOCK);
			this.discard();
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult pResult){
		super.onHitBlock(pResult);
		onCommonHit();
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource){
		return damageSource.is(DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(damageSource);
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult){
		super.onHitEntity(hitResult);

		// Apply the explosion damage using math from the real explosion formula
		float explosivePower = getSkillLevel();
		// From Explosion.class on line 215 (the left side of the formula is 1.0f if you are at the center of the explosion)
		float damage = 7.0f * explosivePower * 2.0f + 1.0f;

		Entity attacker = getOwner();
		hitResult.getEntity().hurt(getDamageSource(this, attacker), damage);
	}
}