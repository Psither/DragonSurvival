package by.dragonsurvivalteam.dragonsurvival.magic.common.innate;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public abstract class DragonWingAbility extends InnateDragonAbility {
    @Translation(type = Translation.Type.MISC, comments = "■ You are able to §2spin§r in air and in lava.")
    private static final String LAVA_SPIN = Translation.Type.ABILITY_DESCRIPTION.wrap("lava_spin");

    @Translation(type = Translation.Type.MISC, comments = "■ You have not yet unlocked the ability to §cspin§r in air and in lava.")
    private static final String NO_LAVA_SPIN = Translation.Type.ABILITY_DESCRIPTION.wrap("no_lava_spin");

    @Translation(type = Translation.Type.MISC, comments = "■ You are able to §2spin§r while flying or swimming.")
    private static final String WATER_SPIN = Translation.Type.ABILITY_DESCRIPTION.wrap("water_spin");

    @Translation(type = Translation.Type.MISC, comments = "■ You have not yet unlocked the ability to §cspin§r while flying or swimming.")
    private static final String NO_WATER_SPIN = Translation.Type.ABILITY_DESCRIPTION.wrap("no_water_spin");

    @Override
    public Component getDescription() {
        String key = Keybind.TOGGLE_FLIGHT.getKey().getDisplayName().getString().toUpperCase(Locale.ROOT);

        if (key.isEmpty()) {
            key = Keybind.TOGGLE_FLIGHT.getKey().getDisplayName().getString();
        }

        DragonStateHandler data = DragonStateProvider.getData(player);
        String spin;

        if (DragonUtils.isType(data, DragonTypes.CAVE)) {
            spin = data.getMovementData().spinLearned ? LAVA_SPIN : NO_LAVA_SPIN;
        } else {
            spin = data.getMovementData().spinLearned ? WATER_SPIN : NO_WATER_SPIN;
        }

        return Component.translatable(Translation.Type.ABILITY_DESCRIPTION.wrap(getName()), key).append("\n").append(Component.translatable(spin));
    }

    @Override
    public int getLevel() {
        return DragonStateProvider.getOptional(getPlayer()).map(DragonStateHandler::hasFlight).orElse(false) ? 1 : 0;
    }
}