package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import com.google.common.base.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

import java.util.List;
import javax.annotation.Nullable;

public class DragonUtils {
    public static AbstractDragonType getDragonType(Player entity) {
        return DragonStateProvider.getData(entity).getType();
    }

    public static AbstractDragonType getDragonType(DragonStateHandler handler) {
        return handler.getType();
    }

    public static AbstractDragonBody getDragonBody(Player entity) {
        return DragonStateProvider.getData(entity).getBody();
    }

    public static AbstractDragonBody getDragonBody(DragonStateHandler handler) {
        return handler.getBody();
    }

    public static boolean isBodyType(final DragonStateHandler data, final AbstractDragonBody typeToCheck) {
        if (data == null || typeToCheck == null) {
            return false;
        }

        return isBodyType(data.getBody(), typeToCheck);
    }

    public static boolean isBodyType(final AbstractDragonBody playerType, final AbstractDragonBody typeToCheck) {
        if (playerType == null || typeToCheck == null) {
            return false;
        }

        return Objects.equal(playerType, typeToCheck);
    }

    public static boolean isDragonType(final Entity entity, final AbstractDragonType typeToCheck) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        return isDragonType(DragonStateProvider.getData(player), typeToCheck);
    }

    public static boolean isDragonType(final DragonStateHandler data, final AbstractDragonType typeToCheck) {
        if (data == null || typeToCheck == null || data.getType() == null) {
            return false;
        }

        return isDragonType(data.getType(), typeToCheck);
    }

    public static boolean isDragonType(final AbstractDragonType playerType, final AbstractDragonType typeToCheck) {
        if (playerType == null || typeToCheck == null) {
            return false;
        }

        // FIXME :: equals checks sub type name - here we explicitly check the "base" type name - could that cause issues somewhere?
        return Objects.equal(playerType.getTypeName(), typeToCheck.getTypeName());
    }

    public static boolean isDragonSubtype(final Entity entity, final AbstractDragonType typeToCheck) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        return isDragonSubtype(DragonStateProvider.getData(player), typeToCheck);
    }

    public static boolean isDragonSubtype(final DragonStateHandler data, final AbstractDragonType typeToCheck) {
        if (data == null || typeToCheck == null || data.getType() == null) {
            return false;
        }

        return isDragonSubtype(data.getType(), typeToCheck);
    }

    public static boolean isDragonSubtype(final AbstractDragonType playerType, final AbstractDragonType typeToCheck) {
        if (playerType == null || typeToCheck == null) {
            return false;
        }

        return Objects.equal(playerType.getSubtypeName(), typeToCheck.getSubtypeName());
    }

    public static DragonLevel getDragonLevel(Player entity) {
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

    /**
     * Converts the supplied harvest level to a corresponding vanilla tier
     */
    public static @Nullable Tier levelToVanillaTier(int level) {
        if (level < 0) {
            return null;
        } else if (level == 0) {
            return Tiers.WOOD;
        } else if (level == 1) {
            return Tiers.STONE;
        } else if (level == 2) {
            return Tiers.IRON;
        } else if (level == 3) {
            return Tiers.DIAMOND;
        }

        return Tiers.NETHERITE;
    }
}