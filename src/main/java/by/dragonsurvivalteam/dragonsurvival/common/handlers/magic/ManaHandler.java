package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.CaveDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.ForestDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.SeaDragonType;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.CaveDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.ForestDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.SeaDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.types.BlockStateConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.MagicAbility;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicStats;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EventBusSubscriber
public class ManaHandler {
    /**
     * Ratio for converting experience to mana and vice versa <br> <br>
     * Level  5:   55 experience points <br>
     * Level 10:  160 experience points <br>
     * Level 20:  550 experience points <br>
     * Level 30: 1395 experience points <br>
     */
    private static final int EXPERIENCE_TO_MANA = 10;

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        DragonStateProvider.getOptional(player).ifPresent(cap -> {
            if (cap.getMagicData().getCurrentlyCasting() != null) {
                return;
            }

            boolean goodConditions = ManaHandler.isPlayerInGoodConditions(player);

            int timeToRecover = goodConditions ? ServerConfig.favorableManaTicks : ServerConfig.normalManaTicks;

            if (player.hasEffect(DSEffects.SOURCE_OF_MAGIC)) {
                timeToRecover = 1;
            }

            if (player.tickCount % Functions.secondsToTicks(timeToRecover) == 0) {
                if (cap.getMagicData().getCurrentMana() < getMaxMana(player)) {
                    replenishMana(player, 1);
                }
            }
        });
    }

    public static boolean isPlayerInGoodConditions(@NotNull Player player) {
        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return false;
        }

        if (player.hasEffect(DSEffects.SOURCE_OF_MAGIC) || data.getType().isInManaCondition(player)) {
            return true;
        }

        BlockState state = player.getBlockStateOn();
        List<BlockStateConfig> conditionalBlocks;
        TagKey<Block> manaBlocks;

        switch (data.getType()) {
            case CaveDragonType ignored -> {
                conditionalBlocks = CaveDragonConfig.caveConditionalManaBlocks;
                manaBlocks = DSBlockTags.REGENERATES_CAVE_DRAGON_MANA;
            }
            case SeaDragonType ignored -> {
                conditionalBlocks = SeaDragonConfig.seaConditionalManaBlocks;
                manaBlocks = DSBlockTags.REGENERATES_SEA_DRAGON_MANA;
            }
            case ForestDragonType ignored -> {
                conditionalBlocks = ForestDragonConfig.forestConditionalManaBlocks;
                manaBlocks = DSBlockTags.REGENERATES_FOREST_DRAGON_MANA;
            }
            default -> throw new IllegalStateException("Invalid dragon type: [" + data.getType().getClass().getName() + "]");
        }

        if (state.getBlock() instanceof TreasureBlock || state.is(manaBlocks)) {
            return true;
        }

        for (BlockStateConfig config : conditionalBlocks) {
            if (config.test(state)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasEnoughMana(final Player player, int manaCost) {
        return player.hasEffect(DSEffects.SOURCE_OF_MAGIC) || getCurrentMana(player) - manaCost >= 0;
    }

    public static int getMaxMana(final Player player) {
        int mana = (int) player.getAttributeValue(DSAttributes.MANA);

        if (ServerConfig.consumeExperienceAsMana) {
            mana += getManaFromExperience(player);
        }

        DragonStateHandler data = DragonStateProvider.getData(player);
        mana += DragonAbilities.getAbility(player, MagicAbility.class, data.getType()).map(MagicAbility::getMana).orElse(0);

        return Math.max(0, mana);
    }

    public static void replenishMana(Player player, int mana) {
        if (player.level().isClientSide()) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (getCurrentMana(player) == getMaxMana(player)) {
            return;
        }

        data.getMagicData().setCurrentMana(data.getMagicData().getCurrentMana() + mana);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncMagicStats.Data(player.getId(), data.getMagicData().getSelectedAbilitySlot(), data.getMagicData().getCurrentMana(), data.getMagicData().shouldRenderAbilities()));
    }

    public static void consumeMana(Player player, int manaCost) {
        if (player == null || player.isCreative() || player.hasEffect(DSEffects.SOURCE_OF_MAGIC) || !hasEnoughMana(player, manaCost)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);
        int pureMana = data.getMagicData().getCurrentMana();

        if (ServerConfig.consumeExperienceAsMana && player.level().isClientSide()) {
            // Check if experience would be consumed as part of the mana cost
            if (pureMana < manaCost && getCurrentMana(player) >= manaCost) {
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.01F, 0.01F);
            }
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (ServerConfig.consumeExperienceAsMana) {
            if (pureMana < manaCost) {
                int missingMana = pureMana - manaCost;
                player.giveExperiencePoints(convertMana(missingMana));
                data.getMagicData().setCurrentMana(0);
            } else {
                data.getMagicData().setCurrentMana(pureMana - manaCost);
            }
        } else {
            data.getMagicData().setCurrentMana(pureMana - manaCost);
        }

        PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncMagicStats.Data(player.getId(), data.getMagicData().getSelectedAbilitySlot(), data.getMagicData().getCurrentMana(), data.getMagicData().shouldRenderAbilities()));
    }

    public static int getCurrentMana(Player player) {
        DragonStateHandler data = DragonStateProvider.getData(player);
        int currentMana = data.getMagicData().getCurrentMana();

        if (ServerConfig.consumeExperienceAsMana) {
            currentMana += getManaFromExperience(player);
        }

        return Math.min(currentMana, getMaxMana(player));
    }

    public static int getManaFromExperience(final Player player) {
        return convertExperience(ExperienceUtils.getTotalExperience(player));
    }

    /** Convert experience points to mana based on the {@link ManaHandler#EXPERIENCE_TO_MANA} ratio */
    private static int convertExperience(int experience) {
        return experience / EXPERIENCE_TO_MANA;
    }

    /** Convert mana to experience points based on the {@link ManaHandler#EXPERIENCE_TO_MANA} ratio */
    private static int convertMana(int mana) {
        return mana * EXPERIENCE_TO_MANA;
    }
}