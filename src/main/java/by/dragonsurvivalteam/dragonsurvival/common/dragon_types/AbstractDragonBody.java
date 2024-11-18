package by.dragonsurvivalteam.dragonsurvival.common.dragon_types;

import by.dragonsurvivalteam.dragonsurvival.common.capability.NBTInterface;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class AbstractDragonBody implements NBTInterface, Comparable<AbstractDragonBody> {
    @Translation(key = "body_affects_hitbox", type = Translation.Type.CONFIGURATION, comments = "If enabled the hitbox will be affected by the body type")
    @ConfigOption(side = ConfigSide.SERVER, category = {"bonuses", "body"}, key = "body_affects_hitbox")
    public static Boolean bodyAffectsHitbox = true;

    public abstract String getBodyName();

    public abstract void onPlayerUpdate();

    public abstract void onPlayerDeath();

    @Override
    public int compareTo(@NotNull AbstractDragonBody b) {
        return getBodyName().compareTo(b.getBodyName());
    }

    public boolean equals(Object object) {
        return super.equals(object) || object instanceof AbstractDragonBody body && body.getBodyName().equals(getBodyName());
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

    public Double getHealthBonus() {
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

    public String getBodyNameLowerCase() {
        return getBodyName().toLowerCase(Locale.ENGLISH);
    }

    public abstract Component translatableName();

    public abstract Component translatableInfo();
}
