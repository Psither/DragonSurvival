package by.dragonsurvivalteam.dragonsurvival.magic.abilities.VoltaicDragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.AoeBuffAbility;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@RegisterDragonAbility
public class DragonsGraceAbility extends AoeBuffAbility {
    @Override
    public int getRange() {
        return 5;
    }

    @Override
    public ParticleOptions getParticleEffect() {
        return ParticleTypes.WAX_ON;
    }

    @Override
    public MobEffectInstance getEffect() {
        return new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0);
    }

    @Override
    public int getSkillCastingTime() {
        return 1;
    }

    @Override
    public int getManaCost() {
        return 1;
    }

    @Override
    public Integer[] getRequiredLevels(){
        return new Integer[]{0, 25, 40, 60};
    }

    @Override
    public int getSkillCooldown() {
        return 60;
    }

    @Override
    public String getName() {
        return "dragons_grace";
    }

    @Override
    public AbstractDragonType getDragonType() {
        return DragonTypes.VOLTAIC;
    }

    @Override
    public ResourceLocation[] getSkillTextures() {
        return new ResourceLocation[] {
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/voltaic/dragons_grace_0.png"),
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/voltaic/dragons_grace_1.png"),
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/voltaic/dragons_grace_2.png"),
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/voltaic/dragons_grace_3.png"),
                ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/skills/voltaic/dragons_grace_4.png"),
        };
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMinLevel() {
        return 0;
    }
}
