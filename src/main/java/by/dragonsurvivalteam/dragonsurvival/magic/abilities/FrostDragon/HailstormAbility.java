package by.dragonsurvivalteam.dragonsurvival.magic.abilities.FrostDragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.DiveAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@RegisterDragonAbility
public class HailstormAbility extends DiveAbility {
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "hailstorm"}, key = "hailStorm", comment = "Whether the hailstorm ability should be enabled" )
    public static Boolean hailStorm = true;

    @ConfigRange( min = 0.05, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "hailstorm"}, key = "hailstormCooldown", comment = "The cooldown in seconds of the hailstorm ability" )
    public static Double hailstormCooldown = 5.0;

    @ConfigRange( min = 0.05, max = 10000 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "hailstorm"}, key = "hailstormCasttime", comment = "The cast time in seconds of the hailstorm ability" )
    public static Double hailstormCasttime = 0.5;

    @ConfigRange( min = 1.0, max = 10000.0 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "hailstorm"}, key = "hailstormDuration", comment = "How long affected targets will be frozen solid" )
    public static Double hailstormDuration = 2.0;

    @ConfigRange( min = 0, max = 100 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "hailstorm"}, key = "hailstormManaCost", comment = "The mana cost for using the hailstorm ability" )
    public static Integer hailstormManaCost = 1;

    @ConfigRange( min = 0, max = 10000 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "hailstorm"}, key = "hailstormDamage", comment = "How much damage enemies take when you land.  Increased by falling further, and fully frozen targets take triple damage." )
    public static Double hailstormDamage = 0.5;

    @ConfigRange( min = 0, max = 10000 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "hailstorm"}, key = "hailstormLevelRange", comment = "How much range is added per level.  Increased depending on fall distance." )
    public static Double hailstormLevelRange = 0.5;

    @ConfigRange( min = 0, max = 10000 )
    @ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "frost_dragon", "actives", "hailstorm"}, key = "hailstormBaseRange", comment = "How much range the ability has, excluding level.  Increased depending on fall distance." )
    public static Double hailstormBaseRange = 1.0;

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || !hailStorm;
    }

    public boolean canCastSkill(Player player) {
        return super.canCastSkill(player) && !player.level.getFluidState(player.blockPosition()).is(Fluids.WATER);
    }

    @Override
    public int getManaCost() {
        return hailstormManaCost;
    }

    @Override
    public Integer[] getRequiredLevels(){
        return new Integer[]{0, 10, 30, 50};
    }

    @Override
    public int getSkillCooldown() {
        return Functions.secondsToTicks(hailstormCooldown);
    }

    @Override
    public String getName() {
        return "hail_storm";
    }

    public boolean canLandOnFluid(FluidState fluidState) {
        return fluidState.is(Fluids.WATER) || fluidState.is(Fluids.FLOWING_WATER);
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.FROST;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/hail_storm_0.png"),
                new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/hail_storm_1.png"),
                new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/hail_storm_2.png"),
                new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/hail_storm_3.png"),
                new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/frost/hail_storm_4.png")};
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMinLevel() {
        return 0;
    }

    @Override
    public int getSkillCastingTime() {
        return Functions.secondsToTicks(hailstormCasttime);
    }

    @Override
    public void onCasting(Player player, int currentCastTime) {
        makeParticles(player, 0, false);
    }

    @Override
    public void castingComplete(Player player) {
        super.castingComplete(player);
    }

    @Override
    public int getSortOrder() {
        return 2;
    }

    @Override
    public float getRange() {
        return (float) (hailstormBaseRange + (hailstormLevelRange * getLevel()));
    }

    public float getDamage() {
        return (float) (hailstormDamage * getLevel());
    }

    public int getDuration() {
        return Functions.secondsToTicks(hailstormDuration) * getLevel();
    }

    public void continueDive(Player player, float distance) {
        float r = getRange() * rangeBonusFromDistance(player.fallDistance);
        if (player.level.isClientSide()) {
            makeParticles(player, 0, false);
        } else {
            for (int x = (int) -r; x < (int) r; x++) {
                for (int z = (int) -r; z < (int) r; z++) {
                    if (player.distanceToSqr(new Vec3(player.getX() + x, player.getY(), player.getZ() + z)) < r * r) {
                        for (int y = 0; y < 4; y++) {
                            BlockPos blockPos = new BlockPos(player.getX() + x, player.getY(), player.getZ() + z);
                            if (player.level.getFluidState(blockPos.below(y)).is(Fluids.WATER) && player.level.getBlockState(blockPos.below(y)).getMaterial().isLiquid()) {
                                player.level.setBlockAndUpdate(blockPos.below(y), Blocks.FROSTED_ICE.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void finishDive(Player player, float distance) {
        super.finishDive(player, distance);
        if (player.level.isClientSide()) {
            player.level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f, false);
            player.level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 0.9f, false);
            makeParticles(player, distance, true);
        } else {
            float range = getRange() * rangeBonusFromDistance(distance);
            List<LivingEntity> list1 = player.level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range));
            if(!list1.isEmpty())
                for(LivingEntity livingEntity : list1){
                    if (livingEntity.equals(player))
                        continue;
                    double d0 = livingEntity.getX() - player.getX();
                    double d1 = livingEntity.getY() - player.getY();
                    double d2 = livingEntity.getZ() - player.getZ();
                    double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                    if(d3 <= (double)(range * range)){
                        livingEntity.addEffect(new MobEffectInstance(DragonEffects.FROSTED, getDuration()));
                        livingEntity.addEffect(new MobEffectInstance(DragonEffects.BRITTLE, getDuration()));
                        if (livingEntity.canFreeze()) {
                            if (livingEntity.isFullyFrozen() && livingEntity.hasEffect(DragonEffects.FROSTED)) {
                                livingEntity.hurt(new EntityDamageSource("freeze", player), getDamage() * 3 * getLevel() * damageBonusFromDistance(distance));
                            } else {
                                livingEntity.hurt(new EntityDamageSource("freeze", player), getDamage() * getLevel() * damageBonusFromDistance(distance));
                                livingEntity.setTicksFrozen(livingEntity.getTicksRequiredToFreeze() + 2);
                            }
                        }
                    }
                }

            // Make fish
            /*float f5 = (float)Math.PI * getRange() * getRange() * rangeBonusFromDistance(distance);

            for(int i = 0; i < 1; i++)
                for(int k1 = 0; (float)k1 < f5; ++k1){
                    ItemStack pStack = new ItemStack(Items.FISHING_ROD);
                    LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel)player.level)).withParameter(LootContextParams.ORIGIN, player.position()).withParameter(LootContextParams.TOOL, pStack).withParameter(LootContextParams.THIS_ENTITY, player).withRandom(player.getRandom()).withLuck((float)0 + player.getLuck());
                    lootcontext$builder.withParameter(LootContextParams.KILLER_ENTITY, player).withParameter(LootContextParams.TOOL, pStack);
                    LootTable loottable = player.level.getServer().getLootTables().get(BuiltInLootTables.FISHING_FISH);
                    List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(LootContextParamSets.FISHING));

                    float f6 = player.getRandom().nextFloat() * ((float)Math.PI * 2F);

                    float f7 = Mth.sqrt(player.getRandom().nextFloat()) * getRange() * rangeBonusFromDistance(distance);
                    float f8 = Mth.cos(f6) * f7;
                    float f9 = Mth.sin(f6) * f7;

                    for (ItemStack itemstack : list) {
                        ItemEntity itementity = new ItemEntity(player.level, player.getX() + f8, player.getY(), player.getZ() + f9, itemstack);
                        player.level.addFreshEntity(itementity);
                    }
                }*/
        }
    }

    @Override
    public ParticleOptions getParticle() {
        return DSParticles.snowflake;
    }
}
