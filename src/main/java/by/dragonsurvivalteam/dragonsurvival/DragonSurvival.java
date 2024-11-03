package by.dragonsurvivalteam.dragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.AddTableLootExtendedLootModifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.DragonHeartLootModifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.DragonOreLootModifier;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.Proxy;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.ServerProxy;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers.DS_TRIGGERS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes.DS_ATTRIBUTES;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks.DS_BLOCKS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSContainers.DS_CONTAINERS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSCreativeTabs.DS_CREATIVE_MODE_TABS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSEffects.DS_MOB_EFFECTS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSEntities.DS_ENTITY_TYPES;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSEquipment.DS_ARMOR_MATERIALS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSItems.DS_ITEMS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSMapDecorationTypes.DS_MAP_DECORATIONS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSParticles.DS_PARTICLES;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSPotions.DS_POTIONS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSSounds.DS_SOUNDS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSStructurePlacementTypes.DS_STRUCTURE_PLACEMENT_TYPES;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities.DS_TILE_ENTITIES;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSTrades.DS_POI_TYPES;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSTrades.DS_VILLAGER_PROFESSIONS;

@Mod(DragonSurvival.MODID)
public class DragonSurvival {
    public static final String MODID = "dragonsurvival";
    public static final Logger LOGGER = LogManager.getLogger("Dragon Survival");
    public static Proxy PROXY;

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    private static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<DragonOreLootModifier>> DRAGON_ORE = DragonSurvival.GLM.register("dragon_ore", DragonOreLootModifier.CODEC);
    private static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<DragonHeartLootModifier>> DRAGON_HEART = DragonSurvival.GLM.register("dragon_heart", DragonHeartLootModifier.CODEC);
    private static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddTableLootExtendedLootModifier>> ADD_TABLE_LOOT_EXTENDED = DragonSurvival.GLM.register("add_table_loot_extended", () -> AddTableLootExtendedLootModifier.CODEC);

    public static final DeferredRegister<AttachmentType<?>> DS_ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    public static final Supplier<AttachmentType<EntityStateHandler>> ENTITY_HANDLER = DS_ATTACHMENT_TYPES.register(
            "entity_handler",
            () -> AttachmentType.serializable(EntityStateHandler::new).build()
    );

    // TODO :: does this need a custom copy handle for entering the end portal?
    public static final Supplier<AttachmentType<DragonStateHandler>> DRAGON_HANDLER = DS_ATTACHMENT_TYPES.register(
            "dragon_handler",
            () -> AttachmentType.serializable(DragonStateHandler::new).copyOnDeath().build()
    );

    public DragonSurvival(IEventBus bus, ModContainer container) {
        PROXY = FMLLoader.getDist().isClient() ? new ClientProxy() : new ServerProxy();

        DragonTypes.registerTypes();
        DragonBodies.registerBodies();

        ConfigHandler.initConfig();
        DragonAbilities.initAbilities();

        bus.addListener(this::addPackFinders);

        // We need to register blocks before items, since otherwise the items will register before the item-blocks can be assigned
        DS_ATTRIBUTES.register(bus);
        DS_ARMOR_MATERIALS.register(bus);
        DS_BLOCKS.register(bus);
        DS_ITEMS.register(bus);
        DS_ATTACHMENT_TYPES.register(bus);
        DS_MOB_EFFECTS.register(bus);
        DS_CONTAINERS.register(bus);
        DS_CREATIVE_MODE_TABS.register(bus);
        DS_PARTICLES.register(bus);
        DS_SOUNDS.register(bus);
        DS_POTIONS.register(bus);
        DS_TILE_ENTITIES.register(bus);
        DS_ENTITY_TYPES.register(bus);
        DS_MAP_DECORATIONS.register(bus);
        DS_POI_TYPES.register(bus);
        DS_VILLAGER_PROFESSIONS.register(bus);
        DS_STRUCTURE_PLACEMENT_TYPES.register(bus);
        DS_TRIGGERS.register(bus);
        GLM.register(bus);
    }

    private void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            HashMap<MutableComponent, String> resourcePacks = new HashMap<>();
            //resourcePacks.put(Component.literal("- Dragon East"), "resourcepacks/ds_east");
            //resourcePacks.put(Component.literal("- Dragon North"), "resourcepacks/ds_north");
            //resourcePacks.put(Component.literal("- Dragon South"), "resourcepacks/ds_south");
            //resourcePacks.put(Component.literal("- Dragon West"), "resourcepacks/ds_west");
            resourcePacks.put(Component.literal("- Old Magic Icons for DS"), "resourcepacks/ds_old_magic");
            resourcePacks.put(Component.literal("- Dark GUI for DS"), "resourcepacks/ds_dark_gui");
            for (Map.Entry<MutableComponent, String> entry : resourcePacks.entrySet()) {
                registerBuiltinResourcePack(event, entry.getKey(), entry.getValue());
            }
        }
    }

    private static void registerBuiltinResourcePack(AddPackFindersEvent event, MutableComponent name, String folder) {
        event.addPackFinders(res(folder), PackType.CLIENT_RESOURCES, name, PackSource.BUILT_IN, false, Pack.Position.TOP);
    }

    // TODO :: move into a utils class?

    /** Creates a {@link ResourceLocation} with the dragon survival namespace */
    public static ResourceLocation res(final String path) {
        return location(DragonSurvival.MODID, path);
    }

    public static ResourceLocation location(final String namespace, final String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}