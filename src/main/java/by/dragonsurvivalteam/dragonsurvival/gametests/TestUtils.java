package by.dragonsurvivalteam.dragonsurvival.gametests;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;

public class TestUtils {
    public static final String AIR_CUBE_1X = "test_templates/1x1x1_air";
    public static final String AIR_CUBE_3X = "test_templates/3x3x3_air";

    /** Sets the block at {@link BlockPos#ZERO} and returns the resulting {@link BlockState} */
    public static BlockState setBlock(final GameTestHelper helper, final Block block) {
        return setBlock(helper, block, BlockPos.ZERO);
    }

    public static BlockState setBlock(final GameTestHelper helper, final Block block, final BlockPos position) {
        helper.setBlock(position, block);
        // TODO :: should log the block which is currently set
        helper.assertBlock(position, blockToCheck -> blockToCheck == block, "Block at position [" + position + "] is wrong - expected [" + block + "]");
        return helper.getBlockState(position);
    }

    public static void setAndCheckConfig(final GameTestHelper helper, final String configKey, final Object value) {
        ConfigHandler.updateConfigValue(configKey, value);
        Field field = ConfigHandler.getField(configKey);

        try {
            Object fieldValue = field.get(null);
            boolean condition;

            if (value instanceof Number number && fieldValue instanceof Number fieldNumber) {
                // 1 and 1.0 are otherwise not seen as equal
                condition = Float.compare(number.floatValue(), fieldNumber.floatValue()) == 0;
            } else {
                condition = value.equals(fieldValue);
            }

            helper.assertTrue(condition, "The field value [" + fieldValue + "] did not match the new value [" + value + "] after updating the config");
        } catch (IllegalAccessException exception) {
            helper.fail("Failed trying to access a field for the config [" + configKey + "] to validate the config: [" + exception.getMessage() + "]");
        }
    }

    public static void setToDragon(final GameTestHelper helper, final Player player, final AbstractDragonType dragonType, final ResourceKey<DragonBody> dragonBody, double size) {
        DragonStateHandler data = DragonStateProvider.getData(player);

        data.setType(dragonType, player);
        helper.assertTrue(DragonUtils.isType(data, dragonType), String.format("Dragon type was [%s] - expected [%s]", data.getType(), dragonType));

        Holder.Reference<DragonBody> body = player.registryAccess().lookupOrThrow(DragonBody.REGISTRY).getOrThrow(dragonBody);

        data.setBody(body, player);
        helper.assertTrue(DragonUtils.isBody(data, body), String.format("Dragon type was [%s] - expected [%s]", data.getBody(), dragonBody));

        data.setSize(size, player);
        helper.assertTrue(data.getSize() == size, String.format("Size was [%f] - expected [%f]", data.getSize(), size));

        helper.assertTrue(data.isDragon(), "Player is not a dragon - expected player to be a dragon");
    }

    public static Player createPlayer(final GameTestHelper helper, final GameType type) {
        Player player = helper.makeMockPlayer(type);
        resetPlayer(helper, player);
        return player;
    }

    public static void resetPlayer(final GameTestHelper helper, final Player player) {
        player.setData(DragonSurvival.DRAGON_HANDLER, new DragonStateHandler());
        DragonStateHandler data = DragonStateProvider.getData(player);

        AbstractDragonType dragonType = data.getType();
        helper.assertTrue(dragonType == null, String.format("Dragon type was [%s] - expected [null]", dragonType));

        Holder<DragonBody> dragonBody = data.getBody();
        helper.assertTrue(dragonBody == null, String.format("Dragon body was [%s] - expected [null]", dragonBody));

        double size = data.getSize();
        helper.assertTrue(size == 0, String.format("Size was [%f] - expected [0]", size));
    }
}
