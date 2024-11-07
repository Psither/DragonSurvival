package by.dragonsurvivalteam.dragonsurvival.magic.abilities.FrostDragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.BreathAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;

@RegisterDragonAbility
public class FrostBreathAbility extends BreathAbility {
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "frost_breath"}, key = "frostBreath", comment = "Whether the frost breath ability should be enabled" )
    public static Boolean frostBreath = true;

    @ConfigRange( min = 0.0, max = 100.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "frost_breath"}, key = "frostBreathDamage", comment = "The amount of damage the frost breath ability deals. This value is multiplied by the skill level." )
    public static Double frostBreathDamage = 2.0;

    @ConfigRange( min = 0.05, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "frost_breath"}, key = "frostBreathCooldown", comment = "The cooldown in seconds of the frost breath ability" )
    public static Double frostBreathCooldown = 5.0;

    @ConfigRange( min = 0.05, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "frost_breath"}, key = "frostBreathCasttime", comment = "The casttime in seconds of the frost breath ability" )
    public static Double frostBreathCasttime = 1.0;

    @ConfigRange( min = 0, max = 100 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "frost_breath"}, key = "frostBreathInitialMana", comment = "The mana cost for starting the frost breath ability" )
    public static Integer frostBreathInitialMana = 2;

    @ConfigRange( min = 0, max = 100 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "frost_breath"}, key = "frostBreathOvertimeMana", comment = "The mana cost of sustaining the frost breath ability" )
    public static Integer frostBreathOvertimeMana = 1;

    @ConfigRange( min = 0.5, max = 100.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "frost_breath"}, key = "frostBreathManaTicks", comment = "How often in seconds, mana is consumed while using frost breath" )
    public static Double frostBreathManaTicks = 2.0;

    @ConfigRange( min = 0, max = 100.0 )
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "frost_breath"}, key = "frostBreathBrittlePercent", comment = "How much the victim's armor will be reduced while fully frozen, times level" )
    public static Double frostBreathBrittlePercent = 0.15;

    public ClipContext.Fluid clipContext() {
        return ClipContext.Fluid.NONE;
    }

    public boolean isDisabled() {
        return super.isDisabled() || !frostBreath;
    }

    @Override
    public boolean canHitEntity(LivingEntity entity) {
        return true;
    }

    @Override
    public void onDamage(LivingEntity entity) {
        if (!entity.level.isClientSide()) {
            if (entity.isFullyFrozen()) {
                entity.addEffect(new MobEffectInstance(DragonEffects.FROSTED, Functions.secondsToTicks(4 * getLevel()), getLevel()));
                entity.setTicksFrozen(entity.getTicksFrozen() + getLevel() * 5);
            } else {
                entity.addEffect(new MobEffectInstance(DragonEffects.FROSTED, Functions.secondsToTicks(1.5 * getLevel()), getLevel()));
                entity.setTicksFrozen(entity.getTicksFrozen() + getLevel() * 15);
            }
            entity.addEffect(new MobEffectInstance(DragonEffects.BRITTLE, Functions.secondsToTicks(10 * getLevel()), getLevel()));
        }
    }

    @Override
    public float getDamage() {
        return (float) (frostBreathDamage * getLevel());
    }

    @Override
    public void onBlock(BlockPos pos, BlockState blockState, Direction direction) {

    }

    @Override
    public int getSkillChargeTime() {
        return Functions.secondsToTicks(frostBreathCasttime);
    }

    @Override
    public int getContinuousManaCostTime() {
        return Functions.secondsToTicks(frostBreathManaTicks);
    }

    @Override
    public int getInitManaCost() {
        return frostBreathInitialMana;
    }

    @Override
    public void castComplete(Player player) {

    }

    @Override
    public void onChanneling(Player player, int castDuration) {
        super.onChanneling(player, castDuration);

        if(player.level.isClientSide && castDuration <= 0){
            //DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> (DistExecutor.SafeRunnable)this::sound);
        }

        if(player.level.isClientSide){
            for(int i = 0; i < calculateNumberOfParticles(DragonUtils.getHandler(player).getSize()) / 6; i++){
                double xSpeed = speed * 1f * xComp;
                double ySpeed = speed * 1f * yComp;
                double zSpeed = speed * 1f * zComp;
                player.level.addAlwaysVisibleParticle(DSParticles.snowflake, dx, dy, dz, xSpeed, ySpeed, zSpeed);
            }

            for(int i = 0; i < calculateNumberOfParticles(DragonUtils.getHandler(player).getSize()) / 12; i++){
                double xSpeed = speed * xComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - xComp * xComp);
                double ySpeed = speed * yComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - yComp * yComp);
                double zSpeed = speed * zComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - zComp * zComp);
                player.level.addParticle(DSParticles.snowflake, dx, dy, dz, xSpeed, ySpeed, zSpeed);
            }
        }

        hitEntities();

        if(player.tickCount % 10 == 0){
            hitBlocks();
        }
    }
    @Override
    public int getManaCost() {
        return frostBreathOvertimeMana;
    }

    @Override
    public Integer[] getRequiredLevels(){
        return new Integer[]{0, 10, 30, 50};
    }

    @Override
    public int getSkillCooldown() {
        return Functions.secondsToTicks(frostBreathCooldown);
    }

    @Override
    public String getName() {
        return "frost_breath";
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.FROST;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/frost_breath_0.png"),
                new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/frost_breath_1.png"),
                new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/frost_breath_2.png"),
                new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/frost_breath_3.png"),
                new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/frost_breath_4.png")};
    }

    @Override
    public int getSortOrder() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMinLevel() {
        return 0;
    }
}