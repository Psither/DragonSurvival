package by.dragonsurvivalteam.dragonsurvival.magic.common;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

public abstract class DragonAbility {
	protected static NumberFormat nf = NumberFormat.getInstance();

	public Player player;
	public int level;

	static {
		nf.setMaximumFractionDigits(1);
	}

	public void onKeyPressed(Player player, Runnable onFinish, long castStartTime, long clientTime){}
	public void onKeyReleased(Player player){}

	public Player getPlayer(){
		return player;
	}

	@OnlyIn( Dist.CLIENT )
	public Component getTitle(){
		return Component.translatable("ds.skill." + getName());
	}
	@OnlyIn( Dist.CLIENT )
	public Component getDescription(){
		return Component.translatable("ds.skill.description." + getName());
	}

	public abstract String getName();
	public abstract AbstractDragonType getDragonType();

	@OnlyIn(Dist.CLIENT)
	public abstract ResourceLocation[] getSkillTextures();

	public ResourceLocation getIcon(){
		return getSkillTextures()[Mth.clamp(getLevel(), 0, getSkillTextures().length-1)];
	}

	public int getSortOrder(){
		return 0;
	}

	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getInfo(){ return new ArrayList<>(); }

	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){ return new ArrayList<>(); }

	public CompoundTag saveNBT(){
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("level", level);
		return nbt;
	}

	public void loadNBT(CompoundTag nbt){
		level = nbt.getInt("level");
	}

	public abstract int getMaxLevel();
	public abstract int getMinLevel();

	public boolean isDisabled(){
		if(!ServerConfig.dragonAbilities){
			return true;
		}
		if(Objects.equals(getDragonType(), DragonTypes.CAVE) && !ServerConfig.caveDragonAbilities){
			return true;
		}
		if(Objects.equals(getDragonType(), DragonTypes.SEA) && !ServerConfig.seaDragonAbilities){
			return true;
		}
		return Objects.equals(getDragonType(), DragonTypes.FOREST) && !ServerConfig.forestDragonAbilities;
	}

	public int getLevel(){
		if(isDisabled())
			return 0;

		return level;
	}

	public void setLevel(int level){
		this.level = Mth.clamp(level, getMinLevel(), getMaxLevel());
	}
}