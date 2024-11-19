package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.ToolTipHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.CaveDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.ForestDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.SeaDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.types.FoodConfigCollector;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;

@EventBusSubscriber
public class DragonFoodHandler {
    @Translation(key = "dragon_food_is_required", type = Translation.Type.CONFIGURATION, comments = "Dragons will need to adhere to their diets if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "food", key = "dragon_food_is_required")
    public static Boolean requireDragonFood = true;

    @ConfigRange(min = 0, max = 10_000)
    @Translation(key = "charged_soup_effect_duration", type = Translation.Type.CONFIGURATION, comments = "Determines the duration of the fire effect from eating charged soup - disabled if set to 0")
    @ConfigOption(side = ConfigSide.SERVER, category = {"food"}, key = "charged_soup_effect_duration")
    public static Integer chargedSoupBuffDuration = 300;

    // Tooltip maps
    public static CopyOnWriteArrayList<Item> CAVE_DRAGON_FOOD = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<Item> FOREST_DRAGON_FOOD = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<Item> SEA_DRAGON_FOOD = new CopyOnWriteArrayList<>();

    private static ConcurrentHashMap<String, Map<Item, FoodProperties>> DRAGON_FOODS = new ConcurrentHashMap<>();

    public static void rebuildFoodMap() {
        DragonSurvival.LOGGER.debug("Rebuilding food map...");
        DRAGON_FOODS = buildDragonFoodMap();

        // No need to keep them in-memory (they are rebuilt in 'ConfigHandler#handleConfigChange')
        // Currently this method is only called on the server thread meaning there should be no issues
        CaveDragonConfig.caveDragonFoods.clear();
        SeaDragonConfig.seaDragonFoods.clear();
        ForestDragonConfig.validFood.clear();

        // Clear tooltip maps
        // TODO :: rebuild them here as well?
        CAVE_DRAGON_FOOD.clear();
        FOREST_DRAGON_FOOD.clear();
        SEA_DRAGON_FOOD.clear();
    }

    private static ConcurrentHashMap<String, Map<Item, FoodProperties>> buildDragonFoodMap() {
        ConcurrentHashMap<String, Map<Item, FoodProperties>> foodMap = new ConcurrentHashMap<>();
        merge(foodMap, CaveDragonConfig.caveDragonFoods, DragonTypes.CAVE.getTypeName());
        merge(foodMap, SeaDragonConfig.seaDragonFoods, DragonTypes.SEA.getTypeName());
        merge(foodMap, ForestDragonConfig.validFood, DragonTypes.FOREST.getTypeName());
        return foodMap;
    }

    /**
     * Add the collected entries for the dragon type to the global food map (replacing previously added food properties for items if a better one (higher nutrition / saturation) was found)
     */
    private static void merge(final ConcurrentHashMap<String, Map<Item, FoodProperties>> foodMap, final List<FoodConfigCollector> collectors, final String type) {
        foodMap.put(type, new ConcurrentHashMap<>()); // The logic which comes after this needs at least an empty map per dragon type

        for (FoodConfigCollector collector : collectors) {
            Map<Item, FoodProperties> collectedData = collector.collectFoodData();

            for (Item item : collectedData.keySet()) {
                foodMap.get(type).merge(item, calculate(item, collectedData.get(item)), (oldData, newData) -> {
                    if (newData.nutrition() > oldData.nutrition() || newData.nutrition() == oldData.nutrition() && newData.saturation() > oldData.saturation()) {
                        return newData;
                    }

                    return oldData;
                });
            }
        }
    }

    @SuppressWarnings("deprecation") // registry holder usage is ok
    private static FoodProperties calculate(final Item item, final FoodProperties properties) {
        FoodProperties.Builder builder = new FoodProperties.Builder();
        boolean shouldKeepEffects = item.builtInRegistryHolder().is(DSItemTags.KEEP_EFFECTS);

        // saturation is calculated in 'FoodConstants#saturationByModifier' when the properties are built
        float saturation = (properties.saturation() / properties.nutrition()) / 2;

        if (properties.canAlwaysEat()) {
            builder.alwaysEdible();
        }

        // eat duration in ticks is 16 when fast eating is enabled
        if (properties.eatDurationTicks() <= 16) {
            builder.fast();
        }

        properties.effects().forEach(possibleEffect -> {
            if (shouldKeepEffects || possibleEffect.effect().getEffect().value().isBeneficial()) {
                builder.effect(possibleEffect.effectSupplier(), possibleEffect.probability());
            }
        });

        builder.nutrition(properties.nutrition()).saturationModifier(saturation);
        return builder.build();
    }

    private static FoodProperties getBadFoodProperties() {
        FoodProperties.Builder builder = new FoodProperties.Builder();
        builder.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F);
        builder.effect(() -> new MobEffectInstance(MobEffects.POISON, 600, 0), 0.5F);
        builder.nutrition(1);
        return builder.build();
    }

    public static List<Item> getEdibleFoods(final AbstractDragonType type) {
        if (type == null) {
            return List.of();
        }

        if (DragonUtils.isType(type, DragonTypes.FOREST) && !FOREST_DRAGON_FOOD.isEmpty()) {
            return FOREST_DRAGON_FOOD;
        } else if (DragonUtils.isType(type, DragonTypes.SEA) && !SEA_DRAGON_FOOD.isEmpty()) {
            return SEA_DRAGON_FOOD;
        } else if (DragonUtils.isType(type, DragonTypes.CAVE) && !CAVE_DRAGON_FOOD.isEmpty()) {
            return CAVE_DRAGON_FOOD;
        }

        CopyOnWriteArrayList<Item> foods = new CopyOnWriteArrayList<>();

        for (Item item : DRAGON_FOODS.get(type.getTypeName()).keySet()) {
            final FoodProperties foodProperties = DRAGON_FOODS.get(type.getTypeName()).get(item);
            boolean isSafe = true;

            if (foodProperties != null) {
                for (FoodProperties.PossibleEffect possibleEffect : foodProperties.effects()) {
                    if (ToolTipHandler.hideUnsafeFood && !possibleEffect.effectSupplier().get().getEffect().value().isBeneficial()) {
                        isSafe = false;
                        break;
                    }
                }

                if (isSafe && (foodProperties.nutrition() > 0 || foodProperties.saturation() > 0)) {
                    foods.add(item);
                }
            }
        }

        if (DragonUtils.isType(type, DragonTypes.FOREST)) {
            FOREST_DRAGON_FOOD = foods;
        } else if (DragonUtils.isType(type, DragonTypes.CAVE)) {
            CAVE_DRAGON_FOOD = foods;
        } else if (DragonUtils.isType(type, DragonTypes.SEA)) {
            SEA_DRAGON_FOOD = foods;
        }

        return foods;
    }

    public static @Nullable FoodProperties getDragonFoodProperties(final ItemStack stack, final AbstractDragonType type) {
        FoodProperties properties = DRAGON_FOODS.get(type.getTypeName()).get(stack.getItem());

        if (properties != null) {
            return properties;
        }

        FoodProperties baseProperties = stack.getFoodProperties(null);
        if (baseProperties != null) {
            if (requireDragonFood) {
                return getBadFoodProperties();
            } else {
                return baseProperties;
            }
        }

        return null;
    }

    public static @Nullable FoodProperties getDragonFoodProperties(final Item item, final AbstractDragonType type) {
        FoodProperties properties = DRAGON_FOODS.get(type.getTypeName()).get(item);

        if (properties != null) {
            return properties;
        }

        FoodProperties baseProperties = item.getFoodProperties(new ItemStack(item), null);
        if (baseProperties != null) {
            if (requireDragonFood) {
                return getBadFoodProperties();
            } else {
                return baseProperties;
            }
        }

        return null;
    }

    public static boolean isEdible(final ItemStack stack, final AbstractDragonType type) {
        if (requireDragonFood && type != null && DRAGON_FOODS.get(type.getTypeName()).containsKey(stack.getItem())) {
            return true;
        }

        return stack.getFoodProperties(null) != null;
    }

    public static int getUseDuration(final ItemStack stack, final Player entity) {
        FoodProperties properties = getDragonFoodProperties(stack.getItem(), DragonStateProvider.getData(entity).getType());

        if (properties != null) {
            return properties.eatDurationTicks();
        } else {
            return stack.getUseDuration(entity);
        }
    }

    @SubscribeEvent
    public static void setDragonFoodUseDuration(final LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon() || DragonFoodHandler.isEdible(event.getItem(), data.getType())) {
            return;
        }

        FoodProperties properties = DragonFoodHandler.getDragonFoodProperties(event.getItem().getItem(), data.getType());

        if (properties != null) {
            event.setDuration(properties.eatDurationTicks());
        }
    }
}