package by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.innate;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.innate.DragonClawsAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.resources.ResourceLocation;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
        "■ Forest dragons §2can§r deal increased damage, and may chop down trees without tools. They grow stronger with age.\n",
        "■ §cCannot§r ride horses and use several items.",
})
@Translation(type = Translation.Type.ABILITY, comments = "Claws and Teeth")
@RegisterDragonAbility
public class ForestClawAbility extends DragonClawsAbility {
    @Override
    public String getName() {
        return "forest_claws_and_teeth";
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.FOREST;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_claws_and_teeth_0.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_claws_and_teeth_1.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_claws_and_teeth_2.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_claws_and_teeth_3.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_claws_and_teeth_4.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_claws_and_teeth_5.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_claws_and_teeth_6.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/forest_claws_and_teeth_7.png")
        };
    }
}