package by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive;


import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.PassiveDragonAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

@RegisterDragonAbility
public class CliffhangerAbility extends PassiveDragonAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "passives"}, key = "cliffHanger", comment = "Whether the cliffhanger ability should be enabled" )
	public static Boolean cliffHanger = true;

	@Override
	public Component getDescription(){
		return Component.translatable("ds.skill.description." + getName(), 3 + getHeight() + ServerConfig.forestFallReduction);
	}

	@Override
	public int getSortOrder(){
		return 4;
	}

	@Override
	public String getName(){
		return "cliffhanger";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.FOREST;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/cliffhanger_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/cliffhanger_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/cliffhanger_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/cliffhanger_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/cliffhanger_4.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/cliffhanger_5.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/cliffhanger_6.png")};
	}

	public int getHeight(){
		return getLevel();
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(Component.translatable("ds.skill.range.blocks", "+1"));
		return list;
	}

	@Override
	public int getMaxLevel(){
		return 6;
	}

	@Override
	public int getMinLevel(){
		return 0;
	}

	@Override
	public boolean isDisabled(){
		return super.isDisabled() || !cliffHanger;
	}
}