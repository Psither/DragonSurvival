package by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.innate;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.ForestDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.innate.InnateDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.resources.ResourceLocation;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
        "■ The predatory plants in your body dislike §dDarkness§r. If the light level around you is lower than 4, you may receive the §c«Stress»§r effect, rapidly draining your food gauge.\n",
        "■ The skill «Light the Dark» and effect «Forest Magic» §7could make your life easier.",
})
@Translation(type = Translation.Type.ABILITY, comments = "Fear of Darkness")
@RegisterDragonAbility
public class FearOfDarkAbility extends InnateDragonAbility {
    @Override
    public String getName() {
        return "fear_of_dark";
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.FOREST;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/fear_of_dark_0.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/fear_of_dark_1.png")
        };
    }

    @Override
    public int getLevel() {
        return ServerConfig.penaltiesEnabled && ForestDragonConfig.stressTicks != 0.0 ? 1 : 0;
    }

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !ServerConfig.penaltiesEnabled || ForestDragonConfig.stressTicks == 0.0;
    }

    @Override
    public int getSortOrder() {
        return 4;
    }
}