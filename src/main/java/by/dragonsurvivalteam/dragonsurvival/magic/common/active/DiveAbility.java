package by.dragonsurvivalteam.dragonsurvival.magic.common.active;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public abstract class DiveAbility extends ChargeCastAbility {
    @Override
    public void castingComplete(Player player) {
        DragonStateProvider.getData(player).setWingsSpread(false);
        DragonStateProvider.getData(player).setIsDiving(true);
        player.resetFallDistance();
    }

    @Override
    public boolean requiresStationaryCasting(){
        return false;
    }

    public abstract float getRange();

    public void continueDive(Player player, float distance) { }

    public void finishDive(Player player, float distance) {
        DragonStateProvider.getData(player).setWingsSpread(false);
        DragonStateProvider.getData(player).setIsDiving(false);
    }

    public abstract ParticleOptions getParticle();

    public float rangeBonusFromDistance(float distance) {
        return 1 + Mth.clamp((float) Math.log(Math.max(distance + 1, 0.0f)), 0.0f, 3.0f);
    }

    public float damageBonusFromDistance(float distance) {
        return 1 + Mth.clamp((float) Math.log(Math.max(distance + 1, 0.0f)), 0.0f, 3.0f);
    }

    public void makeParticles(Player player, float distance, boolean complete) {
        float f5 = (float)Math.PI * getRange() * getRange() * rangeBonusFromDistance(distance);

        for(int i = 0; i < (complete ? 5 : 1); i++)
            for(int k1 = 0; (float)k1 < f5; ++k1){
                float f6 = player.getRandom().nextFloat() * ((float)Math.PI * 2F);

                float f7 = Mth.sqrt(player.getRandom().nextFloat()) * getRange() * rangeBonusFromDistance(distance);
                float f8 = Mth.cos(f6) * f7;
                float f9 = Mth.sin(f6) * f7;
                player.level().addAlwaysVisibleParticle(getParticle(), player.getX() + f8, player.getY(), player.getZ() + f9, ((player.getRandom().nextFloat() * 0.5) - 0.5) * 0.01f, distance * 0.01f, ((player.getRandom().nextFloat() * 0.5) - 0.5) * 0.01f);
            }
    }

    @Override
    public boolean canCastSkill(Player player) {
        if (player.onGround()) {
            MagicHUD.castingError(Component.translatable("ds.skill.must_fly_failure"));
            return false;
        }

        if(player.isCreative())
            return true;

        //DragonStateHandler handler = DragonUtils.getHandler(player);

        if(!canConsumeMana(player)){
            MagicHUD.castingError(Component.translatable("ds.skill_mana_check_failure"));
            return false;
        }

        if(getCurrentCooldown() != 0){
            MagicHUD.castingError(Component.translatable("ds.skill_cooldown_check_failure", nf.format(getCurrentCooldown() / 20F) + "s").withStyle(ChatFormatting.RED));
            return false;
        }

        return !player.isSpectator();
    }
}
