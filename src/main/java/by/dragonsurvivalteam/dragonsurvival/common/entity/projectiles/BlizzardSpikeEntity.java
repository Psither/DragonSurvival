package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BlizzardSpikeEntity extends DragonSpikeEntity {

    public BlizzardSpikeEntity(Level p_i50172_2_){
        super(DSEntities.BLIZZARD_SPIKE.get(), p_i50172_2_);
    }

    public BlizzardSpikeEntity(EntityType<? extends AbstractArrow> type, Level worldIn){
        super(type, worldIn);
    }

    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent(){
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }
}
