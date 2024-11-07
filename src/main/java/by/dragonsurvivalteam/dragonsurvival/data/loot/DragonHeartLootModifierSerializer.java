package by.dragonsurvivalteam.dragonsurvival.data.loot;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class DragonHeartLootModifierSerializer extends GlobalLootModifierProvider {


    public DragonHeartLootModifierSerializer(DataGenerator gen, String modid) {
        super(gen, modid);
    }

    @Override
        protected void start() {
            add("dragon_heart", new DragonHeartLootModifier(new LootItemCondition[0]));
        }
}
