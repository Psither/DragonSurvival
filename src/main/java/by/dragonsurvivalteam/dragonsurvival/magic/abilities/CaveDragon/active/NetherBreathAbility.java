package by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.active;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.FireBreathSound;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeFireParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallFireParticleOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.BurnAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.BreathAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.ArrayList;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSPotions.CAVE_BREATH;

@RegisterDragonAbility
public class NetherBreathAbility extends BreathAbility {
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathEnabled", comment = "Whether the firebreath ability should be enabled")
    public static Boolean fireBreathEnabled = true;

    @ConfigRange(min = 0, max = 100.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathDamage", comment = "The amount of damage the firebreath ability deals. This value is multiplied by the skill level.")
    public static Double fireBreathDamage = 3.0;

    @ConfigRange(min = 0, max = 100)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathInitialMana", comment = "The mana cost for starting the firebreath ability")
    public static Integer fireBreathInitialMana = 2;

    @ConfigRange(min = 0, max = 100)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathOvertimeMana", comment = "The mana cost of sustaining the firebreath ability")
    public static Integer fireBreathOvertimeMana = 1;

    @ConfigRange(min = 0.5, max = 100.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathManaTicks", comment = "How often in seconds, mana is consumed while using fire breath")
    public static Double fireBreathManaTicks = 2.0;

    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathSpreadsFire", comment = "Whether the fire breath actually spreads fire when used")
    public static Boolean fireBreathSpreadsFire = true;

    @ConfigRange(min = 0.05, max = 10000.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathCooldown", comment = "The cooldown in seconds of the fire breath ability")
    public static Double fireBreathCooldown = 5.0;

    @ConfigRange(min = 0.05, max = 10000.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "fire_breath"}, key = "fireBreathCasttime", comment = "The cast time in seconds of the fire breath ability")
    public static Double fireBreathCasttime = 1.0;

    @Override
    public String getName() {
        return "nether_breath";
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.CAVE;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/nether_breath_0.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/nether_breath_1.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/nether_breath_2.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/nether_breath_3.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/nether_breath_4.png")};
    }

    @Override
    public ArrayList<Component> getLevelUpInfo() {
        ArrayList<Component> list = super.getLevelUpInfo();
        list.add(Component.translatable("ds.skill.damage", "+" + fireBreathDamage));
        return list;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !fireBreathEnabled;
    }

    @Override
    public int getManaCost() {
        return fireBreathOvertimeMana;
    }

    @Override
    public Integer[] getRequiredLevels() {
        return new Integer[]{0, 10, 30, 50};
    }

    @Override
    public int getSkillCooldown() {
        return Functions.secondsToTicks(fireBreathCooldown);
    }

    @Override
    public void onBlock(final BlockPos blockPosition, final BlockState blockState, final Direction direction) {
        if (!player.level().isClientSide) {
            if (fireBreathSpreadsFire) {
                BlockPos firePosition = blockPosition.relative(direction);
                Block block = blockState.getBlock();

                if (block instanceof TntBlock tnt) {
                    tnt.onCaughtFire(blockState, player.level(), blockPosition, direction, player);
                    player.level().setBlock(blockPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                } else if (block instanceof CampfireBlock && !blockState.getValue(CampfireBlock.LIT)) {
                    player.level().setBlock(blockPosition, blockState.setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL_IMMEDIATE);
                } else if (FireBlock.canBePlacedAt(player.level(), firePosition, direction) && player.getRandom().nextInt(100) < 50) {
                    BlockState fireBlockState = FireBlock.getState(player.level(), firePosition);
                    player.level().setBlock(firePosition, fireBlockState, Block.UPDATE_ALL_IMMEDIATE);
                    blockState.onCaughtFire(player.level(), blockPosition, direction, player);
                }
            }

            BurnAbility burnAbility = DragonAbilities.getSelfAbility(player, BurnAbility.class);

            if (player.getRandom().nextInt(100) < burnAbility.level * 15) {
                BlockState blockAbove = player.level().getBlockState(blockPosition.above());

                if (blockAbove.getBlock() == Blocks.AIR) {
                    AreaEffectCloud entity = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, player.level());
                    entity.setWaitTime(0);
                    entity.setPos(blockPosition.above().getX(), blockPosition.above().getY(), blockPosition.above().getZ());
                    entity.setPotionContents(new PotionContents(CAVE_BREATH));
                    entity.setDuration(Functions.secondsToTicks(2));
                    entity.setRadius(1);
                    entity.setParticle(new SmallFireParticleOption(37, false));
                    entity.setOwner(player);
                    player.level().addFreshEntity(entity);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                if (player.getRandom().nextInt(100) < 20) {
                    player.level().addParticle(ParticleTypes.LAVA, blockPosition.above().getX(), blockPosition.above().getY(), blockPosition.above().getZ(), 0, 0.05, 0);
                }
            }

            if (blockState.getBlock() == Blocks.WATER) {
                for (int i = 0; i < 4; i++) {
                    if (player.getRandom().nextInt(100) < 90) {
                        player.level().addParticle(ParticleTypes.BUBBLE_COLUMN_UP, blockPosition.above().getX(), blockPosition.above().getY(), blockPosition.above().getZ(), 0, 0.05, 0);
                    }
                }
            }
        }
    }

    @Override
    public void onChanneling(Player player, int castDuration) {
        super.onChanneling(player, castDuration);

        if (player.isInWaterRainOrBubble() || player.level().isRainingAt(player.blockPosition())) {
            if (player.level().isClientSide()) {
                if (player.tickCount % 10 == 0) {
                    player.playSound(SoundEvents.LAVA_EXTINGUISH, 0.25F, 1F);
                }

                for (int i = 0; i < 12; i++) {
                    double xSpeed = speed * 1f * xComp;
                    double ySpeed = speed * 1f * yComp;
                    double zSpeed = speed * 1f * zComp;
                    player.level().addParticle(ParticleTypes.SMOKE, dx, dy, dz, xSpeed, ySpeed, zSpeed);
                }
            }
            return;
        }

        if (player.level().isClientSide() && castDuration <= 0 && FMLEnvironment.dist.isClient()) {
            sound();
        }

        if (player.level().isClientSide()) {
            for (int i = 0; i < calculateNumberOfParticles(DragonStateProvider.getData(player).getSize()); i++) {
                double xSpeed = speed * 1f * xComp;
                double ySpeed = speed * 1f * yComp;
                double zSpeed = speed * 1f * zComp;
                player.level().addParticle(new SmallFireParticleOption(37, true), dx, dy, dz, xSpeed, ySpeed, zSpeed);
            }

            for (int i = 0; i < calculateNumberOfParticles(DragonStateProvider.getData(player).getSize()) / 2; i++) {
                double xSpeed = speed * xComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - xComp * xComp);
                double ySpeed = speed * yComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - yComp * yComp);
                double zSpeed = speed * zComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - zComp * zComp);
                player.level().addParticle(new LargeFireParticleOption(37, false), dx, dy, dz, xSpeed, ySpeed, zSpeed);
            }
        }

        hitEntities();

        if (player.tickCount % 10 == 0) {
            hitBlocks();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void sound() {
        Vec3 pos = player.getEyePosition(1.0F);
        SimpleSoundInstance startingSound = new SimpleSoundInstance(
                DSSounds.FIRE_BREATH_START.get(),
                SoundSource.PLAYERS,
                1.0F, 1.0F,
                SoundInstance.createUnseededRandom(),
                pos.x, pos.y, pos.z
        );
        Minecraft.getInstance().getSoundManager().playDelayed(startingSound, 0);
        Minecraft.getInstance().getSoundManager().stop(ResourceLocation.fromNamespaceAndPath(MODID, "fire_breath_loop"), SoundSource.PLAYERS);
        Minecraft.getInstance().getSoundManager().queueTickingSound(new FireBreathSound(this));
    }

    @OnlyIn(Dist.CLIENT)
    public void stopSound() {
        if (DSSounds.FIRE_BREATH_END != null) {
            Vec3 pos = player.getEyePosition(1.0F);
            SimpleSoundInstance endSound = new SimpleSoundInstance(
                    DSSounds.FIRE_BREATH_END.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.0F,
                    SoundInstance.createUnseededRandom(),
                    pos.x, pos.y, pos.z
            );

            Minecraft.getInstance().getSoundManager().playDelayed(endSound, 0);
        }

        Minecraft.getInstance().getSoundManager().stop(ResourceLocation.fromNamespaceAndPath(MODID, "fire_breath_loop"), SoundSource.PLAYERS);
    }

    @Override
    public boolean canHitEntity(LivingEntity entity) {
        return (!(entity instanceof Player) || player.canHarmPlayer((Player) entity)) && !entity.fireImmune();
    }

    @Override
    public void onEntityHit(LivingEntity entityHit) {
        if (!entityHit.isOnFire()) {
            // Short enough fire duration to not cause fire damage but still drop cooked items
            entityHit.setRemainingFireTicks(1);
        }

        super.onEntityHit(entityHit);

        if (!entityHit.level().isClientSide()) {
            BurnAbility burnAbility = DragonAbilities.getSelfAbility(player, BurnAbility.class);

            if (entityHit.getRandom().nextInt(100) < burnAbility.level * 15) {
                entityHit.getData(DragonSurvival.ENTITY_HANDLER).lastAfflicted = player != null ? player.getId() : -1;
                entityHit.addEffect(new MobEffectInstance(DSEffects.BURN, Functions.secondsToTicks(10), 0, false, true));
            }
        }
    }

    @Override
    public void onDamage(LivingEntity entity) {
        entity.setRemainingFireTicks(Functions.secondsToTicks(30));
    }

    public static float getDamage(int level) {
        return (float) (fireBreathDamage * level);
    }

    @Override
    public float getDamage() {
        return getDamage(getLevel());
    }

    @Override
    public int getSkillChargeTime() {
        return Functions.secondsToTicks(fireBreathCasttime);
    }

    @Override
    public int getContinuousManaCostTime() {
        return Functions.secondsToTicks(fireBreathManaTicks);
    }

    @Override
    public int getInitManaCost() {
        return fireBreathInitialMana;
    }

    @Override
    public void castComplete(Player player) {
        if (player.level().isClientSide() && FMLEnvironment.dist.isClient()) {
            stopSound();
        }
    }

    @Override
    @SuppressWarnings("RedundantMethodOverride")
    public boolean requiresStationaryCasting() {
        return false;
    }

    public static TagKey<Block> getDestructibleBlocks() {
        return DSBlockTags.NETHER_BREATH_DESTRUCTIBLE;
    }
}