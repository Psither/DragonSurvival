package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.CaveDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.ForestDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.SeaDragonType;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.CaveDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.ForestDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.SeaDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.types.ItemHurtConfig;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.PotionUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

import static by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonConfigHandler.DRAGON_BLACKLISTED_ITEMS;

@EventBusSubscriber
public class DragonPenaltyHandler {
    @SubscribeEvent
    public static void hitByWaterPotion(ProjectileImpactEvent potionEvent) {
        if (!ServerConfig.penaltiesEnabled || CaveDragonConfig.caveSplashDamage == 0.0) {
            return;
        }

        if (potionEvent.getProjectile() instanceof ThrownPotion potion) {
            if (potion.getItem().getItem() != Items.SPLASH_POTION) {
                return;
            }

            Optional<Potion> potionData = PotionUtils.getPotion(potion.getItem());
            // If we have no data here, just default to doing nothing (some mods do strange things with potion items that have no Potion data)
            if (potionData.isEmpty() || !potionData.get().getEffects().isEmpty()) {
                return;
            }

            Vec3 pos = potionEvent.getRayTraceResult().getLocation();
            List<Player> entities = potion.level().getEntities(EntityType.PLAYER, new AABB(pos.x - 5, pos.y - 1, pos.z - 5, pos.x + 5, pos.y + 1, pos.z + 5), entity -> entity.position().distanceTo(pos) <= 4);

            for (Player player : entities) {
                if (player.hasEffect(DSEffects.FIRE)) {
                    continue;
                }

                DragonStateProvider.getOptional(player).ifPresent(dragonStateHandler -> {
                    if(dragonStateHandler.isDragon()){
                        if(dragonStateHandler.getType() == null || !DragonUtils.isType(dragonStateHandler, DragonTypes.CAVE)){
                            return;
                        }
                        player.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.WATER_BURN)), CaveDragonConfig.caveSplashDamage.floatValue());
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void consumeHurtfulItem(LivingEntityUseItemEvent.Finish destroyItemEvent) {
        if (!ServerConfig.penaltiesEnabled || !(destroyItemEvent.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = destroyItemEvent.getItem();
        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return;
        }

        List<ItemHurtConfig> hurtfulItems = switch (data.getType()) {
            case CaveDragonType ignored -> CaveDragonConfig.caveDragonHurtfulItems;
            case SeaDragonType ignored -> SeaDragonConfig.seaDragonHurtfulItems;
            case ForestDragonType ignored -> ForestDragonConfig.hurtfulItems;
            default -> throw new IllegalStateException("Not a valid dragon type: " + data.getType().getClass().getName());
        };

        for (ItemHurtConfig config : hurtfulItems) {
            // TODO :: should only the first non-zero value be relevant or should it potentially call 'hurt' multiple times if an item is present in multiple configs?
            float damage = config.getDamage(stack);

            // TODO :: change config name and allow < 0 items to heal the dragon?
            if (damage > 0) {
                player.hurt(player.damageSources().generic(), damage);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onWaterConsumed(LivingEntityUseItemEvent.Finish destroyItemEvent) {
        if (!ServerConfig.penaltiesEnabled || SeaDragonConfig.seaTicksWithoutWater == 0) {
            return;
        }

        DragonStateProvider.getOptional(destroyItemEvent.getEntity()).ifPresent(handler -> {
            if (handler.isDragon() && handler.getType() instanceof SeaDragonType seaDragonType) {
                ItemStack itemStack = destroyItemEvent.getItem();
                Player player = (Player) destroyItemEvent.getEntity();

                if (!player.level().isClientSide() && SeaDragonConfig.seaAllowWaterBottles && itemStack.getItem() instanceof PotionItem) {
                    Optional<Potion> potion = PotionUtils.getPotion(itemStack);

                    if (potion.isPresent() && potion.get() == Potions.WATER.value() && DragonUtils.isType(handler, DragonTypes.SEA)) {
                        seaDragonType.timeWithoutWater = Math.max(seaDragonType.timeWithoutWater - SeaDragonConfig.seaTicksWithoutWaterRestored, 0);
                        PacketDistributor.sendToPlayersTrackingEntity(player, new SyncDragonType.Data(player.getId(), seaDragonType.writeNBT()));
                    }
                }

                if (itemStack.is(DSItemTags.SEA_DRAGON_HYDRATION) && !player.level().isClientSide()) {
                    seaDragonType.timeWithoutWater = Math.max(seaDragonType.timeWithoutWater - SeaDragonConfig.seaTicksWithoutWaterRestored, 0);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncDragonType.Data(player.getId(), seaDragonType.writeNBT()));
                }
            }
        });
    }

    public static boolean itemIsBlacklisted(Item item) {
        return DRAGON_BLACKLISTED_ITEMS.contains(item);
    }

    @SubscribeEvent // Prevent the player from equipping blacklisted armor (or from mixing light and dark dragon armor)
    public static void preventEquipment(final ItemStackedOnOtherEvent event) {
        ItemStack stack = event.getStackedOnItem(); // FIXME :: this is probably a neoforge bug, this should be carried item -> might be changed in the future
        Player player = event.getPlayer();

        if (stack.isEmpty()) {
            return;
        }

        // Will have to see what type of slots modded inventories may use
        if (!(event.getSlot() instanceof ArmorSlot)) {
            return;
        }

        if (DragonStateProvider.isDragon(player)) {
            if (DragonPenaltyHandler.itemIsBlacklisted(stack.getItem())) {
                event.setCanceled(true);
                return;
            }
        }

        boolean isLightArmor = stack.is(DSItemTags.LIGHT_ARMOR);

        if (isLightArmor && player.hasEffect(DSEffects.HUNTER_OMEN)) {
            event.setCanceled(true);
            return;
        }

        boolean isDarkArmor = stack.is(DSItemTags.DARK_ARMOR);

        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.isEmpty()) {
                continue;
            }

            boolean isActionInvalid = false;

            if (isDarkArmor && armor.is(DSItemTags.LIGHT_ARMOR)) {
                isActionInvalid = true;
            } else if (isLightArmor && (armor.is(DSItemTags.DARK_ARMOR))) {
                isActionInvalid = true;
            }

            if (isActionInvalid) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent // Prevent the player from holding blacklisted items
    public static void dropHeldItems(PlayerTickEvent.Pre event) {
        if (!ServerConfig.penaltiesEnabled) {
            return;
        }

        Player player = event.getEntity();
        DragonStateProvider.getOptional(player).ifPresent(dragonStateHandler -> {
            if (dragonStateHandler.isDragon()) {
                ItemStack mainHandItem = player.getMainHandItem();
                ItemStack offHandItem = player.getOffhandItem();

                if (!mainHandItem.isEmpty() && itemIsBlacklisted(mainHandItem.getItem())) {
                    player.getInventory().removeItem(mainHandItem);
                    player.drop(mainHandItem, false);
                }

                if (!offHandItem.isEmpty() && itemIsBlacklisted(offHandItem.getItem())) {
                    player.getInventory().removeItem(offHandItem);
                    player.drop(offHandItem, false);
                }
            }
        });
    }
}