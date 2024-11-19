package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import com.google.common.base.Objects;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class DragonUtils {
    public static AbstractDragonType getType(Player entity) {
        return DragonStateProvider.getData(entity).getType();
    }

    public static AbstractDragonType getType(DragonStateHandler handler) {
        return handler.getType();
    }

    public static Holder<DragonBody> getBody(Player player) {
        return getBody(DragonStateProvider.getData(player));
    }

    public static Holder<DragonBody> getBody(DragonStateHandler handler) {
        return handler.getBody();
    }

    public static boolean isBody(final DragonStateHandler data, final Holder<DragonBody> typeToCheck) {
        if (data == null) {
            return false;
        }

        return isBody(data.getBody(), typeToCheck);
    }

    public static boolean isBody(final Holder<DragonBody> playerBody, final Holder<DragonBody> typeToCheck) {
        if (playerBody == typeToCheck) {
            return true;
        }

        if (playerBody == null || typeToCheck == null) {
            return false;
        }

        return playerBody.is(typeToCheck);
    }

    public static boolean isType(final Entity entity, final AbstractDragonType typeToCheck) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        return isType(DragonStateProvider.getData(player), typeToCheck);
    }

    public static boolean isType(final DragonStateHandler data, final AbstractDragonType typeToCheck) {
        if (data == null) {
            return false;
        }

        return isType(data.getType(), typeToCheck);
    }

    public static boolean isType(final AbstractDragonType playerType, final AbstractDragonType typeToCheck) {
        if (playerType == typeToCheck) {
            return true;
        }

        if (playerType == null || typeToCheck == null) {
            return false;
        }

        // FIXME :: equals checks sub type name - here we explicitly check the "base" type name - could that cause issues somewhere?
        return Objects.equal(playerType.getTypeName(), typeToCheck.getTypeName());
    }

    public static DragonLevel getLevel(Player entity) {
        return DragonStateProvider.getData(entity).getLevel();
    }

    public static boolean isNearbyDragonPlayerToEntity(double detectionRadius, Level level, Entity entity) {
        List<Player> players = level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(detectionRadius));

        for (Player player : players) {
            if (DragonStateProvider.isDragon(player)) {
                return true;
            }
        }
        return false;
    }
}