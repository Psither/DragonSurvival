package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.ClawInventory;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.DragonBonusConfig;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncBrokenTool;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

@EventBusSubscriber
public class ClawToolHandler {
    @SubscribeEvent
    public static void experiencePickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();

        DragonStateProvider.getOptional(player).ifPresent(cap -> {
            ArrayList<ItemStack> stacks = new ArrayList<>();

            for (int i = 0; i < ClawInventory.Slot.size(); i++) {
                ItemStack clawStack = cap.getClawToolData().getClawsInventory().getItem(i);
                Holder<Enchantment> mending = event.getEntity().level().registryAccess().registry(Registries.ENCHANTMENT).get().getHolderOrThrow(Enchantments.MENDING);
                int mendingLevel = clawStack.getEnchantmentLevel(mending);

                if (mendingLevel > 0 && clawStack.isDamaged()) {
                    stacks.add(clawStack);
                }
            }

            if (!stacks.isEmpty()) {
                ItemStack repairTime = stacks.get(player.getRandom().nextInt(stacks.size()));
                if (!repairTime.isEmpty() && repairTime.isDamaged()) {

                    int i = Math.min((int) (event.getOrb().value * repairTime.getXpRepairRatio()), repairTime.getDamageValue());
                    event.getOrb().value -= i * 2;
                    repairTime.setDamageValue(repairTime.getDamageValue() - i);
                }
            }

            event.getOrb().value = Math.max(0, event.getOrb().value);
            player.detectEquipmentUpdates();
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) // In order to add the drops early for other mods (e.g. grave mods)
    public static void playerDieEvent(LivingDropsEvent event) {
        if (!ServerConfig.retainClawItems && event.getEntity() instanceof Player player && !player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            for (int i = 0; i < ClawInventory.Slot.size(); i++) {
                ItemStack stack = handler.getClawToolData().getClawsInventory().getItem(i);

                if (!stack.isEmpty()) {
                    if (!EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                        event.getDrops().add(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack));
                    }

                    handler.getClawToolData().getClawsInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void dropBlocksMinedByPaw(PlayerEvent.HarvestCheck harvestCheck) {
        if (!DragonBonusConfig.bonusesEnabled || !DragonBonusConfig.clawsAreTools) {
            return;
        }

        Player player = harvestCheck.getEntity();
        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        BlockState state = harvestCheck.getTargetBlock();

        if (!harvestCheck.canHarvest() && ToolUtils.shouldUseDragonTools(stack)) {
            harvestCheck.setCanHarvest(data.canHarvestWithPaw(state));
        }
    }

    public static ItemStack getDragonHarvestTool(final Player player, final BlockState state) {
        ItemStack mainStack = player.getInventory().getSelected();
        float newSpeed = 0F;

        if (!ToolUtils.shouldUseDragonTools(mainStack)) {
            return mainStack;
        }

        ItemStack harvestTool = mainStack;
        DragonStateHandler handler = DragonStateProvider.getData(player);

        for (int i = 1; i < ClawInventory.Slot.size(); i++) {
            ItemStack breakingItem = handler.getClawToolData().getClawsInventory().getItem(i);

            if (!breakingItem.isEmpty() && breakingItem.isCorrectToolForDrops(state)) {
                float tempSpeed = breakingItem.getDestroySpeed(state);

                if (breakingItem.getItem() instanceof DiggerItem item) {
                    tempSpeed = item.getDestroySpeed(breakingItem, state);
                }

                if (tempSpeed > newSpeed) {
                    newSpeed = tempSpeed;
                    harvestTool = breakingItem;
                }
            }
        }

        return harvestTool;
    }

    public static Pair<ItemStack, Integer> getDragonHarvestToolAndSlot(final Player player, final BlockState state) {
        ItemStack mainStack = player.getInventory().getSelected();
        float newSpeed = 0F;

        if (!ToolUtils.shouldUseDragonTools(mainStack)) {
            return Pair.of(mainStack, -1);
        }

        ItemStack harvestTool = mainStack;
        DragonStateHandler handler = DragonStateProvider.getData(player);
        int toolSlot = -1;

        for (int i = 0; i < ClawInventory.Slot.size(); i++) {
            ItemStack breakingItem = handler.getClawToolData().getClawsInventory().getItem(i);

            if (!breakingItem.isEmpty() && breakingItem.isCorrectToolForDrops(state)) {
                float tempSpeed = breakingItem.getDestroySpeed(state);

                if (breakingItem.getItem() instanceof DiggerItem item) {
                    tempSpeed = item.getDestroySpeed(breakingItem, state);
                }

                if (tempSpeed > newSpeed) {
                    newSpeed = tempSpeed;
                    harvestTool = breakingItem;
                    toolSlot = i;
                }
            }
        }

        return Pair.of(harvestTool, toolSlot);
    }

    public static ItemStack getDragonHarvestTool(final Player player) {
        ItemStack mainStack = player.getInventory().getSelected();

        if (!ToolUtils.shouldUseDragonTools(mainStack)) {
            return mainStack;
        }

        Level world = player.level();
        BlockHitResult result = Item.getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);

        if (result.getType() != HitResult.Type.MISS) {
            BlockState state = world.getBlockState(result.getBlockPos());
            return getDragonHarvestTool(player, state);
        }

        return mainStack;
    }

    /**
     * @return Only the sword in the dragon tool slot <br>
     * Returns {@link ItemStack#EMPTY} if the player is holding any sort of tool
     */
    public static ItemStack getDragonSword(final LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return ItemStack.EMPTY;
        }

        ItemStack itemInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);

        if (!ToolUtils.shouldUseDragonTools(itemInHand)) {
            return ItemStack.EMPTY;
        }

        return DragonStateProvider.getData(player).getClawToolData().getSword();
    }

    /** Handle tool breaking for the dragon */
    @SubscribeEvent
    public static void onToolBreak(final PlayerDestroyItemEvent event) {
        if (event.getHand() == null) return;
        Player player = event.getEntity();

        if (DragonStateProvider.isDragon(player)) {
            ItemStack clawTool = getDragonHarvestTool(player);

            if (ItemStack.matches(clawTool, event.getOriginal())) {
                clawTool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
            } else {
                if (!player.level().isClientSide()) {
                    DragonStateHandler handler = DragonStateProvider.getData(player);

                    if (handler.switchedTool || handler.switchedWeapon) {
                        player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1, 1);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncBrokenTool.Data(player.getId(), handler.switchedTool ? handler.switchedToolSlot : ClawInventory.Slot.SWORD.ordinal()));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void modifyBreakSpeed(final PlayerEvent.BreakSpeed event) {
        if (!DragonBonusConfig.bonusesEnabled || !DragonBonusConfig.clawsAreTools) {
            return;
        }

        Player player = event.getEntity();
        ItemStack mainStack = player.getMainHandItem();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.switchedTool && !ToolUtils.shouldUseDragonTools(mainStack)) {
            // Bonus does not apply to held tools
            return;
        }

        if (!handler.isDragon()) {
            return;
        }

        BlockState state = event.getState();

        float bonus = 0;
        float unlockedBonus = 0;

        if (handler.getLevel() == DragonLevel.NEWBORN && DragonBonusConfig.bonusUnlockedAt == DragonLevel.NEWBORN) {
            unlockedBonus = DragonBonusConfig.bonusBreakSpeed;
        } else if (handler.getLevel() == DragonLevel.YOUNG && DragonBonusConfig.bonusUnlockedAt != DragonLevel.ADULT) {
            unlockedBonus = DragonBonusConfig.bonusBreakSpeed;
        } else if (handler.getLevel() == DragonLevel.ADULT) {
            unlockedBonus = DragonBonusConfig.bonusBreakSpeedAdult;
            bonus = DragonBonusConfig.baseBreakSpeedAdult;
        }

        if (unlockedBonus > bonus && state.is(handler.getType().harvestableBlocks())) {
            bonus = unlockedBonus;
        }

        for (int i = 0; i < ClawInventory.Slot.size(); i++) {
            ItemStack clawTool = handler.getClawToolData().getClawsInventory().getItem(i);

            if (state.requiresCorrectToolForDrops() && clawTool.isCorrectToolForDrops(state) || clawTool.getDestroySpeed(state) > 1) {
                bonus /= DragonBonusConfig.bonusBreakSpeedReduction;
                break;
            }
        }

        event.setNewSpeed(event.getNewSpeed() * Math.max(1, bonus));
    }
}