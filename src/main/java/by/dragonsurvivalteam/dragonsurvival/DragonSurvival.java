package by.dragonsurvivalteam.dragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.*;
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

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EntityStateHandler>> ENTITY_HANDLER = DS_ATTACHMENT_TYPES.register("entity_handler", () -> AttachmentType.serializable(EntityStateHandler::new).build());
    // TODO :: does this need a custom copy handle for entering the end portal?
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DragonStateHandler>> DRAGON_HANDLER = DS_ATTACHMENT_TYPES.register("dragon_handler", () -> AttachmentType.serializable(DragonStateHandler::new).copyOnDeath().build());

    public DragonSurvival(IEventBus bus, ModContainer container) {
        PROXY = FMLLoader.getDist().isClient() ? new ClientProxy() : new ServerProxy();

        DragonTypes.registerTypes();

        ConfigHandler.initConfig();
        DragonAbilities.initAbilities();

        bus.addListener(this::addPackFinders);

        DS_ATTACHMENT_TYPES.register(bus);
        DSAttributes.DS_ATTRIBUTES.register(bus);
        DSEquipment.DS_ARMOR_MATERIALS.register(bus);
        // We need to register blocks before items, since otherwise the items will register before the item-blocks can be assigned
        DSBlocks.DS_BLOCKS.register(bus);
        DSItems.DS_ITEMS.register(bus);
        DSEffects.DS_MOB_EFFECTS.register(bus);
        DSContainers.DS_CONTAINERS.register(bus);
        DSCreativeTabs.DS_CREATIVE_MODE_TABS.register(bus);
        DSParticles.DS_PARTICLES.register(bus);
        DSSounds.DS_SOUNDS.register(bus);
        DSPotions.DS_POTIONS.register(bus);
        DSTileEntities.DS_TILE_ENTITIES.register(bus);
        DSEntities.DS_ENTITY_TYPES.register(bus);
        DSMapDecorationTypes.DS_MAP_DECORATIONS.register(bus);
        DSTrades.DS_POI_TYPES.register(bus);
        DSTrades.DS_VILLAGER_PROFESSIONS.register(bus);
        DSStructurePlacementTypes.DS_STRUCTURE_PLACEMENT_TYPES.register(bus);
        DSAdvancementTriggers.DS_TRIGGERS.register(bus);
        DSCommands.ARGUMENT_TYPES.register(bus);
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