package by.dragonsurvivalteam.dragonsurvival.magic.common.active;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public abstract class ChargeCastAbility extends ActiveDragonAbility {
	public int castTime = 0;
	public boolean castFinished = false;

	public abstract int getSkillCastingTime();
	
	@Override
	public void onKeyPressed(Player player, Runnable onFinish, long castStartTime, long clientTime){
		if (castFinished)
			return;

		castTime = (int) (clientTime - castStartTime);

		if(castTime >= getSkillCastingTime() && castStartTime != -1 && !castFinished){
			castingComplete(player);
			startCooldown();
			castStartTime = clientTime;
			castFinished = true;

			ManaHandler.consumeMana(player, getManaCost());
			onFinish.run();
		}else{
			saveNBT();
			onCasting(player, castTime);
		}
	}

	@Override
	public void onKeyReleased(Player player){
		castFinished = false;
		castTime = 0;
	}

	public abstract void onCasting(Player player, int currentCastTime);
	public abstract void castingComplete(Player player);

	@Override
	public CompoundTag saveNBT(){
		CompoundTag tag = super.saveNBT();
		tag.putInt("castTime", castTime);
		return tag;
	}

	@Override
	public void loadNBT(CompoundTag nbt){
		super.loadNBT(nbt);
		castTime = nbt.getInt("castTime");
	}

	@Override
	public ArrayList<Component> getInfo(){
		ArrayList<Component> components = super.getInfo();

		if(getSkillCastingTime() > 0)
			components.add(Component.translatable("ds.skill.cast_time", Functions.ticksToSeconds(getSkillCastingTime())));

		return components;
	}

	public int getCastTime() {
		return castTime;
	}
}