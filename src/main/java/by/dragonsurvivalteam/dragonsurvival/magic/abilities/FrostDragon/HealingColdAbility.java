package by.dragonsurvivalteam.dragonsurvival.magic.abilities.FrostDragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.AoeBuffAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

@RegisterDragonAbility
public class HealingColdAbility extends AoeBuffAbility {
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "healing_cold"}, key = "healingCold", comment = "Whether the healing cold ability should be enabled" )
    public static Boolean healingCold = true;

    @ConfigRange( min = 0.05, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "healing_cold"}, key = "healingColdCooldown", comment = "The cooldown in seconds of the healing cold ability" )
    public static Double healingColdCooldown = 30.0;

    @ConfigRange( min = 0.05, max = 10000 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "healing_cold"}, key = "healingColdCasttime", comment = "The cast time in seconds of the healing cold ability" )
    public static Double healingColdCasttime = 1.0;

    @ConfigRange( min = 1.0, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "healing_cold"}, key = "healingColdDuration", comment = "The duration in seconds of the healing cold effect given when the ability is used" )
    public static Double healingColdDuration = 10.0;

    @ConfigRange( min = 0, max = 100 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "healing_cold"}, key = "healingColdManaCost", comment = "The mana cost for using the healing cold ability" )
    public static Integer healingColdManaCost = 1;

    @ConfigRange( min = 0, max = 100 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "healing_cold"}, key = "healingColdHealStrength", comment = "The health restored by healing cold ability per second, multiplied by level" )
    public static Double healingColdHealStrength = 0.5;

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !healingCold;
    }

    @Override
    public int getRange() {
        return 5;
    }

    @Override
    public ParticleOptions getParticleEffect() {
        return DSParticles.snowflake;
    }

    @Override
    public MobEffectInstance getEffect() {
        return new MobEffectInstance(DragonEffects.HEALING_COLD, getDuration(), getLevel());
    }

    public int getDuration() {
        return Functions.secondsToTicks(healingColdDuration);
    }

    @Override
    public int getSkillCastingTime() {
        return Functions.secondsToTicks(healingColdCasttime);
    }

    @Override
    public int getManaCost() {
        return healingColdManaCost;
    }

    @Override
    public Integer[] getRequiredLevels(){
        return new Integer[]{0, 10, 30, 50};
    }

    @Override
    public int getSkillCooldown() {
        return Functions.secondsToTicks(healingColdCooldown);
    }

    @Override
    public String getName() {
        return "healing_cold";
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
        return 3;
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
