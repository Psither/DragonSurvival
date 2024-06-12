package by.dragonsurvivalteam.dragonsurvival.magic.common.active;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public abstract class ChannelingCastAbility extends ActiveDragonAbility {
	public int chargeTime = 0;

	public abstract int getSkillChargeTime();
	public abstract int getContinuousManaCostTime();
	public abstract int getInitManaCost();
	public long lastManaSpentTime = 0;
	public long timeSinceStartChannel = -1;

	@Override
	public void onKeyPressed(Player player, Runnable onFinish, long castStartTime, long clientTime){
		chargeTime = (int) (clientTime - castStartTime);
		if(clientTime - castStartTime >= getSkillChargeTime() && castStartTime != -1)
		{
			timeSinceStartChannel = clientTime - castStartTime - getSkillChargeTime();
			onChanneling(player, (int)(timeSinceStartChannel));
			if (chargeTime < getSkillChargeTime())
				chargeTime = getSkillChargeTime();

			if (clientTime - lastManaSpentTime >= getContinuousManaCostTime()) {
				lastManaSpentTime = clientTime;
				ManaHandler.consumeMana(player, getManaCost());
			}
		}else{
			onCharging(player, chargeTime);

			if(clientTime - castStartTime >= getSkillChargeTime() / 2 && castStartTime != -1){
				if (clientTime - lastManaSpentTime >= getSkillChargeTime() / 2) {
					ManaHandler.consumeMana(player, getInitManaCost());
					lastManaSpentTime = clientTime;
				}
			}
		}
	}

	@Override
	public boolean canConsumeMana(Player player){
		int manaCost = chargeTime < getSkillChargeTime() / 2 ? getManaCost() + getInitManaCost() : getManaCost();
		return ManaHandler.canConsumeMana(player, manaCost);
	}

	@Override
	public void onKeyReleased(Player player){
		if(chargeTime >= getSkillChargeTime()){
			castComplete(player);
			startCooldown();
		}

		chargeTime = 0;
	}

	public abstract void onCharging(Player player, int currentChargeTime);
	public abstract void onChanneling(Player player, int castDuration);
	public abstract void castComplete(Player player);

	@Override
	public CompoundTag saveNBT(){
		CompoundTag tag = super.saveNBT();
		tag.putInt("chargeTime", chargeTime);
		return tag;
	}

	@Override
	public void loadNBT(CompoundTag nbt){
		super.loadNBT(nbt);
		chargeTime = nbt.getInt("chargeTime");
	}

	@Override
	public ArrayList<Component> getInfo(){
		ArrayList<Component> components = super.getInfo();

		if(getSkillChargeTime() > 0)
			components.add(Component.translatable("ds.skill.cast_time", Functions.ticksToSeconds(getSkillChargeTime())));

		return components;
	}

	public int getChargeTime() {
		return chargeTime;
	}
}