<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/common/magic/abilities/Actives/BuffAbilities/EyesBuffAbility.java
package by.jackraidenph.dragonsurvival.common.magic.abilities.Actives.BuffAbilities;

import by.jackraidenph.dragonsurvival.client.handlers.KeyInputHandler;
import by.jackraidenph.dragonsurvival.common.DragonEffects;
import by.jackraidenph.dragonsurvival.common.magic.common.AbilityAnimation;
import by.jackraidenph.dragonsurvival.common.magic.common.ActiveDragonAbility;
import by.jackraidenph.dragonsurvival.config.ConfigHandler;
import by.jackraidenph.dragonsurvival.misc.DragonType;
import by.jackraidenph.dragonsurvival.util.Functions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
=======
package by.dragonsurvivalteam.dragonsurvival.common.magic.abilities.Actives.BuffAbilities;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.KeyInputHandler;
import by.dragonsurvivalteam.dragonsurvival.common.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.common.magic.common.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.magic.common.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonType;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/common/magic/abilities/Actives/BuffAbilities/EyesBuffAbility.java
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Locale;

<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/common/magic/abilities/Actives/BuffAbilities/EyesBuffAbility.java
public class EyesBuffAbility extends ActiveDragonAbility
{
	private MobEffect effect;
	public EyesBuffAbility(DragonType type, MobEffect effect, String name, String icon, int minLevel, int maxLevel, int manaCost, int castTime, int cooldown, Integer[] requiredLevels)
	{
=======
public class EyesBuffAbility extends ActiveDragonAbility{
	private final Effect effect;

	public EyesBuffAbility(DragonType type, Effect effect, String name, String icon, int minLevel, int maxLevel, int manaCost, int castTime, int cooldown, Integer[] requiredLevels){
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/common/magic/abilities/Actives/BuffAbilities/EyesBuffAbility.java
		super(type, name, icon, minLevel, maxLevel, manaCost, castTime, cooldown, requiredLevels);
		this.effect = effect;
	}

	@Override
	public EyesBuffAbility createInstance(){
		return new EyesBuffAbility(type, effect, id, icon, minLevel, maxLevel, manaCost, castTime, abilityCooldown, requiredLevels);
	}
<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/common/magic/abilities/Actives/BuffAbilities/EyesBuffAbility.java
	
	public int getDuration(){
		return (effect == DragonEffects.LAVA_VISION ? ConfigHandler.SERVER.lavaVisionDuration.get() : ConfigHandler.SERVER.seaEyesDuration.get()) * getLevel();
	}
	
	
	
	@Override
	public void onActivation(Player player)
	{
		super.onActivation(player);
		player.addEffect(new MobEffectInstance(effect, Functions.secondsToTicks(getDuration())));
		
		player.level.playLocalSound(player.position().x, player.position().y + 0.5, player.position().z, SoundEvents.UI_TOAST_IN, SoundSource.PLAYERS, 5F, 0.1F, false);
	}
	
	@Override
	public boolean isDisabled()
	{
		if(effect == DragonEffects.LAVA_VISION && !ConfigHandler.SERVER.lavaVision.get()) return true;
		if(effect == DragonEffects.WATER_VISION && !ConfigHandler.SERVER.seaEyes.get()) return true;
		return super.isDisabled();
	}
	
	@Override
	public ArrayList<Component> getInfo()
	{
		ArrayList<Component> components = super.getInfo();
		components.add(new TranslatableComponent("ds.skill.duration.seconds", getDuration()));
		
		if(!KeyInputHandler.ABILITY4.isUnbound()) {
=======

	@Override
	public ArrayList<ITextComponent> getInfo(){
		ArrayList<ITextComponent> components = super.getInfo();
		components.add(new TranslationTextComponent("ds.skill.duration.seconds", getDuration()));

		if(!KeyInputHandler.ABILITY4.isUnbound()){
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/common/magic/abilities/Actives/BuffAbilities/EyesBuffAbility.java
			String key = KeyInputHandler.ABILITY4.getKey().getDisplayName().getContents().toUpperCase(Locale.ROOT);

			if(key.isEmpty()){
				key = KeyInputHandler.ABILITY4.getKey().getDisplayName().getString();
			}
			components.add(new TranslatableComponent("ds.skill.keybind", key));
		}

		return components;
	}
<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/common/magic/abilities/Actives/BuffAbilities/EyesBuffAbility.java
	
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(new TranslatableComponent("ds.skill.duration.seconds", "+" + (effect == DragonEffects.LAVA_VISION ? ConfigHandler.SERVER.lavaVisionDuration.get() : ConfigHandler.SERVER.seaEyesDuration.get())));
		return list;
	}
	
	@Override
	public Component getDescription()
	{
		return new TranslatableComponent("ds.skill.description." + getId(), getDuration());
=======

	@Override
	public void onActivation(PlayerEntity player){
		super.onActivation(player);
		player.addEffect(new EffectInstance(effect, Functions.secondsToTicks(getDuration())));

		player.level.playLocalSound(player.position().x, player.position().y + 0.5, player.position().z, SoundEvents.UI_TOAST_IN, SoundCategory.PLAYERS, 5F, 0.1F, false);
	}

	public int getDuration(){
		return (effect == DragonEffects.LAVA_VISION ? ConfigHandler.SERVER.lavaVisionDuration.get() : ConfigHandler.SERVER.seaEyesDuration.get()) * getLevel();
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/common/magic/abilities/Actives/BuffAbilities/EyesBuffAbility.java
	}

	public boolean canMoveWhileCasting(){return false;}

	@Override
	public AbilityAnimation getLoopingAnimation(){
		return new AbilityAnimation("cast_self_buff", true, false);
	}

	@Override
	public AbilityAnimation getStoppingAnimation(){
		return new AbilityAnimation("self_buff", 0.52 * 20, true, false);
	}

	@Override
	public IFormattableTextComponent getDescription(){
		return new TranslationTextComponent("ds.skill.description." + getId(), getDuration());
	}

	@OnlyIn( Dist.CLIENT )
	public ArrayList<ITextComponent> getLevelUpInfo(){
		ArrayList<ITextComponent> list = super.getLevelUpInfo();
		list.add(new TranslationTextComponent("ds.skill.duration.seconds", "+" + (effect == DragonEffects.LAVA_VISION ? ConfigHandler.SERVER.lavaVisionDuration.get() : ConfigHandler.SERVER.seaEyesDuration.get())));
		return list;
	}

	@Override
	public boolean isDisabled(){
		if(effect == DragonEffects.LAVA_VISION && !ConfigHandler.SERVER.lavaVision.get()){
			return true;
		}
		if(effect == DragonEffects.WATER_VISION && !ConfigHandler.SERVER.seaEyes.get()){
			return true;
		}
		return super.isDisabled();
	}
}