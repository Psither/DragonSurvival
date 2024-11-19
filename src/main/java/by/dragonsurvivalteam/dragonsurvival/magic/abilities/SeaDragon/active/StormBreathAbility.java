package by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.StormBreathSound;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.*;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.BreathAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import by.dragonsurvivalteam.dragonsurvival.util.TargetingFunctions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSPotions.STORM_BREATH;

@Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
        "■ Elemental breath: a stream of sparks. Targets become §c«Electrified»§r and deal electric damage to everything nearby.\n",
        "■ Charges creepers, and may summon thunderbolts during a storm.\n",
        "■ Range depends on the age of the dragon."
})
@Translation(type = Translation.Type.ABILITY, comments = "Storm Breath")
@RegisterDragonAbility
public class StormBreathAbility extends BreathAbility {
    @Translation(key = "storm_breath", type = Translation.Type.CONFIGURATION, comments = "Enable / Disable the storm breath ability")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "storm_breath")
    public static Boolean isEnabled = true;

    @ConfigRange(min = 0.0, max = 100.0)
    @Translation(key = "storm_breath_damage", type = Translation.Type.CONFIGURATION, comments = "Amount of damage (multiplied by the ability level)")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "storm_breath_damage")
    public static Double damage = 1.0;

    @ConfigRange(min = 0, max = 100)
    @Translation(key = "storm_breath_initial_mana_cost", type = Translation.Type.CONFIGURATION, comments = "Mana cost for starting the cast")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "storm_breath_initial_mana_cost")
    public static Integer initialManaCost = 2;

    @ConfigRange(min = 0.05, max = 10_000)
    @Translation(key = "storm_breath_cooldown", type = Translation.Type.CONFIGURATION, comments = "Cooldown (in seconds) after using the ability")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "storm_breath_cooldown")
    public static Double cooldown = 10.0;

    @ConfigRange(min = 0.05, max = 10_000.0)
    @Translation(key = "storm_breath_cast_time", type = Translation.Type.CONFIGURATION, comments = "Cast time (in seconds)")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "storm_breath_cast_time")
    public static Double castTime = 1.0;

    @ConfigRange(min = 0, max = 100)
    @Translation(key = "storm_breath_sustaining_mana_cost", type = Translation.Type.CONFIGURATION, comments = "Mana cost for sustaining the ability")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "storm_breath_sustaining_mana_cost")
    public static Integer sustainedManaCost = 1;

    @ConfigRange(min = 0.0, max = 100.0)
    @Translation(key = "storm_breath_mana_cost_tick_rate", type = Translation.Type.CONFIGURATION, comments = "Time (in seconds) between ticks of the sustained mana cost being applied")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "storm_breath_mana_cost_tick_rate")
    public static Double sustainedManaCostTickRate = 2.0;

    @ConfigRange(min = 0, max = 100)
    @Translation(key = "storm_breath_chain_count", type = Translation.Type.CONFIGURATION, comments = "Amount of entities the storm breath can chain to at once")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "storm_breath_chain_count")
    public static Integer chainCount = 2;

    // TODO :: move to effect class?
    @ConfigRange(min = 0, max = 100)
    @Translation(key = "charged_effect_chain_count", type = Translation.Type.CONFIGURATION, comments = "Amount of entities the charged effect can chain to at once")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "charged_effect_chain_count")
    public static Integer chargedEffectChainCount = 2;

    @ConfigRange(max = 100)
    @Translation(key = "charged_effect_max_chain", type = Translation.Type.CONFIGURATION, comments = "Determines the max. amount of times the charged effect can chain")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "charged_effect_max_chain")
    public static Integer chargedEffectMaxChain = 5;

    @ConfigRange(min = 0, max = 100)
    @Translation(key = "charged_effect_chain_range", type = Translation.Type.CONFIGURATION, comments = "Determines the max. distance (in blocks) the storm breath and charged effect can chain (between entities)")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "charged_effect_chain_range")
    public static Integer chargedChainRange = 4;

    @ConfigRange(min = 0.0, max = 100.0)
    @Translation(key = "charged_effect_damage_multiplier", type = Translation.Type.CONFIGURATION, comments = "Damage multiplier (scales with ability level)")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "charged_effect_damage_multiplier")
    public static Double chargedEffectDamageMultiplier = 1.0;

    @ConfigType(EntityType.class) // FIXME :: use tag
    @Translation(key = "charged_effect_spread_blacklist", type = Translation.Type.CONFIGURATION, comments = "Entities which will not spread the charged effect - Format: namespace:path")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "charged_effect_spread_blacklist", validation = Validation.RESOURCE_LOCATION)
    public static List<String> chargedSpreadBlacklist = List.of("minecraft:armor_stand", "minecraft:cat", "minecraft:cart", "minecraft:guardian", "minecraft:elder_guardian", "minecraft:enderman", "upgrade_aquatic:thrasher", "upgrade_aquatic:great_thrasher");

    @ConfigType(EntityType.class) // FIXME :: use tag
    @Translation(key = "charged_effect_blacklist", type = Translation.Type.CONFIGURATION, comments = "Entities which are not affected by the charged effect - Format: namespace:path")
    @ConfigOption(side = ConfigSide.SERVER, category = {"sea_dragon", "magic", "abilities", "active", "storm_breath"}, key = "charged_effect_blacklist", validation = Validation.RESOURCE_LOCATION)
    public static List<String> chargedBlacklist = List.of("minecraft:armor_stand", "minecraft:cat", "minecraft:cart", "minecraft:guardian", "minecraft:elder_guardian", "minecraft:enderman", "upgrade_aquatic:thrasher", "upgrade_aquatic:great_thrasher");

    public static void onDamageChecks(LivingEntity entity) {
        if (entity instanceof Creeper creeper) {

            if (!creeper.isPowered()) {
                creeper.getEntityData().set(Creeper.DATA_IS_POWERED, true);
            }
        }
    }

    public static void spark(LivingEntity source, LivingEntity target) {
        if (source.level().isClientSide()) {
            float eyeHeight = source instanceof Player ? 0f : source.getEyeHeight();
            Vec3 start = source.getPosition(eyeHeight);
            Vec3 end = target.getPosition(target.getEyeHeight());

            int parts = 20;

            double xDif = (end.x - start.x) / parts;
            double yDif = (end.y - start.y) / parts;
            double zDif = (end.z - start.z) / parts;

            if (end.x - start.x >= 64 || end.y - start.y >= 64 || end.z - start.z >= 64) {
                return;
            }

            for (int i = 0; i < parts; i++) {
                double x = start.x + xDif * i;
                double y = start.y + yDif * i + eyeHeight;
                double z = start.z + zDif * i;
                source.level().addParticle(new SmallLightningParticleOption(37, true), x, y, z, xDif, yDif, zDif);
            }
        }
    }

    @Override
    public int getManaCost() {
        return sustainedManaCost;
    }

    @Override
    public Integer[] getRequiredLevels() {
        return new Integer[]{0,
                10,
                30,
                50};
    }

    @Override
    public int getSkillCooldown() {
        return Functions.secondsToTicks(cooldown);
    }

    public static void chargedEffectSparkle(Player player, LivingEntity source, int chainRange, int maxChainTargets, double damage) {
        List<LivingEntity> secondaryTargets = getEntityLivingBaseNearby(source, chainRange);
        secondaryTargets.removeIf(e -> !isValidTarget(source, e));

        if (secondaryTargets.size() > maxChainTargets) {
            secondaryTargets.sort((c1, c2) -> Boolean.compare(c1.hasEffect(DSEffects.CHARGED), c2.hasEffect(DSEffects.CHARGED)));
            secondaryTargets = secondaryTargets.subList(0, maxChainTargets);
        }

        secondaryTargets.add(source);

        for (LivingEntity target : secondaryTargets) {
            if (player != null) {
                TargetingFunctions.attackTargets(player, eTarget -> eTarget.hurt(player.damageSources()/* TODO 1.20 :: Unsure */.mobProjectile(source, player), (float) damage), target);
            } else {
                target.hurt(target.damageSources().mobAttack(source), (float) damage);
            }

            onDamageChecks(target);

            if (!chargedSpreadBlacklist.contains(ResourceHelper.getKey(source).toString())) {
                if (target != source) {
                    EntityStateHandler sourceData = source.getData(DragonSurvival.ENTITY_HANDLER);
                    EntityStateHandler targetData = target.getData(DragonSurvival.ENTITY_HANDLER);

                    targetData.chainCount = sourceData.chainCount + 1;

                    if (!target.level().isClientSide()) {
                        if (target.getRandom().nextInt(100) < 40) {
                            if (targetData.chainCount < chargedEffectMaxChain || chargedEffectMaxChain == -1) {
                                targetData.lastAfflicted = player != null ? player.getId() : -1;
                                target.addEffect(new MobEffectInstance(DSEffects.CHARGED, Functions.secondsToTicks(10), 0, false, true));
                            }
                        }
                    }

                    if (player != null) {
                        if (player.getRandom().nextInt(100) < 50) {
                            if (!player.level().isClientSide()) {
                                player.addEffect(new MobEffectInstance(DSEffects.CHARGED, Functions.secondsToTicks(30)));
                            }
                        }
                    }
                    spark(source, target);
                }
            }
        }
    }

    public static float getDamage(int level) {
        return (float) (damage * level);
    }

    public static boolean isValidTarget(LivingEntity attacker, LivingEntity target) {
        if (target == null || attacker == null) {
            return false;
        }
        if (chargedBlacklist.contains(ResourceHelper.getKey(target).toString())) {
            return false;
        }
        if (chargedSpreadBlacklist.contains(ResourceHelper.getKey(attacker).toString())) {
            return false;
        }

        if (target.getLastHurtByMob() == attacker && target.getLastHurtByMobTimestamp() + Functions.secondsToTicks(1) < target.tickCount) {
            return false;
        }

        return TargetingFunctions.isValidTarget(attacker, target) && !DragonUtils.isType(target, DragonTypes.SEA);
    }

    public void hurtTarget(LivingEntity entity) {
        TargetingFunctions.attackTargets(getPlayer(), e -> e.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.SEA_DRAGON_BREATH), player), getDamage()), entity);
        onDamage(entity);

        if (player.getRandom().nextInt(100) < 50) {
            if (!player.level().isClientSide()) {
                player.addEffect(new MobEffectInstance(DSEffects.CHARGED, Functions.secondsToTicks(30)));
            }
        }

        if (!entity.level().isClientSide()) {
            if (!chargedBlacklist.contains(ResourceHelper.getKey(entity).toString())) {
                if (entity.getRandom().nextInt(100) < 40) {
                    EntityStateHandler data = entity.getData(DragonSurvival.ENTITY_HANDLER);
                    data.lastAfflicted = player.getId();
                    data.chainCount = 1;

                    entity.addEffect(new MobEffectInstance(DSEffects.CHARGED, Functions.secondsToTicks(10), 0, false, true));
                }
            }
        }
    }

    @Override
    public String getName() {
        return "storm_breath";
    }

    @Override
    public Fluid clipContext() {
        return ClipContext.Fluid.NONE;
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.SEA;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/sea/storm_breath_0.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/sea/storm_breath_1.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/sea/storm_breath_2.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/sea/storm_breath_3.png"),
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/sea/storm_breath_4.png")
        };
    }


    @Override
    public ArrayList<Component> getLevelUpInfo() {
        ArrayList<Component> list = super.getLevelUpInfo();
        list.add(Component.translatable(LangKey.ABILITY_DAMAGE, "+" + damage));
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
        return super.isDisabled() || !isEnabled;
    }


    @OnlyIn(Dist.CLIENT)
    public void sound() {
        Vec3 pos = player.getEyePosition(1.0F);
        SimpleSoundInstance startingSound = new SimpleSoundInstance(
                DSSounds.STORM_BREATH_START.get(),
                SoundSource.PLAYERS,
                1.0F, 1.0F,
                SoundInstance.createUnseededRandom(),
                pos.x, pos.y, pos.z
        );
        Minecraft.getInstance().getSoundManager().playDelayed(startingSound, 0);
        Minecraft.getInstance().getSoundManager().stop(ResourceLocation.fromNamespaceAndPath(MODID, "storm_breath_loop"), SoundSource.PLAYERS);
        Minecraft.getInstance().getSoundManager().queueTickingSound(new StormBreathSound(this));
    }

    @Override
    public void onBlock(BlockPos pos, BlockState blockState, Direction direction) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (blockState.isSolid()) {
            if (/* 30% */ player.getRandom().nextInt(100) < 30) {
                AreaEffectCloud entity = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, player.level());
                entity.setWaitTime(0);
                entity.setPos(pos.above().getX(), pos.above().getY(), pos.above().getZ());
                entity.setPotionContents(new PotionContents(STORM_BREATH));
                entity.setDuration(Functions.secondsToTicks(2));
                entity.setRadius(1);
                entity.setParticle(new SmallLightningParticleOption(37, false));
                entity.setOwner(player);
                serverLevel.addFreshEntity(entity);
            }
        }

        if (blockState.getFluidState().is(FluidTags.WATER)) {
            if (/* 30% */ player.getRandom().nextInt(100) < 30) {
                AreaEffectCloud entity = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, player.level());
                entity.setWaitTime(0);
                entity.setPos(pos.getX(), pos.getY(), pos.getZ());
                entity.setPotionContents(new PotionContents(STORM_BREATH));
                entity.setDuration(Functions.secondsToTicks(2));
                entity.setRadius(0.45f);
                entity.setParticle(new SmallLightningParticleOption(37, true));
                entity.setOwner(player);
                serverLevel.addFreshEntity(entity);
            }
        }

        Level level = player.level();
        if (level.isClientSide) {
            if (player.tickCount % 40 == 0) {
                if (level.isThundering()) {
                    if (player.getRandom().nextInt(100) < 30) {
                        if (level.canSeeSky(pos)) {
                            LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(player.level());
                            lightningboltentity.moveTo(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                            lightningboltentity.setCause((ServerPlayer) player);
                            level.addFreshEntity(lightningboltentity);
                            level.playSound(player, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 5F, 1.0F);
                        }
                    }
                }
            }
        }
    }


    @OnlyIn(Dist.CLIENT)
    public void stopSound() {
        if (DSSounds.STORM_BREATH_END != null) {
            Vec3 pos = player.getEyePosition(1.0F);
            SimpleSoundInstance endSound = new SimpleSoundInstance(
                    DSSounds.STORM_BREATH_END.get(),
                    SoundSource.PLAYERS,
                    1.0F, 1.0F,
                    SoundInstance.createUnseededRandom(),
                    pos.x, pos.y, pos.z
            );
            Minecraft.getInstance().getSoundManager().playDelayed(endSound, 0);
        }

        Minecraft.getInstance().getSoundManager().stop(ResourceLocation.fromNamespaceAndPath(MODID, "storm_breath_loop"), SoundSource.PLAYERS);
    }


    @Override
    public void onChanneling(Player player, int castDuration) {
        super.onChanneling(player, castDuration);

        if (player.level().isClientSide() && castDuration <= 0 && FMLEnvironment.dist == Dist.CLIENT) {
            sound();
        }

        if (player.level().isClientSide()) {
            for (int i = 0; i < calculateNumberOfParticles(DragonStateProvider.getData(player).getSize()) / 6; i++) {
                double xSpeed = speed * 1f * xComp;
                double ySpeed = speed * 1f * yComp;
                double zSpeed = speed * 1f * zComp;
                player.level().addParticle(new SmallLightningParticleOption(37, true), dx, dy, dz, xSpeed, ySpeed, zSpeed);
            }

            for (int i = 0; i < calculateNumberOfParticles(DragonStateProvider.getData(player).getSize()) / 12; i++) {
                double xSpeed = speed * xComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - xComp * xComp);
                double ySpeed = speed * yComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - yComp * yComp);
                double zSpeed = speed * zComp + spread * 0.7 * (player.getRandom().nextFloat() * 2 - 1) * Math.sqrt(1 - zComp * zComp);
                player.level().addParticle(new LargeLightningParticleOption(37, false), dx, dy, dz, xSpeed, ySpeed, zSpeed);
            }
        }

        hitEntities();

        if (player.tickCount % 10 == 0) {
            hitBlocks();
        }
    }

    @Override
    public boolean canHitEntity(LivingEntity entity) {
        return !(entity instanceof Player) || player.canHarmPlayer((Player) entity);
    }

    @Override
    public void onDamage(LivingEntity entity) {
        onDamageChecks(entity);
    }

    @Override
    public void onEntityHit(LivingEntity entityHit) {
        hurtTarget(entityHit);
        chargedEffectSparkle(player, entityHit, chargedChainRange, chainCount, chargedEffectDamageMultiplier);
    }


    @Override
    public float getDamage() {
        return getDamage(getLevel());
    }

    @Override
    public int getSkillChargeTime() {
        return Functions.secondsToTicks(castTime);
    }

    @Override
    public int getContinuousManaCostTime() {
        return Functions.secondsToTicks(sustainedManaCostTickRate);
    }

    @Override
    public int getInitManaCost() {
        return initialManaCost;
    }

    @Override
    public void castComplete(Player player) {
        stopSound();
    }

    @Override
    @SuppressWarnings("RedundantMethodOverride")
    public boolean requiresStationaryCasting() {
        return false;
    }
}