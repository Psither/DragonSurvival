package by.dragonsurvivalteam.dragonsurvival.common.dragon_types.bodies;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import net.minecraft.nbt.CompoundTag;

public class CenterBodyType extends AbstractDragonBody {

	@ConfigRange(min = -1.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerJumpBonus", comment = "The jump bonus given to Center-type dragons. It's a very sensitive parameter.")
	public static Double centerJumpBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerStepBonus", comment = "The step bonus given to Center-type dragons")
	public static Double centerStepBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerDamageBonus", comment = "The damage bonus given to Center-type dragons")
	public static Double centerDamageBonus = 0.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerDamageMult", comment = "The damage multiplier given to Center-type dragons")
	public static Double centerDamageMult = 1.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerArmorBonus", comment = "The armor bonus given to Center-type dragons")
	public static Double centerArmorBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerManaBonus", comment = "The mana bonus given to Center-type dragons")
	public static Double centerManaBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerSwimSpeedBonus", comment = "The swimSpeed bonus given to Center-type dragons")
	public static Double centerSwimSpeedBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerHealthBonus", comment = "The health bonus given to Center-type dragons")
	public static Double centerHealthBonus = 0.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerHealthMult", comment = "The health multiplier given to Center-type dragons")
	public static Double centerHealthMult = 1.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerKnockbackBonus", comment = "The knockback bonus given to Center-type dragons")
	public static Double centerKnockbackBonus = 0.0;

	@ConfigRange(min = -10.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerRunMult", comment = "The run speed multiplier given to Center-type dragons")
	public static Double centerRunMult = 1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerExpMult", comment = "The exp multiplier given to Center-type dragons. Can cause dupes with some mods. Increase carefully.")
	public static Double centerExpMult = 1.0;

	@ConfigRange(min = 1.0, max = 10)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerFlightMult", comment = "The flight multiplier given to Center-type dragons. It is not recommended to do less than 1.0 (otherwise your dragon will fall instead of flying upwards)")
	public static Double centerFlightMult = 1.2;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerFlightStaminaMult", comment = "The flightStamina (food waste rate) multiplier given to Center-type dragons. The higher the number, the faster hunger is consumed. It's a very sensitive setting.")
	public static Double centerFlightStaminaMult = 1.0;

	@ConfigRange(min = 0.0, max = 100)
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body", "center"}, key = "centerGravityMult", comment = "The gravity multiplier given to Center-type dragons. The greater the gravity, the faster the dragon will fall during flight and drown faster.")
	public static Double centerGravityMult = 1.0;

	@Override
	public CompoundTag writeNBT() {
		CompoundTag tag = new CompoundTag();
		return tag;
	}

	@Override
	public void readNBT(CompoundTag base) {}

	@Override
	public String getBodyName() {
		return "center";
	}

	public Boolean canHideWings() {
		return false;
	}

	@Override
	public void onPlayerUpdate() {}

	@Override
	public void onPlayerDeath() {}

	public Double getJumpBonus() {
		return centerJumpBonus;
	}
	public Double getStepBonus() {
		return centerStepBonus;
	}
	public Double getDamageBonus() {
		return centerDamageBonus;
	}
	public Double getArmorBonus() {
		return centerArmorBonus;
	}
	public Double getManaBonus() {
		return centerManaBonus;
	}
	public Double getSwimSpeedBonus() {
		return centerSwimSpeedBonus;
	}
	public Double getHealthBonus() {
		return centerHealthBonus;
	}
	public Double getKnockbackBonus() {
		return centerKnockbackBonus;
	}
	public Double getRunMult() {
		return centerRunMult;
	}
	public Double getDamageMult() {
		return centerDamageMult;
	}
	public Double getExpMult() {
		return centerExpMult;
	}
	public Double getFlightMult() {
		return centerFlightMult;
	}
	public Double getFlightStaminaMult() {
		return centerFlightStaminaMult;
	}
	public Double getHealthMult() {
		return centerHealthMult;
	}
	public Double getGravityMult() {
		return centerGravityMult;
	}
}
