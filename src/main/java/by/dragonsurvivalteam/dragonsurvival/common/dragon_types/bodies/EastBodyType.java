package by.dragonsurvivalteam.dragonsurvival.common.dragon_types.bodies;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import net.minecraft.nbt.CompoundTag;

public class EastBodyType extends AbstractDragonBody {

	@ConfigRange(min = -1.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastJumpBonus", comment = "The jump bonus given to East-type dragons. It's a very sensitive parameter.")
	public static Double eastJumpBonus = 0.2;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastStepBonus", comment = "The step bonus given to East-type dragons")
	public static Double eastStepBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastDamageBonus", comment = "The damage bonus given to East-type dragons")
	public static Double eastDamageBonus = -1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastDamageMult", comment = "The damage multiplier given to East-type dragons")
	public static Double eastDamageMult = 1.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastArmorBonus", comment = "The armor bonus given to East-type dragons")
	public static Double eastArmorBonus = 2.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastManaBonus", comment = "The mana bonus given to East-type dragons")
	public static Double eastManaBonus = 2.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastSwimSpeedBonus", comment = "The swimSpeed bonus given to East-type dragons")
	public static Double eastSwimSpeedBonus = 0.5;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastHealthBonus", comment = "The health bonus given to East-type dragons")
	public static Double eastHealthBonus = 0.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastHealthMult", comment = "The health multiplier given to East-type dragons")
	public static Double eastHealthMult = 1.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastKnockbackBonus", comment = "The knockback bonus given to East-type dragons")
	public static Double eastKnockbackBonus = -1.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastRunMult", comment = "The run speed multiplier given to East-type dragons")
	public static Double eastRunMult = 1.1;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastExpMult", comment = "The exp multiplier given to East-type dragons. Can cause dupes with some mods. Increase carefully.")
	public static Double eastExpMult = 1.0;

	@ConfigRange(min = 0.0, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastFlightMult", comment = "The flight multiplier given to East-type dragons. It is not recommended to do less than 1.0 (otherwise your dragon will fall instead of flying upwards)")
	public static Double eastFlightMult = 1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastFlightStaminaMult", comment = "The flightStamina (food waste rate) multiplier given to East-type dragons. The higher the number, the faster hunger is consumed. It's a very sensitive setting.")
	public static Double eastFlightStaminaMult = 1.2;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "east"}, key = "eastGravityMult", comment = "The gravity multiplier given to East-type dragons. The greater the gravity, the faster the dragon will fall during flight and drown faster.")
	public static Double eastGravityMult = 1.0;

	@Override
	public CompoundTag writeNBT() {
		CompoundTag tag = new CompoundTag();
		return tag;
	}

	@Override
	public void readNBT(CompoundTag base) {}

	@Override
	public String getBodyName() {
		return "east";
	}

	@Override
	public void onPlayerUpdate() {}

	@Override
	public void onPlayerDeath() {}

	public Double getJumpBonus() {
		return eastJumpBonus;
	}
	public Double getFlightMult() {
		return eastFlightMult;
	}
	public Double getStepBonus() {
		return eastStepBonus;
	}
	public Double getDamageBonus() {
		return eastDamageBonus;
	}
	public Double getArmorBonus() {
		return eastArmorBonus;
	}
	public Double getManaBonus() {
		return eastManaBonus;
	}
	public Double getSwimSpeedBonus() {
		return eastSwimSpeedBonus;
	}
	public Double getHealthBonus() {
		return eastHealthBonus;
	}
	public Double getKnockbackBonus() {
		return eastKnockbackBonus;
	}
	public Double getRunMult() {
		return eastRunMult;
	}
	public Double getDamageMult() {
		return eastDamageMult;
	}
	public Double getExpMult() {
		return eastExpMult;
	}
	public Double getFlightStaminaMult() {
		return eastFlightStaminaMult;
	}
	public Double getHealthMult() {
		return eastHealthMult;
	}
	public Double getGravityMult() {
		return eastGravityMult;
	}
}
