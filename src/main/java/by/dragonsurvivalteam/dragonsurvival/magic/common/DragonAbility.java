package by.dragonsurvivalteam.dragonsurvival.magic.common;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.CaveDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.ForestDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.SeaDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.text.NumberFormat;
import java.util.ArrayList;

public abstract class DragonAbility {
    protected static NumberFormat nf = NumberFormat.getInstance();

    public Player player;
    public int level;

    static {
        nf.setMaximumFractionDigits(1);
    }

    public void onKeyPressed(Player player, Runnable onFinish, long castStartTime, long clientTime) {
    }

    public void onKeyReleased(Player player) {
    }

    public Player getPlayer() {
        return player;
    }

    public Component getTitle() {
        return Component.translatable(Translation.Type.ABILITY.wrap(getName()));
    }

    public Component getDescription() {
        return Component.translatable(Translation.Type.ABILITY_DESCRIPTION.wrap(getName()));
    }

    public abstract String getName();

    public abstract AbstractDragonType getDragonType();

    public abstract ResourceLocation[] getSkillTextures();

    public ResourceLocation getIcon() {
        return getSkillTextures()[Mth.clamp(getLevel(), 0, getSkillTextures().length - 1)];
    }

    public int getSortOrder() {
        return 0;
    }

    public ArrayList<Component> getInfo() {
        return new ArrayList<>();
    }

    public ArrayList<Component> getLevelUpInfo() {
        return new ArrayList<>();
    }

    public CompoundTag saveNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("level", level);
        return nbt;
    }

    public void loadNBT(CompoundTag nbt) {
        level = nbt.getInt("level");
    }

    public abstract int getMaxLevel();

    public abstract int getMinLevel();

    public boolean isDisabled(){
        if(!ServerConfig.dragonAbilities){
            return true;
        }
        if(DragonUtils.isType(getDragonType(), DragonTypes.CAVE) && !CaveDragonConfig.caveDragonAbilities){
            return true;
        }
        if(DragonUtils.isType(getDragonType(), DragonTypes.SEA) && !SeaDragonConfig.seaDragonAbilities){
            return true;
        }
        return DragonUtils.isType(getDragonType(), DragonTypes.FOREST) && !ForestDragonConfig.areAbilitiesEnabled;
    }

    public int getLevel() {
        if (isDisabled())
            return 0;

        return level;
    }

    public void setLevel(int level) {
        this.level = Mth.clamp(level, getMinLevel(), getMaxLevel());
    }

    public MutableComponent description() {
        return Component.empty();
    }
}