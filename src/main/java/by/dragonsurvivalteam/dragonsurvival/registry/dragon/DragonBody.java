package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DSAttributeModifier;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonBody(List<DSAttributeModifier> modifiers, double heightMultiplier, boolean hasExtendedCrouch, boolean canHideWings) {
    public static final ResourceKey<Registry<DragonBody>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_bodies"));

    public static final Codec<DragonBody> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DSAttributeModifier.CODEC.listOf().fieldOf("modifiers").forGetter(DragonBody::modifiers),
            Codec.DOUBLE.optionalFieldOf("height_multiplier", 1.0).forGetter(DragonBody::heightMultiplier),
            Codec.BOOL.optionalFieldOf("has_extended_crouch", false).forGetter(DragonBody::hasExtendedCrouch),
            Codec.BOOL.optionalFieldOf("can_hide_wings", true).forGetter(DragonBody::canHideWings)
    ).apply(instance, instance.stable(DragonBody::new)));

    public static final Codec<Holder<DragonBody>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonBody>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    public static final String ATTRIBUTE_PATH = DragonSurvival.MODID + "/body/";

    private static final RandomSource RANDOM = RandomSource.create();

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Central Type§r",
            "■ Inhabitants of all biomes, and the most common type of dragon. They are the most balanced type of dragon, having no particular strengths or weaknesses.",
            "§7■ You may change your body type at any time, but you will lose your growth progress."
    })
    @Translation(type = Translation.Type.BODY, comments = "Center")
    public static ResourceKey<DragonBody> center = key("center");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Eastern Type§r",
            "■ Adapted to life in caves, they lack large wings, reducing the effectiveness of their levitation magic, but they are still excellent swimmers. They have a larger supply of mana, and natural armor.",
            "§7■ You may change your body type at any time, but you will lose your growth progress."
    })
    @Translation(type = Translation.Type.BODY, comments = "East")
    public static ResourceKey<DragonBody> east = key("east");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Northern Type§r",
            "■ Perfect travelers, conquering water, lava and air. They are slower on the ground and weaker physically, but are magically adept and excel at swimming. Their flat bodies allow them to go places other dragons cannot.",
            "§7■ You may change your body type at any time, but you will lose your growth progress. Each type has their own strengths and weaknesses, but the change is mostly cosmetic."
    })
    @Translation(type = Translation.Type.BODY, comments = "North")
    public static ResourceKey<DragonBody> north = key("north");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Southern Type§r",
            "■ They are adapted to life on the plains, capable of running swiftly, and leaping high into the air. The special structure of their paws gives them many advantages on the ground, and they are physically strong, but they will struggle at flight and swimming.",
            "§7■ You may change your body type at any time, but you will lose your growth progress. Each type has their own strengths and weaknesses, but the change is mostly cosmetic."
    })
    @Translation(type = Translation.Type.BODY, comments = "South")
    public static ResourceKey<DragonBody> south = key("south");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Western Type§r",
            "■ Conquerors of mountain and sky, they are unrivalled in their element, but are rather clumsy on the ground.",
            "§7■ You may change your body type at any time, but you will lose your growth progress."
    })
    @Translation(type = Translation.Type.BODY, comments = "West")
    public static ResourceKey<DragonBody> west = key("west");

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    public static void registerBodies(final BootstrapContext<DragonBody> context) {
        context.register(center, new DragonBody(List.of(
                createModifier(center, DSAttributes.FLIGHT_SPEED, 0.2, AttributeModifier.Operation.ADD_VALUE)
        ), 1, false, false));

        context.register(east, new DragonBody(List.of(
                createModifier(east, Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE),
                createModifier(east, Attributes.ATTACK_DAMAGE, -1, AttributeModifier.Operation.ADD_VALUE),
                createModifier(east, Attributes.ATTACK_KNOCKBACK, -1, AttributeModifier.Operation.ADD_VALUE),
                createModifier(east, Attributes.GRAVITY, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                createModifier(east, Attributes.JUMP_STRENGTH, 0.1, AttributeModifier.Operation.ADD_VALUE),
                createModifier(east, Attributes.MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                createModifier(east, NeoForgeMod.SWIM_SPEED, 1, AttributeModifier.Operation.ADD_VALUE),
                createModifier(east, DSAttributes.MANA, 2, AttributeModifier.Operation.ADD_VALUE),
                createModifier(east, DSAttributes.FLIGHT_SPEED, 0.2, AttributeModifier.Operation.ADD_VALUE),
                createModifier(east, DSAttributes.FLIGHT_STAMINA_COST, -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 1, false, true));

        context.register(north, new DragonBody(List.of(
                createModifier(north, Attributes.ATTACK_DAMAGE, -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                createModifier(north, Attributes.ATTACK_KNOCKBACK, -0.5, AttributeModifier.Operation.ADD_VALUE),
                createModifier(north, Attributes.MOVEMENT_SPEED, -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                createModifier(north, NeoForgeMod.SWIM_SPEED, 0.5, AttributeModifier.Operation.ADD_VALUE),
                createModifier(north, DSAttributes.MANA, 2, AttributeModifier.Operation.ADD_VALUE),
                createModifier(north, DSAttributes.FLIGHT_STAMINA_COST, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 0.55, true, true));

        context.register(south, new DragonBody(List.of(
                createModifier(south, Attributes.ATTACK_DAMAGE, 0.5, AttributeModifier.Operation.ADD_VALUE),
                createModifier(south, Attributes.GRAVITY, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                createModifier(south, Attributes.JUMP_STRENGTH, 0.2, AttributeModifier.Operation.ADD_VALUE),
                createModifier(south, Attributes.MOVEMENT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                createModifier(south, NeoForgeMod.SWIM_SPEED, -0.2, AttributeModifier.Operation.ADD_VALUE),
                createModifier(south, DSAttributes.FLIGHT_SPEED, -0.2, AttributeModifier.Operation.ADD_VALUE),
                createModifier(south, DSAttributes.FLIGHT_STAMINA_COST, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 1, false, true));

        context.register(west, new DragonBody(List.of(
                createModifier(west, Attributes.ATTACK_KNOCKBACK, 0.5, AttributeModifier.Operation.ADD_VALUE),
                createModifier(west, Attributes.MOVEMENT_SPEED, -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                createModifier(west, Attributes.STEP_HEIGHT, 1, AttributeModifier.Operation.ADD_VALUE),
                createModifier(west, NeoForgeMod.SWIM_SPEED, -0.3, AttributeModifier.Operation.ADD_VALUE),
                createModifier(west, DSAttributes.FLIGHT_SPEED, 0.2, AttributeModifier.Operation.ADD_VALUE),
                createModifier(west, DSAttributes.FLIGHT_STAMINA_COST, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 1, false, false));
    }

    public static DSAttributeModifier createModifier(final ResourceKey<DragonBody> body, final Holder<Attribute> attribute, double amount, final AttributeModifier.Operation operation) {
        //noinspection DataFlowIssue -> key is not null
        String id = ATTRIBUTE_PATH + body.location().getPath() + "." + attribute.getKey().location().getPath();
        return new DSAttributeModifier(attribute, new AttributeModifier(DragonSurvival.res(id), amount, operation));
    }

    public static ResourceKey<DragonBody> key(final ResourceLocation location) {
        return ResourceKey.create(REGISTRY, location);
    }

    private static ResourceKey<DragonBody> key(final String path) {
        return key(DragonSurvival.res(path));
    }

    public static Holder<DragonBody> random(@Nullable final HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<DragonBody> registry;

        if (provider == null) {
            registry = CommonHooks.resolveLookup(REGISTRY);
        } else {
            registry = provider.lookupOrThrow(REGISTRY);
        }

        //noinspection DataFlowIssue -> registry expected to be present
        Object[] bodies = registry.listElements().toArray();

        if (bodies.length == 0) {
            throw new IllegalStateException("There are no registered dragon bodies");
        }

        //noinspection unchecked -> cast is okay
        return (Holder<DragonBody>) bodies[RANDOM.nextInt(bodies.length)];
    }
}
