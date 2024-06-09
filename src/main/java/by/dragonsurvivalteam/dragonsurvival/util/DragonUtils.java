package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import com.google.common.base.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class DragonUtils {
	public static DragonStateHandler getHandler(final Entity entity) {
		if (entity == null) {
			return new DragonStateHandler();
		}

		LazyOptional<DragonStateHandler> cap = DragonStateProvider.getCap(entity);

		return cap.orElse(new DragonStateHandler());
	}

	public static EntityStateHandler getEntityHandler(Entity entity){
		if (entity == null) {
			return new EntityStateHandler();
		}

		LazyOptional<EntityStateHandler> cap = (LazyOptional<EntityStateHandler>) DragonStateProvider.getEntityCap(entity);

		return cap.orElse(new EntityStateHandler());
	}

	public static boolean isDragon(Entity entity){
		if (!(entity instanceof Player)) {
			return false;
		}

		return DragonStateProvider.getCap(entity).filter(DragonStateHandler::isDragon).isPresent();
	}

	public static AbstractDragonType getDragonType(Entity entity){
		return getHandler(entity).getType();
	}
	
	public static AbstractDragonType getDragonType(DragonStateHandler handler) {
		return handler.getType();
	}

	public static AbstractDragonBody getDragonBody(Entity entity) {
		return getHandler(entity).getBody();
	}

	public static AbstractDragonBody getDragonBody(DragonStateHandler handler) {
		return handler.getBody();
	}

	public static boolean isDragonSubtype(final Entity entity, final AbstractDragonType type) {
		if (!(entity instanceof Player))
			return false;
		return Objects.equal(DragonUtils.getHandler(entity).getSubtypeName(), type.getSubtypeName());
	}

	public static boolean isDragonType(final Entity entity, final AbstractDragonType typeToCheck) {
		if (!(entity instanceof Player)) {
			return false;
		}

		return isDragonType(getHandler(entity), typeToCheck);
	}
	
	public static boolean isDragonType(final DragonStateHandler playerHandler, final AbstractDragonType typeToCheck) {
		if (playerHandler == null || typeToCheck == null || playerHandler.getType() == null) {
			return false;
		}

		return Objects.equal(playerHandler.getType().getTypeName(), typeToCheck.getTypeName());
	}

	public static boolean isDragonType(final AbstractDragonType playerType, final AbstractDragonType typeToCheck) {
		if (playerType == null || typeToCheck == null) {
			return false;
		}

		return Objects.equal(playerType.getTypeName(), typeToCheck.getTypeName());
	}

	public static DragonLevel getDragonLevel(Entity entity){
		return getHandler(entity).getLevel();
	}

	/** Converts the supplied {@link Tier#getLevel} to vanilla values (0 to 4) */
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