package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin( EnchantmentHelper.class )
public abstract class MixinEnchantmentHelper{

	@ModifyReturnValue(method = "hasAquaAffinity", at = @At("RETURN"))
	private static boolean modifyHasAquaAffinityForSeaDragon(boolean original, @Local(index = 0, argsOnly = true) LivingEntity pEntity){
		if(!(pEntity instanceof Player player)){
			return original;
		}

		if(DragonUtils.isDragonType(player, DragonTypes.SEA)){
			return true;
		}

		return original;
	}

	// FIXME: I'm 99% sure this code doesn't do anything. I disabled it for now after doing a fair bit of testing. Probably retest some more to make sure it does nothing, though.
	/*@Unique
	private static final List<EnchantmentCategory> IGNORED_CATEGORIES = List.of(
		    EnchantmentCategory.ARMOR,
		    EnchantmentCategory.ARMOR_HEAD,
		    EnchantmentCategory.ARMOR_CHEST,
		    EnchantmentCategory.ARMOR_LEGS,
		    EnchantmentCategory.ARMOR_FEET,
            // Doesn't make sense in the dragon sword slot
		    EnchantmentCategory.BOW,
		    EnchantmentCategory.CROSSBOW,
		    EnchantmentCategory.FISHING_ROD
			// Maybe in the future?
//		    EnchantmentCategory.TRIDENT,
	);

	@Unique
	private static final List<String> IGNORED_CATEGORY_NAMES = List.of(
			"bows",
			"horse_armor"
	);*/

	/*@ModifyExpressionValue(method = "getEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/entity/LivingEntity;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)I"))
	private static int modifyGetEnchantmentLevelForDragonTools(int original, Enchantment enchantment, LivingEntity pItemStack, @Local(argsOnly = true) LivingEntity entity){
		if(!(entity instanceof Player player)){
			return original;
		}

		if(IGNORED_CATEGORIES.contains(enchantment.category)){
			return original;
		}

		if(IGNORED_CATEGORY_NAMES.contains(enchantment.category.name())){
			return original;
		}

		if(!ToolUtils.shouldUseDragonTools(player.getMainHandItem())){
			return original;
		}

		if(DragonUtils.isDragon(player)){
			ItemStack stack = ClawToolHandler.getDragonHarvestTool(player);

			if(stack == player.getMainHandItem()){
				// No relevant tool found - get the sword
				stack = ClawToolHandler.getDragonSword(player);
			}

			if(stack != ItemStack.EMPTY){
				int enchantmentLevel = stack.getEnchantmentLevel(enchantment);

				// Just to be safe
				if(enchantmentLevel > 0){
					return enchantmentLevel;
				}
			}
		}

		return original;
	}*/
}