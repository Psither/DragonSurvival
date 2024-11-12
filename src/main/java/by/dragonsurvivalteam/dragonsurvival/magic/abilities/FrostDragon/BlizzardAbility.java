package by.dragonsurvivalteam.dragonsurvival.magic.abilities.FrostDragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.BlizzardSpikeEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ChargeCastAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.MathUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@RegisterDragonAbility
public class BlizzardAbility extends ChargeCastAbility {
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "blizzard"}, key = "blizzard", comment = "Whether the blizzard ability should be enabled" )
    public static Boolean blizzard = true;

    @ConfigRange( min = 1.0, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "blizzard"}, key = "blizzardDuration", comment = "The duration in seconds of the blizzard effect given when the ability is used" )
    public static Double blizzardDuration = 30.0;

    @ConfigRange( min = 0.05, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "blizzard"}, key = "blizzardCooldown", comment = "The cooldown in seconds of the blizzard ability" )
    public static Double blizzardCooldown = 30.0;

    @ConfigRange( min = 0.05, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "blizzard"}, key = "blizzardCasttime", comment = "The cast time in seconds of the blizzard ability" )
    public static Double blizzardCasttime = 3.0;

    @ConfigRange ( min = 0, max = 10000.0 )
    @ConfigOption ( side = ConfigSide.SERVER, category = {"magic", "abilities", "primordial_dragon", "high_voltage"}, key = "blizzardRange", comment = "How far away the passive damage aura extends" )
    public static double blizzardRange = 5.0;

    @ConfigRange( min = 0.0, max = 100.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "blizzard"}, key = "blizzardDamage", comment = "The damage inflicted per hit.  This value is multiplied by the skill level." )
    public static Double blizzardDamage = 1.0;

    @ConfigRange( min = 0, max = 100 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "blizzard"}, key = "blizzardManaCost", comment = "The mana cost for using the blizzard ability" )
    public static Integer blizzardManaCost = 1;

    public static void inflictDamageOnNearbyEntities(LivingEntity entity, int amp) {
        int range = (int) getRange(amp);
        List<Entity> entities = entity.level().getEntities(null, new AABB(entity.position().x - range, entity.position().y - range, entity.position().z - range, entity.position().x + range, entity.position().y + range, entity.position().z + range));
        entities.removeIf(e -> e.distanceTo(entity) > range);
        entities.removeIf(e -> !(e instanceof LivingEntity));
        entities.removeIf(e -> (e instanceof LivingEntity en && en.getHealth() <= 0));

        for(Entity ent : entities){
            attackTarget(entity, ent, amp);
        }
    }

    public static void attackTarget(LivingEntity source, Entity target, int amp) {
        if (source.tickCount % 5 != 0)
            return;
        ClipContext cc = new ClipContext(source.getPosition(0), target.getPosition(0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source);
        if (target.level().clip(cc).getType() == HitResult.Type.BLOCK) {
            return;
        }

        if (source.level().isClientSide()) {
            // Do something here?
        } else if (target instanceof LivingEntity livingTarget) {
            if (!DragonUtils.isDragonSubtype(livingTarget, DragonTypes.FROST)) {
                Vec3 randLoc = new Vec3(MathUtils.randomPointInSphere(getRange(amp) * 0.5f, source.getRandom()));
                randLoc = randLoc.multiply(1, source.onGround() && randLoc.y < 0 ? -1 : 1, 1);
                Vec3 targetPos = target.position();
                if(target instanceof Player player)
                {
                    DragonStateHandler handler = DragonStateProvider.getData(player);

                    if (!handler.isDragon()) {
                        targetPos = targetPos.add(0, player.getEyeHeight(), 0);
                    } else {
                        targetPos = targetPos.add(0, DragonSizeHandler.calculateDragonEyeHeight(handler, player), 0);
                    }
                }
                Vec3 shootPosition = source.position().add(randLoc);
                Vec3 shootDirectionRaw = targetPos.subtract(shootPosition);
                float distance = (float) shootDirectionRaw.length();

                targetPos = targetPos.add(0, distance / 10.f, 0);
                targetPos = targetPos.add(target.getDeltaMovement().scale(distance / 5.f));

                Vec3 shootDirection = targetPos.subtract(shootPosition).normalize();

                BlizzardSpikeEntity bse = new BlizzardSpikeEntity(source.level());
                bse.setOwner(source);
                bse.setPos(shootPosition);
                bse.setArrow_level(amp + 1);
                bse.setBaseDamage(getDamage(amp));
                bse.pickup = AbstractArrow.Pickup.DISALLOWED;
                bse.shoot(shootDirection.x, shootDirection.y, shootDirection.z, 1.6f, 0.98f);
                source.level().addFreshEntity(bse);

                /*float damage = getDamage(amp);
                if (TargetingFunctions.attackTargets(source, entity -> entity.hurt(DamageSource.FREEZE, damage), target)) {
                    livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().multiply(0.25, 1, 0.25));
                    onHurtTarget(source, livingTarget);
                }*/
            }
        }
    }

    public void onHurtTarget(LivingEntity source, LivingEntity target){

    }

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !blizzard;
    }

    @Override
    public int getSkillCastingTime() {
        return Functions.secondsToTicks(blizzardCasttime);
    }

    @Override
    public void onCasting(Player player, int currentCastTime) {}

    @Override
    public void castingComplete(Player player){
        player.addEffect(new MobEffectInstance(DSEffects.BLIZZARD, getDuration(), getLevel() - 1));
        player.level().playLocalSound(player.position().x, player.position().y + 0.5, player.position().z, SoundEvents.UI_TOAST_IN, SoundSource.PLAYERS, 5F, 0.1F, true);
    }

    public int getDuration() {
        return Functions.secondsToTicks(blizzardDuration);
    }

    public static float getRange(int amp) {
        return (float) (blizzardRange);
    }

    public static float getDamage(int amp) {
        return (float) (blizzardDamage * (amp + 1));
    }

    public float getDamage() {
        return (float) (blizzardDamage * getLevel());
    }

    @Override
    public int getManaCost() {
        return blizzardManaCost;
    }

    @Override
    public Integer[] getRequiredLevels(){
        return new Integer[]{0, 10, 30, 50};
    }

    @Override
    public int getSkillCooldown() {
        return Functions.secondsToTicks(blizzardCooldown);
    }

    @Override
    public String getName() {
        return "blizzard";
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.FROST;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/frost/frost_breath_0.png"),
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/frost/frost_breath_1.png"),
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/frost/frost_breath_2.png"),
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/frost/frost_breath_3.png"),
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/frost/frost_breath_4.png")};
    }

    @Override
    public int getSortOrder() {
        return 4;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMinLevel() {
        return 0;
    }

    @Override
    public boolean requiresStationaryCasting(){return false;}

    @Override
    public AbilityAnimation getLoopingAnimation(){
        return new AbilityAnimation("cast_self_buff", true, false);
    }

    @Override
    public AbilityAnimation getStoppingAnimation(){
        return new AbilityAnimation("self_buff", 0.52 * 20, true, false);
    }
}
