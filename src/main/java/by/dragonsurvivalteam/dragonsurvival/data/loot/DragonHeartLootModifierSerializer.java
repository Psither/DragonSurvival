package by.dragonsurvivalteam.dragonsurvival.data.loot;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class DragonHeartLootModifierSerializer extends GlobalLootModifierProvider {

        public DragonHeartLootModifierSerializer(PackOutput output, String modid) {
            super(output, modid);
        }

        @Override
        protected void start() {
            add("dragon_heart", new DragonHeartLootModifier(new LootItemCondition[0]));
        }
}
