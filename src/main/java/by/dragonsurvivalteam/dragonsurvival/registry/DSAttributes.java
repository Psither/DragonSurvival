package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DSAttributes {
    public static final DeferredRegister<Attribute> DS_ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, DragonSurvival.MODID);

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Dragon Flight Stamina")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Reduces the food exhaustion of flying")
    public static final Holder<Attribute> FLIGHT_STAMINA_COST = DS_ATTRIBUTES.register("flight_stamina_cost", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("flight_stamina"), 1, 0, 5).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Lava Swim Speed")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the lava swim speed") // TODO :: enable 'can swim in fluid' for lava when this value is above 0 (or some other threshold)?
    public static final Holder<Attribute> LAVA_SWIM_SPEED = DS_ATTRIBUTES.register("lava_swim_speed", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("lava_swim_speed"), 1, 0, 1024).setSyncable(true));

    // TODO :: use Attributes#FLYING_SPEED instead? Currently it seems to be only used for mobs
    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Dragon Flight Speed")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the dragon flight speed")
    public static final Holder<Attribute> FLIGHT_SPEED = DS_ATTRIBUTES.register("flight_speed", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("flight_speed"), 1, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Dragon Mana")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Amount of mana for dragon abilities")
    public static final Holder<Attribute> MANA = DS_ATTRIBUTES.register("mana", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("mana"), 1, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Experience")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the dropped experience")
    public static final Holder<Attribute> EXPERIENCE = DS_ATTRIBUTES.register("experience", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("experience"), 1, 0, 1024).setSyncable(true));

    @SubscribeEvent
    public static void attachAttributes(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, FLIGHT_STAMINA_COST);
        event.add(EntityType.PLAYER, FLIGHT_SPEED);
        event.add(EntityType.PLAYER, MANA);
        event.add(EntityType.PLAYER, EXPERIENCE);
        event.getTypes().forEach(type -> event.add(type, LAVA_SWIM_SPEED));
    }
}
