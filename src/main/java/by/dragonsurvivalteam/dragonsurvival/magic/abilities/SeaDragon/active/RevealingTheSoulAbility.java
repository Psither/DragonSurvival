package by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;

@RegisterDragonAbility
public class RevealingTheSoulAbility extends AoeBuffAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoul", comment = "Whether the revealing The Soul ability should be enabled" )
	public static Boolean revealingTheSoul = true;

	@ConfigRange( min = 1.0, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulDuration", comment = "The duration in seconds of the revealing The Soul effect given when the ability is used" )
	public static Double revealingTheSoulDuration = 200.0;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulCooldown", comment = "The cooldown in seconds of the revealing the soul ability" )
	public static Double revealingTheSoulCooldown = 30.0;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulCasttime", comment = "The cast time in seconds of the revealing the soul ability" )
	public static Double revealingTheSoulCasttime = 1.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulManaCost", comment = "The mana cost for using the revealing The Soul ability" )
	public static Integer revealingTheSoulManaCost = 1;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulMaxEXP", comment = "The max amount of increased exp that can be gained from a single mob with revealing the soul" )
	public static Integer revealingTheSoulMaxEXP = 20;

	@ConfigRange( min = 0, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "revealing_the_soul"}, key = "revealingTheSoulMultiplier", comment = "The multiplier that is applied to exp with revealing the soul, the extra exp is in addition to the normal drops. so 1.0 = 100% increase" )
	public static Double revealingTheSoulMultiplier = 1.0;

	@Override
	public String getName(){
		return "revealing_the_soul";
	}

	@Override
	public int getSortOrder(){
		return 3;
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.SEA;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/revealing_the_soul_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/revealing_the_soul_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/revealing_the_soul_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/revealing_the_soul_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/revealing_the_soul_4.png"),};
	}

	@Override
	public int getMaxLevel(){
		return 4;
	}

	@Override
	public int getMinLevel(){
		return 0;
	}

	@Override
	public int getManaCost(){
		return revealingTheSoulManaCost;
	}

	@Override
	public Integer[] getRequiredLevels(){
		return new Integer[]{0, 25, 40, 60};
	}

	@Override
	public int getSkillCooldown(){
		return Functions.secondsToTicks(revealingTheSoulCooldown);
	}

	@Override
	public int getRange(){
		return 5;
	}

	@Override
	public ParticleOptions getParticleEffect(){
		return DSParticles.magicBeaconParticle;
	}

	@Override
	public MobEffectInstance getEffect(){
		return new MobEffectInstance(DragonEffects.REVEALING_THE_SOUL, Functions.secondsToTicks(revealingTheSoulDuration));
	}

	@Override
	public int getSkillCastingTime(){
		return Functions.secondsToTicks(revealingTheSoulCasttime);
	}

	@Override
	public ArrayList<Component> getInfo(){
		ArrayList<Component> components = super.getInfo();
		components.add(Component.translatable("ds.skill.bonus_exp.multiplier", revealingTheSoulMultiplier + "x"));
		components.add(Component.translatable("ds.skill.bonus_exp.max_gain", Integer.toString(revealingTheSoulMaxEXP)));
		return components;
	}
}