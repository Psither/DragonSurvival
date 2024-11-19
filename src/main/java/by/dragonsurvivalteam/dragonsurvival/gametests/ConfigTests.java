package by.dragonsurvivalteam.dragonsurvival.gametests;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonPenaltyHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigUtils;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.config.types.FoodConfigCollector;
import by.dragonsurvivalteam.dragonsurvival.config.types.ItemHurtConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static net.neoforged.neoforge.event.EventHooks.onItemUseFinish;

@PrefixGameTestTemplate(false)
@GameTestHolder(DragonSurvival.MODID)
public class ConfigTests {
    @BeforeBatch(batch = "config_tests")
    public static void beforeTest(final ServerLevel level) {
        ConfigHandler.resetConfigValues(ConfigSide.SERVER);
    }

    @GameTest(template = TestUtils.AIR_CUBE_1X, batch = "config_tests")
    public static void teat_food_config(final GameTestHelper helper) {
        String configKey = "cave_foods";

        // Map as initial setup so the data can be easier accessed
        Map<Item, Pair<Integer, Float>> foodMap = Map.of(
                Items.RAIL, Pair.of(5, 3f),
                Items.POWERED_RAIL, Pair.of(2, 1f)
        );

        List<FoodConfigCollector> foods = foodMap.keySet().stream()
                .map(item -> FoodConfigCollector.of(ConfigUtils.location(item), foodMap.get(item).first(), foodMap.get(item).second()))
                .toList();

        AbstractDragonType dragonType = DragonTypes.CAVE;

        // Check that the items we set as edible are correctly set
        TestUtils.setAndCheckConfig(helper, configKey, foods);
        List<Item> edibleFoods = DragonFoodHandler.getEdibleFoods(dragonType);

        helper.assertTrue(
                edibleFoods.size() == foods.size(),
                String.format("[%s] dragon food list has [" + edibleFoods.size() + "] entries - expected [" + foods.size() + "]", dragonType)
        );
        //noinspection SlowListContainsAll -> ignore performance
        helper.assertTrue(
                edibleFoods.containsAll(foodMap.keySet()),
                String.format("[%s] dragon food list items were [" + edibleFoods + "] - expected [" + foodMap.keySet() + "]", dragonType)
        );

        // Check that the specified food properties are correctly set
        for (Item item : foodMap.keySet()) {
            FoodProperties properties = DragonFoodHandler.getDragonFoodProperties(item, dragonType);

            //noinspection DataFlowIssue -> ignore potential null value
            helper.assertTrue(
                    properties.nutrition() == foodMap.get(item).first(),
                    String.format("[%s] has a nutrition value of [%d] - expected [%d]", item, properties.nutrition(), foodMap.get(item).first())
            );
            helper.assertTrue(
                    properties.saturation() == foodMap.get(item).second(),
                    String.format("[%s] has a nutrition value of [%f] - expected [%f]", item, properties.saturation(), foodMap.get(item).second())
            );
        }

        // Check that pufferfish does not retain the (non-beneficial) confusion effect
        // Its default food properties should also be kept since no custom properties were specified
        foods = List.of(FoodConfigCollector.of(ConfigUtils.location(Items.PUFFERFISH)));
        TestUtils.setAndCheckConfig(helper, configKey, foods);

        FoodProperties properties = DragonFoodHandler.getDragonFoodProperties(Items.PUFFERFISH, dragonType);
        FoodProperties pufferfishProperties = Items.PUFFERFISH.components().get(DataComponents.FOOD);

        //noinspection DataFlowIssue -> ignore potential null value
        helper.assertTrue(
                properties.nutrition() == pufferfishProperties.nutrition(),
                String.format("[%s] has a nutrition value of [%d] - expected [%d]", Items.PUFFERFISH, properties.nutrition(), pufferfishProperties.nutrition())
        );
        helper.assertTrue(
                properties.saturation() == pufferfishProperties.saturation(),
                String.format("[%s] has a nutrition value of [%f] - expected [%f]", Items.PUFFERFISH, properties.saturation(), pufferfishProperties.saturation())
        );
        helper.assertTrue(
                properties.effects().stream().noneMatch(possibleEffect -> possibleEffect.effect().getEffect().is(MobEffects.CONFUSION)),
                String.format("[%s] effects are [%s] - expected [%s] to not be present", Items.PUFFERFISH, properties.effects(), MobEffects.CONFUSION.getRegisteredName())
        );

        helper.succeed();
    }

    @GameTest(template = TestUtils.AIR_CUBE_1X, batch = "config_tests")
    public static void test_hurt_config(final GameTestHelper helper) {
        Player player = TestUtils.createPlayer(helper, GameType.DEFAULT_MODE);
        TestUtils.setToDragon(helper, player, DragonTypes.CAVE, DragonBody.center, DragonLevel.YOUNG.size);

        int damage = 2;
        List<ItemHurtConfig> configs = List.of(ItemHurtConfig.of(ConfigUtils.location(Items.POTION), damage));
        TestUtils.setAndCheckConfig(helper, "cave_hurtful_items", configs);

        float expectedHealth = player.getHealth() - damage;
        onItemUseFinish(player, Items.POTION.getDefaultInstance(), 20, Items.GLASS_BOTTLE.getDefaultInstance());
        float currentHealth = player.getHealth();
        helper.assertTrue(currentHealth == expectedHealth, String.format("Health is [%f] - expected [%f]", currentHealth, expectedHealth));

        helper.succeed();
    }

    @GameTest(template = TestUtils.AIR_CUBE_1X, batch = "config_tests")
    public static void test_blacklist_config(final GameTestHelper helper) {
        String configKey = "blacklisted_items";

        List<String> value = List.of("minecraft:potion", "minecraft:swords", "minecraft:.*bow");
        TestUtils.setAndCheckConfig(helper, configKey, value);

        List<Item> itemsToCheck = List.of(
                Items.POTION,
                Items.BOW,
                Items.CROSSBOW,
                Items.IRON_SWORD,
                Items.NETHERITE_SWORD
        );

        for (Item item : itemsToCheck) {
            helper.assertTrue(DragonPenaltyHandler.itemIsBlacklisted(item), "[%s] is not in the blacklist - expected item to be blacklisted");
        }

        helper.succeed();
    }

    @GameTest(template = TestUtils.AIR_CUBE_1X, batch = "config_tests")
    public static void test_invalid_config_list(final GameTestHelper helper) {
        String configKey = "blacklisted_items";
        List<String> defaultValue = List.of("minecraft:potion");

        List<List<String>> invalidValues = List.of(
                List.of("minecraft:Bow"),
                List.of("minecraft_bow"),
                List.of("minecraft:#bow")
        );

        invalidValues.forEach(invalidValue -> {
            TestUtils.setAndCheckConfig(helper, configKey, defaultValue);
            ConfigHandler.updateConfigValue(configKey, invalidValue);
            Field field = ConfigHandler.getField(configKey);

            try {
                Object fieldValue = field.get(null);
                helper.assertTrue(fieldValue.equals(defaultValue), "The field value [" + fieldValue + "] did not match the expected value [" + defaultValue + "] (config should not have been updated)");
            } catch (IllegalAccessException exception) {
                helper.fail("Failed trying to access a field for the config [" + configKey + "] to validate the config: [" + exception.getMessage() + "]");
            }
        });

        helper.succeed();
    }

    @GameTest(template = TestUtils.AIR_CUBE_1X, batch = "config_tests")
    public static void test_invalid_config(final GameTestHelper helper) {
        String configKey = "break_speed_multiplier";
        float valueNotInRange = 0f;
        float defaultValue = 2f;

        TestUtils.setAndCheckConfig(helper, configKey, defaultValue);
        ConfigHandler.updateConfigValue(configKey, valueNotInRange);
        Field field = ConfigHandler.getField(configKey);

        try {
            Object fieldValue = field.get(null);
            helper.assertTrue(fieldValue.equals(defaultValue), "The field value [" + fieldValue + "] did not match the expected value [" + defaultValue + "] (config should not have been updated)");
        } catch (IllegalAccessException exception) {
            helper.fail("Failed trying to access a field for the config [" + configKey + "] to validate the config: [" + exception.getMessage() + "]");
        }

        helper.succeed();
    }
}
