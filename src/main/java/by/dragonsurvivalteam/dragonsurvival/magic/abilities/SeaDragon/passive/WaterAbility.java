package by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive;


import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.PassiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

@RegisterDragonAbility
public class WaterAbility extends PassiveDragonAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "passives"}, key = "water", comment = "Whether the water ability should be enabled" )
	public static Boolean water = true;

	@Override
	public Component getDescription(){
		return Component.translatable("ds.skill.description." + getName(), getDuration() + Functions.ticksToSeconds(ServerConfig.seaTicksWithoutWater));
	}

	@Override
	public int getSortOrder(){
		return 3;
	}

	@Override
	public String getName(){
		return "water";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.SEA;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/water_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/water_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/water_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/water_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/water_4.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/water_5.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/water_6.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/water_7.png")};
	}


	public int getDuration(){
		return 60 * getLevel();
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(Component.translatable("ds.skill.duration.seconds", "+60"));
		return list;
	}

	@Override
	public int getMaxLevel(){
		return 7;
	}

	@Override
	public int getMinLevel(){
		return 0;
	}

	@Override
	public boolean isDisabled(){
		return super.isDisabled() || !water;
	}
}