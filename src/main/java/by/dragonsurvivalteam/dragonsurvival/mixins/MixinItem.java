package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.api.DragonFood;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin( Item.class )
public class MixinItem{
	@ModifyExpressionValue (method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
	private boolean replaceIsEdibleInUse(boolean original, Level level, Player player, InteractionHand hand){
		if(DragonUtils.isDragon(player))
		{
			return DragonFood.isEdible(player.getItemInHand(hand).getItem(), player);
		}

		return original;
	}

	// TODO: Figure out how to inject into IForgeItemStack instead to just override getFoodProperties
	@ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getFoodProperties(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/food/FoodProperties;"))
	private FoodProperties replaceFoodPropertiesInUse(FoodProperties original, Level pLevel, Player pPlayer, InteractionHand pUsedHand, @Local ItemStack itemStack){
		if(DragonUtils.isDragon(pPlayer))
		{
			return DragonFood.getEffectiveFoodProperties(itemStack.getItem(), pPlayer);
		}

		return original;
	}

	@ModifyExpressionValue (method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
	private boolean replaceIsEdibleInFinishUsingItem(boolean original, ItemStack item, Level level, LivingEntity entity){
		if(DragonUtils.isDragon(entity))
		{
			return DragonFood.isEdible(item.getItem(), entity);
		}

		return original;
	}
}