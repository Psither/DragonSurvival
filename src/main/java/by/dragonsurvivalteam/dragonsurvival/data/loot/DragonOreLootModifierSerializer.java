package by.dragonsurvivalteam.dragonsurvival.data.loot;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

// We aren't going to serialize anything here, so this is essentially just a formality.
public class DragonOreLootModifierSerializer extends GlobalLootModifierProvider {

    public DragonOreLootModifierSerializer(PackOutput output, String modid) {
        super(output, modid);
    }

    @Override
    protected void start() {
        add("dragon_ore", new DragonOreLootModifier(new LootItemCondition[0]));
    }
}
