package by.dragonsurvivalteam.dragonsurvival.data.loot;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

// We aren't going to serialize anything here, so this is essentially just a formality.
public class DragonOreLootModifierSerializer extends GlobalLootModifierProvider {

    public DragonOreLootModifierSerializer(DataGenerator gen, String modid) {
        super(gen, modid);
    }

    @Override
    protected void start() {
        add("dragon_ore", new DragonOreLootModifier(new LootItemCondition[0]));
    }
}
