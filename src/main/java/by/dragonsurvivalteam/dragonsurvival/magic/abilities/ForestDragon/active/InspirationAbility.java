package by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.active;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.AoeBuffAbility;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@RegisterDragonAbility
public class InspirationAbility extends AoeBuffAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspiration", comment = "Whether the inspiration ability should be enabled" )
	public static Boolean inspiration = true;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspirationCooldown", comment = "The cooldown in seconds of the inspiration ability" )
	public static Double inspirationCooldown = 60.0;

	@ConfigRange( min = 0.05, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspirationCasttime", comment = "The cast time in seconds of the inspiration ability" )
	public static Double inspirationCasttime = 1.0;

	@ConfigRange( min = 1.0, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspirationDuration", comment = "The duration in seconds of the inspiration effect given when the ability is used" )
	public static Double inspirationDuration = 200.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "inspiration"}, key = "inspirationManaCost", comment = "The mana cost for using the inspiration ability" )
	public static Integer inspirationManaCost = 1;

	@Override
	public String getName(){
		return "inspiration";
	}

	@Override
	public int getSortOrder(){
		return 3;
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.FOREST;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/inspiration_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/inspiration_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/inspiration_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/inspiration_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/inspiration_4.png")};
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
		return inspirationManaCost;
	}

	@Override
	public Integer[] getRequiredLevels(){
		return new Integer[]{0, 15, 35, 50};
	}

	@Override
	public int getSkillCooldown(){
		return Functions.secondsToTicks(inspirationCooldown);
	}

	@Override
	public int getRange(){
		return 5;
	}

	@Override
	public ParticleOptions getParticleEffect(){
		return DSParticles.fireBeaconParticle;
	}

	@Override
	public MobEffectInstance getEffect(){
		return new MobEffectInstance(MobEffects.DIG_SPEED, Functions.secondsToTicks(inspirationDuration), 2);
	}

	@Override
	public int getSkillCastingTime(){
		return Functions.secondsToTicks(inspirationCasttime);
	}
}