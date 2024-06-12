package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.api.DragonFood;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.data.DataDamageTypeTagsProvider;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( LivingEntity.class )
public abstract class MixinLivingEntity extends Entity{
	@Shadow public abstract ItemStack getMainHandItem();
	@Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);
	@Shadow protected ItemStack useItem;

	@Shadow public abstract void knockback(double pStrength, double pX, double pZ);

	public MixinLivingEntity(EntityType<?> p_i48580_1_, Level p_i48580_2_){
		super(p_i48580_1_, p_i48580_2_);
	}

	@Redirect( method = "collectEquipmentChanges", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;" ) )
	private ItemStack grantDragonSwordAttributes(LivingEntity entity, EquipmentSlot slotType){
		if (slotType == EquipmentSlot.MAINHAND) {
			if ((LivingEntity)(Object)this instanceof Player player) {
				if(DragonUtils.isDragon(player)) {
					DragonStateHandler cap = DragonUtils.getHandler(entity);
					if (ToolUtils.shouldUseDragonTools(getMainHandItem())) {
						// Without this the item in the dragon slot for the sword would not grant any of its attributes
						ItemStack sword = cap.getClawToolData().getClawsInventory().getItem(0);

						if (!sword.isEmpty()) {
							return sword;
						}
					}
				}
			}
		}

		return getItemBySlot(slotType);
	}

	@ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
	private boolean replaceIsEdibleInEat(boolean original, @Local(argsOnly = true) ItemStack itemStack){
		if(DragonUtils.isDragon((LivingEntity)(Object)this))
		{
			return DragonFood.isEdible(itemStack.getItem(), (LivingEntity)(Object)this);
		}

		return original;
	}

	@ModifyExpressionValue(method = "addEatEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
	private boolean replaceIsEdibleInAddEatEffect(boolean original, @Local(argsOnly = true) ItemStack itemStack){
		if(DragonUtils.isDragon((LivingEntity)(Object)this))
		{
			return DragonFood.isEdible(itemStack.getItem(), (LivingEntity)(Object)this);
		}

		return original;
	}

	// TODO: Figure out how to inject into IForgeItemStack instead to just override getFoodProperties
	@ModifyExpressionValue(method = "addEatEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getFoodProperties(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/food/FoodProperties;"))
	private FoodProperties replaceFoodPropertiesInAddEatEffect(FoodProperties original, ItemStack pFood, Level pLevel, LivingEntity pLivingEntity){
		if (DragonUtils.isDragon((LivingEntity) (Object) this)) {
			return DragonFood.getEffectiveFoodProperties(useItem.getItem(), (LivingEntity) (Object) this);
		}

		return original;
	}

	@Unique
	private int dragon_Survival$getHumanOrDragonUseDuration(int original){
		if ((LivingEntity)(Object)this instanceof Player player) {
			DragonStateHandler handler = DragonStateProvider.getHandler(player);
			if (handler != null && handler.isDragon()) {
				return DragonFoodHandler.getUseDuration(useItem, handler.getType());
			}
		}

		return original;
	}

	@ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
	private int replaceUseDurationInShouldTriggerItemUseEffects(int original){
		return dragon_Survival$getHumanOrDragonUseDuration(original);
	}

	// TODO: Figure out how to inject into IForgeItemStack instead to just override getFoodProperties
	@ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getFoodProperties(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/food/FoodProperties;"))
	private FoodProperties replaceGetFoodPropertiesInShouldTriggerItemUseEffects(FoodProperties original){
		if (DragonUtils.isDragon((LivingEntity) (Object) this)) {
			return DragonFood.getEffectiveFoodProperties(useItem.getItem(), (LivingEntity) (Object) this);
		}

		return original;
	}

	@ModifyExpressionValue(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
	private int replaceUseDurationInSyncedDataUpdated(int original){
		return dragon_Survival$getHumanOrDragonUseDuration(original);
	}

	@WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void disableKnockbackForMagic(LivingEntity instance, double pX, double pZ, double pStrength, Operation<Void> original, @Local(argsOnly = true) final DamageSource damageSource) {
		if (damageSource.is(DataDamageTypeTagsProvider.NO_KNOCKBACK)) {
			this.knockback(0.0D, pX, pZ);
		} else {
			original.call(instance, pX, pZ, pStrength);
		}
	}

	@Unique
	private UseAnim dragon_Survival$getUseAnimation(UseAnim original, ItemStack pStack){
		if(DragonUtils.isDragon((LivingEntity)(Object)this)) {
			return DragonFood.isEdible(pStack.getItem(), (LivingEntity)(Object)this) ? UseAnim.EAT : original;
		}

		return original;
	}

	// TODO: Possible to combine these into one statement?
	@ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;", ordinal = 0))
	private UseAnim replaceGetUseAnimationInTriggerItemUseEffects0(UseAnim original, ItemStack pStack, int pAmount){
		return dragon_Survival$getUseAnimation(original, pStack);
	}

	@ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;", ordinal = 1))
	private UseAnim replaceGetUseAnimationInTriggerItemUseEffects1(UseAnim original, ItemStack pStack, int pAmount){
		return dragon_Survival$getUseAnimation(original, pStack);
	}
}