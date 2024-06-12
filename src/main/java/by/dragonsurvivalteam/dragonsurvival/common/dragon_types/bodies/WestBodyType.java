package by.dragonsurvivalteam.dragonsurvival.common.dragon_types.bodies;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import net.minecraft.nbt.CompoundTag;

public class WestBodyType extends AbstractDragonBody {

	@ConfigRange(min = -1.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westJumpBonus", comment = "The jump bonus given to West-type dragons. It's a very sensitive parameter.")
	public static Double westJumpBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westStepBonus", comment = "The step bonus given to West-type dragons")
	public static Double westStepBonus = 1.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westDamageBonus", comment = "The damage bonus given to West-type dragons")
	public static Double westDamageBonus = 0.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westDamageMult", comment = "The damage multiplier given to West-type dragons")
	public static Double westDamageMult = 1.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westArmorBonus", comment = "The armor bonus given to West-type dragons")
	public static Double westArmorBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westManaBonus", comment = "The mana bonus given to West-type dragons")
	public static Double westManaBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westSwimSpeedBonus", comment = "The swimSpeed bonus given to West-type dragons")
	public static Double westSwimSpeedBonus = -0.2;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westHealthBonus", comment = "The health bonus given to West-type dragons")
	public static Double westHealthBonus = 0.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westHealthMult", comment = "The health multiplier given to West-type dragons")
	public static Double westHealthMult = 1.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westKnockbackBonus", comment = "The knockback bonus given to West-type dragons")
	public static Double westKnockbackBonus = 0.5;
	
	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westRunMult", comment = "The run speed multiplier given to West-type dragons")
	public static Double westRunMult = 0.9;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westExpMult", comment = "The exp multiplier given to West-type dragons. Can cause dupes with some mods. Increase carefully.")
	public static Double westExpMult = 1.0;

	@ConfigRange(min = 0.0, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westFlightMult", comment = "The flight multiplier given to West-type dragons. It is not recommended to do less than 1.0 (otherwise your dragon will fall instead of flying upwards)")
	public static Double westFlightMult = 1.2;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westFlightStaminaMult", comment = "The flightStamina (food waste rate) multiplier given to West-type dragons. The higher the number, the faster hunger is consumed. It's a very sensitive setting.")
	public static Double westFlightStaminaMult = 0.2;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "west"}, key = "westGravityMult", comment = "The gravity multiplier given to West-type dragons. The greater the gravity, the faster the dragon will fall during flight and drown faster.")
	public static Double westGravityMult = 1.0;

	@Override
	public CompoundTag writeNBT() {
		CompoundTag tag = new CompoundTag();
		return tag;
	}

	@Override
	public void readNBT(CompoundTag base) {}

	@Override
	public String getBodyName() {
		return "west";
	}

	public Boolean canHideWings() {
		return false;
	}

	@Override
	public void onPlayerUpdate() {}

	@Override
	public void onPlayerDeath() {}

	public Double getJumpBonus() {
		return westJumpBonus;
	}
	public Double getStepBonus() {
		return westStepBonus;
	}
	public Double getDamageBonus() {
		return westDamageBonus;
	}
	public Double getArmorBonus() {
		return westArmorBonus;
	}
	public Double getManaBonus() {
		return westManaBonus;
	}
	public Double getSwimSpeedBonus() {
		return westSwimSpeedBonus;
	}
	public Double getHealthBonus() {
		return westHealthBonus;
	}
	public Double getKnockbackBonus() {
		return westKnockbackBonus;
	}
	public Double getRunMult() {
		return westRunMult;
	}
	public Double getDamageMult() {
		return westDamageMult;
	}
	public Double getExpMult() {
		return westExpMult;
	}
	public Double getFlightMult() {
		return westFlightMult;
	}
	public Double getFlightStaminaMult() {
		return westFlightStaminaMult;
	}
	public Double getHealthMult() {
		return westHealthMult;
	}
	public Double getGravityMult() {
		return westGravityMult;
	}
}
