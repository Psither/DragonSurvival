package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.MagicAbility;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicStats;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber
public class ManaHandler{
	@SubscribeEvent
	public static void playerTick(PlayerTickEvent event){
		if(event.phase == Phase.START){
			return;
		}

		Player player = event.player;

		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(cap.getMagicData().getCurrentlyCasting() != null){
				return;
			}

			boolean goodConditions = ManaHandler.isPlayerInGoodConditions(player);

			int timeToRecover = goodConditions ? ServerConfig.favorableManaTicks : ServerConfig.normalManaTicks;

			if(player.hasEffect(DragonEffects.SOURCE_OF_MAGIC)){
				timeToRecover = 1;
			}

			if(player.tickCount % Functions.secondsToTicks(timeToRecover) == 0){
				if(cap.getMagicData().getCurrentMana() < getMaxMana(player)){
					replenishMana(player, 1);
				}
			}
		});
	}

	public static boolean isPlayerInGoodConditions(Player player){
		if(!DragonUtils.isDragon(player)){
			return false;
		}

		BlockState blockBelow = player.level().getBlockState(player.blockPosition().below());
		BlockState feetBlock = player.getFeetBlockState();

		if(feetBlock.getBlock() instanceof TreasureBlock || blockBelow.getBlock() instanceof TreasureBlock){
			return true;
		}

		if(player.hasEffect(DragonEffects.SOURCE_OF_MAGIC)){
			return true;
		}

		return DragonStateProvider.getCap(player).map(cap -> {
			if(DragonConfigHandler.DRAGON_MANA_BLOCKS != null && DragonConfigHandler.DRAGON_MANA_BLOCKS.containsKey(cap.getTypeName())){
				if(DragonConfigHandler.DRAGON_MANA_BLOCKS.get(cap.getTypeName()).contains(blockBelow.getBlock()) || DragonConfigHandler.DRAGON_MANA_BLOCKS.get(cap.getTypeName()).contains(feetBlock.getBlock())){
					if(!(blockBelow.getBlock() instanceof AbstractFurnaceBlock) && !(feetBlock.getBlock() instanceof AbstractFurnaceBlock) && !(blockBelow.getBlock() instanceof AbstractCauldronBlock) && !(feetBlock.getBlock() instanceof AbstractCauldronBlock)){
						return true;
					}
				}
			}

			return cap.getType().isInManaCondition(player, cap);
		}).orElse(false);
	}

	public static int getMaxMana(Player entity){
		int mana = 1 + (ServerConfig.noEXPRequirements ? 9 : Math.max(0, (Math.min(50, entity.experienceLevel) - 5) / 5) + (DragonAbilities.getSelfAbility(entity, MagicAbility.class) != null ? DragonAbilities.getSelfAbility(entity, MagicAbility.class).getMana() : 0));
		if (DragonUtils.getDragonBody(entity) != null)
			mana += DragonUtils.getDragonBody(entity).getManaBonus();
		return Math.max(mana, 0);
	}
	public static boolean canConsumeMana(Player player, int manaCost)
	{
		manaCost -= ManaHandler.getCurrentMana(player);
		if (ServerConfig.consumeEXPAsMana)
			manaCost -= player.totalExperience / 10;
		return manaCost <= 0;
	}
	public static void replenishMana(Player entity, int mana){
		if(entity.level().isClientSide()){
			return;
		}

		DragonStateProvider.getCap(entity).ifPresent(cap -> {
			cap.getMagicData().setCurrentMana(Math.min(getMaxMana(entity), cap.getMagicData().getCurrentMana() + mana));
			NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)entity), new SyncMagicStats(entity.getId(), cap.getMagicData().getSelectedAbilitySlot(), cap.getMagicData().getCurrentMana(), cap.getMagicData().shouldRenderAbilities()));
		});
	}

	public static void consumeMana(Player entity, int mana){
		if(entity == null || entity.isCreative() || entity.hasEffect(DragonEffects.SOURCE_OF_MAGIC))
			return;

		if(ServerConfig.consumeEXPAsMana){
			if(entity.level().isClientSide()){
				if(getCurrentMana(entity) < mana && (getCurrentMana(entity) + entity.totalExperience / 10 >= mana || entity.experienceLevel > 0)){
					entity.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.01F, 0.01F);
				}
			}
		}

		if(entity.level().isClientSide()){
			return;
		}

		DragonStateProvider.getCap(entity).ifPresent(cap -> {
			if(ServerConfig.consumeEXPAsMana){
				if(getCurrentMana(entity) < mana && (getCurrentMana(entity) + entity.totalExperience / 10 >= mana || entity.experienceLevel > 0)){
					int missingMana = mana - getCurrentMana(entity);
					int missingExp = missingMana * 10;
					entity.giveExperiencePoints(-missingExp);
					cap.getMagicData().setCurrentMana(0);
				}else{
					cap.getMagicData().setCurrentMana(Math.max(0, cap.getMagicData().getCurrentMana() - mana));
				}
			}else{
				cap.getMagicData().setCurrentMana(Math.max(0, cap.getMagicData().getCurrentMana() - mana));
			}

			NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)entity), new SyncMagicStats(entity.getId(), cap.getMagicData().getSelectedAbilitySlot(), cap.getMagicData().getCurrentMana(), cap.getMagicData().shouldRenderAbilities()));
		});
	}

	public static int getCurrentMana(Player entity){
		return DragonStateProvider.getCap(entity).map(cap -> Math.min(cap.getMagicData().getCurrentMana(), getMaxMana(entity))).orElse(0);
	}
}