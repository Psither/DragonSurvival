package by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.active;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.PoisonBreathSound;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargePoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallPoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.*;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSPotions.FOREST_BREATH;

@RegisterDragonAbility
public class ForestBreathAbility extends BreathAbility {
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreath", comment = "Whether the forest breath ability should be enabled")
    public static Boolean forestBreath = true;

    @ConfigRange(min = 0.0, max = 100.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathDamage", comment = "The amount of damage the forest breath ability deals. This value is multiplied by the skill level.")
    public static Double forestBreathDamage = 2.0;

    @ConfigRange(min = 0.05, max = 10000.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathCooldown", comment = "The cooldown in seconds of the forest breath ability")
    public static Double forestBreathCooldown = 5.0;

    @ConfigRange(min = 0.05, max = 10000.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathCasttime", comment = "The casttime in seconds of the forest breath ability")
    public static Double forestBreathCasttime = 1.0;

    @ConfigRange(min = 0, max = 100)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathInitialMana", comment = "The mana cost for starting the forest breath ability")
    public static Integer forestBreathInitialMana = 2;

    @ConfigRange(min = 0, max = 100)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathOvertimeMana", comment = "The mana cost of sustaining the forest breath ability")
    public static Integer forestBreathOvertimeMana = 1;

    @ConfigRange(min = 0.5, max = 100.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "forestBreathManaTicks", comment = "How often in seconds, mana is consumed while using forest breath")
    public static Double forestBreathManaTicks = 2.0;

    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "allowDirtTransformation", comment = "Allow the forest breath to transform dirt into nature related blocks")
    public static Boolean allowDirtTransformation = true;

    @ConfigType(Block.class)
    @ConfigOption(side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "forest_breath"}, key = "dirtTransformationBlocks", validation = Validation.RESOURCE_LOCATION_NUMBER, comment = "Blocks which dirt can be transformed into. Formatting: mod_id:block_id:chance (e.g. minecraft:podzol:7) (The chance is x out of 100)")
    public static List<String> dirtTransformationBlocks = List.of(
            "minecraft:moss_block:3",
            "minecraft:podzol:5",
            "minecraft:mycelium:1",
            "minecraft:grass_block:25"
    );

    @Override
    public String getName() {
        return "poisonous_breath";
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.FOREST;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/poisonous_breath_0.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/poisonous_breath_1.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/poisonous_breath_2.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/poisonous_breath_3.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/poisonous_breath_4.png")};
    }

    @Override
    public ArrayList<Component> getLevelUpInfo() {
        ArrayList<Component> list = super.getLevelUpInfo();
        list.add(Component.translatable("ds.skill.damage", "+" + forestBreathDamage));
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
        return super.isDisabled() || !forestBreath;
    }

    @Override
    public void onBlock(final BlockPos blockPosition, final BlockState blockState, final Direction direction) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (blockState.isSolid()) {
            if (/* 30% */ player.getRandom().nextInt(100) < 30) {
                AreaEffectCloud entity = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, player.level());
                entity.setWaitTime(0);
                entity.setPos(blockPosition.above().getX(), blockPosition.above().getY(), blockPosition.above().getZ());
                entity.setPotionContents(new PotionContents(FOREST_BREATH));
                entity.setDuration(Functions.secondsToTicks(2));
                entity.setRadius(1);
                entity.setParticle(new LargePoisonParticleOption(37, false));
                entity.setOwner(player);
                serverLevel.addFreshEntity(entity);
            }
        }

        if (blockState.getBlock() == Blocks.POTATOES) {
            if (/* 10% */ player.getRandom().nextInt(100) < 10) {
                if (((PotatoBlock) blockState.getBlock()).isMaxAge(blockState)) {
                    serverLevel.destroyBlock(blockPosition, false);
                    serverLevel.addFreshEntity(new ItemEntity(serverLevel, blockPosition.getX() + 0.5, blockPosition.getY(), blockPosition.getZ() + 0.5, new ItemStack(Items.POISONOUS_POTATO)));
                }
            }
        }

        if (/* 50% */ player.getRandom().nextInt(100) < 50) {
            if (!blockState.is(DSBlockTags.FOREST_BREATH_GROW_BLACKLIST) && blockState.getBlock() instanceof BonemealableBlock bonemealableBlock) {
                if (bonemealableBlock.isValidBonemealTarget(serverLevel, blockPosition, blockState)) {
                    if (bonemealableBlock.isBonemealSuccess(serverLevel, player.getRandom(), blockPosition, blockState)) {
                        for (int i = 0; i < 3; i++) {
                            if (bonemealableBlock instanceof DoublePlantBlock) {
                                if (!blockState.hasProperty(DoublePlantBlock.HALF)) {
                                    continue;
                                }
                            }

                            bonemealableBlock.performBonemeal(serverLevel, player.getRandom(), blockPosition, blockState);
                        }
                    }
                }
            }
        }

        if (allowDirtTransformation) {
            if ((blockState.is(Blocks.DIRT) || blockState.is(Blocks.COARSE_DIRT)) && serverLevel.getBlockState(blockPosition.above()).is(Blocks.AIR)) {
                List<String> toProcess = new ArrayList<>(dirtTransformationBlocks);

                while (!toProcess.isEmpty()) {
                    int index = player.getRandom().nextInt(toProcess.size());
                    String element = toProcess.get(index);
                    String[] data = element.split(":");

                    if (player.getRandom().nextInt(100) < Integer.parseInt(data[2])) {
                        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(data[0], data[1]);

                        if (BuiltInRegistries.BLOCK.containsKey(resourceLocation)) {
                            serverLevel.setBlock(blockPosition, BuiltInRegistries.BLOCK.get(resourceLocation).defaultBlockState(), Block.UPDATE_ALL);
                            break;
                        }
                    }

                    toProcess.remove(index);
                }
            }
        }
    }

    @Override
    public void onChanneling(Player player, int castDuration) {
        super.onChanneling(player, castDuration);

        if (player.hasEffect(DSEffects.STRESS)) {
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
                player.level().addParticle(new LargePoisonParticleOption(37, true), dx, dy, dz, xSpeed, ySpeed, zSpeed);
            }

            for (int i = 0; i < calculateNumberOfParticles(DragonStateProvider.getData(player).getSize()) / 2; i++) {
                double xSpeed = speed * xComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - xComp * xComp);
                double ySpeed = speed * yComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - yComp * yComp);
                double zSpeed = speed * zComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - zComp * zComp);
                player.level().addParticle(new SmallPoisonParticleOption(37, false), dx, dy, dz, xSpeed, ySpeed, zSpeed);
            }
        }

        hitEntities();

        if (player.tickCount % 10 == 0) {
            hitBlocks();
        }
    }


    @Override
    public int getManaCost() {
        return forestBreathOvertimeMana;
    }

    @Override
    public Integer[] getRequiredLevels() {
        return new Integer[]{0, 10, 30, 50};
    }


    @Override
    public int getSkillCooldown() {
        return Functions.secondsToTicks(forestBreathCooldown);
    }


    @OnlyIn(Dist.CLIENT)
    public void stopSound() {
        if (DSSounds.FOREST_BREATH_END != null) {
            Vec3 pos = player.getEyePosition(1.0F);
            SimpleSoundInstance endSound = new SimpleSoundInstance(
                    DSSounds.FOREST_BREATH_END.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.0F,
                    SoundInstance.createUnseededRandom(),
                    pos.x, pos.y, pos.z
            );
            Minecraft.getInstance().getSoundManager().playDelayed(endSound, 0);
        }


        Minecraft.getInstance().getSoundManager().stop(ResourceLocation.fromNamespaceAndPath(MODID, "forest_breath_loop"), SoundSource.PLAYERS);
    }

    @Override
    public boolean canHitEntity(LivingEntity entity) {
        return !(entity instanceof Player) || player.canHarmPlayer((Player) entity);
    }

    @Override
    public void onEntityHit(LivingEntity entityHit) {
        super.onEntityHit(entityHit);

        if (!entityHit.level().isClientSide()) {
            if (entityHit.getRandom().nextInt(100) < 30) {
                entityHit.getData(DragonSurvival.ENTITY_HANDLER).lastAfflicted = player != null ? player.getId() : -1;
                entityHit.addEffect(new MobEffectInstance(DSEffects.DRAIN, Functions.secondsToTicks(10), 0, false, true));
            }
        }
    }

    @Override
    public void onDamage(LivingEntity entity) {
    }

    @Override
    public float getDamage() {
        return getDamage(getLevel());
    }

    public static float getDamage(int level) {
        return (float) (forestBreathDamage * level);
    }


    @OnlyIn(Dist.CLIENT)
    public void sound() {
        Vec3 pos = player.getEyePosition(1.0F);
        SimpleSoundInstance startingSound = new SimpleSoundInstance(
                DSSounds.FOREST_BREATH_START.get(),
                SoundSource.PLAYERS,
                1.0F, 1.0F,
                SoundInstance.createUnseededRandom(),
                pos.x, pos.y, pos.z
        );
        Minecraft.getInstance().getSoundManager().playDelayed(startingSound, 0);
        Minecraft.getInstance().getSoundManager().stop(ResourceLocation.fromNamespaceAndPath(MODID, "forest_breath_loop"), SoundSource.PLAYERS);
        Minecraft.getInstance().getSoundManager().queueTickingSound(new PoisonBreathSound(this));
    }

    @Override
    public int getSkillChargeTime() {
        return Functions.secondsToTicks(forestBreathCasttime);
    }

    @Override
    public int getContinuousManaCostTime() {
        return Functions.secondsToTicks(forestBreathManaTicks);
    }

    @Override
    public int getInitManaCost() {
        return forestBreathInitialMana;
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
        return DSBlockTags.FOREST_BREATH_DESTRUCTIBLE;
    }
}