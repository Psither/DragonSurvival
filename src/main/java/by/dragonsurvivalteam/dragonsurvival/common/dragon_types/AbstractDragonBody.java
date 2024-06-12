package by.dragonsurvivalteam.dragonsurvival.common.dragon_types;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import by.dragonsurvivalteam.dragonsurvival.common.capability.NBTInterface;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;

public abstract class AbstractDragonBody implements NBTInterface, Comparable<AbstractDragonBody> {
	@ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body"}, key = "bodyAffectsHitbox", comment = "Whether the hitbox is affected by your body type")
	public static Boolean bodyAffectsHitbox = true;

	public abstract String getBodyName();
	public abstract void onPlayerUpdate();
	public abstract void onPlayerDeath();
	
	public int compareTo(@NotNull AbstractDragonBody b) {
		return getBodyName().compareTo(b.getBodyName());
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof AbstractDragonBody b) {
			return Objects.equals(getBodyName(), b.getBodyName());
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return getBodyName();
	}
	
	public Boolean canHideWings() {
		return true;
	}

	public Double getHeightMult() {
		return 1.0;
	}
	
	public Boolean isSquish() {
		return false;
	}

	public Double getEyeHeightMult() {
		return 1.0;
	}

	public Double getJumpBonus() {
		return 0.0;
	}
	
	public Double getStepBonus() {
		return 0.0;
	}
	
	public Double getDamageBonus() {
		return 0.0;
	}
	
	public Double getArmorBonus() {
		return 0.0;
	}
	
	public Double getManaBonus() {
		return 0.0;
	}

	public Double getSwimSpeedBonus() {
		return 0.0;
	}
	
	public Double getKnockbackBonus() {
		return 0.0;
	}
	
	public Double getRunMult() {
		return 1.0;
	}

	public Double getDamageMult() {
		return 1.0;
	}
	
	public Double getExpMult() {
		return 1.0;
	}

	public Double getFlightMult() {
		return 1.0;
	}

	public Double getFlightStaminaMult() {
		return 1.0;
	}

	public Double getHealthMult() {
		return 1.0;
	}

	public Double getGravityMult() {
		return 1.0;
	}
}
