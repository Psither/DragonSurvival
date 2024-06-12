package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonConfigHandler.DRAGON_DESTRUCTIBLE_BLOCKS;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes.entityDamageSource;

@Mod.EventBusSubscriber
public class DragonDestructionHandler {

    private static int crushTickCounter = 0;
    private static boolean isBreakingMultipleBlocks = false;

    private static void checkAndDestroyCollidingBlocks(DragonStateHandler dragonStateHandler, TickEvent.PlayerTickEvent event, AABB boundingBox) {
        if(!ServerConfig.allowLargeBlockDestruction) {
            return;
        }

        if(ServerConfig.largeBlockDestructionSize > dragonStateHandler.getSize()) {
            return;
        }

        // Copied from checkWalls in EnderDragon.java
        int i = Mth.floor(boundingBox.minX);
        int j = Mth.floor(boundingBox.minY);
        int k = Mth.floor(boundingBox.minZ);
        int l = Mth.ceil(boundingBox.maxX);
        int i1 = Mth.ceil(boundingBox.maxY);
        int j1 = Mth.ceil(boundingBox.maxZ);

        RandomSource random = new XoroshiroRandomSource(event.player.level().getGameTime());

        for (int k1 = i; k1 <= l; ++k1) {
            for (int l1 = j; l1 <= i1; ++l1) {
                for (int i2 = k; i2 <= j1; ++i2) {
                    BlockPos blockpos = new BlockPos(k1, l1, i2);
                    BlockState blockstate = event.player.level().getBlockState(blockpos);
                    if (!blockstate.isAir()) {
                        if(ServerConfig.useBlacklistForDestructibleBlocks) {
                            if(!DRAGON_DESTRUCTIBLE_BLOCKS.contains(blockstate.getBlock())) {
                                if(random.nextFloat() > ServerConfig.largeBlockDestructionRemovePercentage) {
                                    event.player.level().destroyBlock(blockpos, false);
                                }
                                else {
                                    event.player.level().removeBlock(blockpos, false);
                                }
                            }
                        }
                        else {
                            // #TODO: Make the MINEABLE_WITH_AXE and FLOWERS tag configurable in data
                            if(DRAGON_DESTRUCTIBLE_BLOCKS.contains(blockstate.getBlock()) || blockstate.canBeReplaced() || blockstate.is(BlockTags.MINEABLE_WITH_AXE) || blockstate.is(BlockTags.FLOWERS)) {
                                if(random.nextFloat() > ServerConfig.largeBlockDestructionRemovePercentage) {
                                    event.player.level().destroyBlock(blockpos, false);
                                }
                                else {
                                    event.player.level().removeBlock(blockpos, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void checkAndDamageCrushedEntities(DragonStateHandler dragonStateHandler, ServerPlayer player, AABB boundingBox) {
        if(!ServerConfig.allowCrushing) {
            return;
        }

        if(ServerConfig.crushingSize > dragonStateHandler.getSize()) {
            return;
        }

        if(--crushTickCounter > 0) {
            return;
        }

        // Get only the bounding box of the player's feet (estimate by using only the bottom 1/3 of the bounding box)
        AABB feetBoundingBox = new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY - (boundingBox.maxY - boundingBox.minY) / 3.0, boundingBox.maxZ);

        for(var entity : player.level().getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT, player, feetBoundingBox)){
            // If the entity being crushed is too big, don't damage it.
            if(entity.getBoundingBox().getSize() > boundingBox.getSize() / 2.0f) {
                continue;
            }

            entity.hurt(entityDamageSource(player.level(), DSDamageTypes.CRUSHED, player), (float)(dragonStateHandler.getSize() * ServerConfig.crushingDamageScalar));
            crushTickCounter = ServerConfig.crushingTickDelay;
        }
    }

    @SubscribeEvent
    public static void destroyBlocksInRadius(BlockEvent.BreakEvent event)
    {
        if(isBreakingMultipleBlocks) {
            return;
        }

        if(!ServerConfig.allowLargeScaling) {
            return;
        }

        if(ServerConfig.largeBlockBreakRadiusScalar <= 0.0f) {
            return;
        }

        if(!(event.getPlayer() instanceof ServerPlayer player)){
            return;
        }

        if(player.isCrouching()) {
            return;
        }

        DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
            if(dragonStateHandler.isDragon()) {
                if(dragonStateHandler.getSize() < ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
                    return;
                }

                isBreakingMultipleBlocks = true;

                event.setCanceled(true);

                // Break the blocks in a radius around the broken block
                int radius = (int) Math.floor((dragonStateHandler.getSize() - ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / 60.f * ServerConfig.largeBlockBreakRadiusScalar);
                BlockPos pos = event.getPos();
                for(int x = -radius; x <= radius; x++) {
                    for(int y = -radius; y <= radius; y++) {
                        for(int z = -radius; z <= radius; z++) {
                            BlockPos newPos = pos.offset(x, y, z);
                            player.gameMode.destroyBlock(newPos);
                        }
                    }
                }

                isBreakingMultipleBlocks = false;
            }
        });
    }



    @SubscribeEvent
    public static void checkAndDestroyCollidingBlocksAndCrushedEntities(TickEvent.PlayerTickEvent event) {
        if(!ServerConfig.allowLargeBlockDestruction && !ServerConfig.allowCrushing) {
            return;
        }

        if(!(event.player instanceof ServerPlayer player)){
            return;
        }

        DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
            if(dragonStateHandler.isDragon()) {

                if(player.isCrouching()) {
                    return;
                }

                Vec2 deltaXZ = new Vec2((float)player.getDeltaMovement().x, (float)player.getDeltaMovement().z);
                if(deltaXZ.length() < 0.05f && Math.abs(player.getDeltaMovement().y) < 0.25f) {
                    return;
                }

                AABB boundingBox;
                if (ServerConfig.sizeChangesHitbox) {
                    boolean squish = dragonStateHandler.getBody() == null ? dragonStateHandler.getBody().isSquish() : false;
                    double size = dragonStateHandler.getSize();
                    double height = DragonSizeHandler.calculateModifiedHeight(DragonSizeHandler.calculateDragonHeight(size, ServerConfig.hitboxGrowsPastHuman), event.player.getPose(), ServerConfig.sizeChangesHitbox, squish);
                    double width = DragonSizeHandler.calculateDragonWidth(size, ServerConfig.hitboxGrowsPastHuman) / 2.0D;
                    boundingBox = DragonSizeHandler.calculateDimensions(width, height).makeBoundingBox(player.position());
                } else {
                    boundingBox = player.getBoundingBox();
                }

                boundingBox = boundingBox.inflate(1.25);

                checkAndDestroyCollidingBlocks(dragonStateHandler, event, boundingBox);
                checkAndDamageCrushedEntities(dragonStateHandler, player, boundingBox);
            }
        });
    }
}
