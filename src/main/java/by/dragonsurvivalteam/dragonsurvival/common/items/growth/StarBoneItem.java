package by.dragonsurvivalteam.dragonsurvival.common.items.growth;


import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncSize;
import by.dragonsurvivalteam.dragonsurvival.network.player.SynchronizeDragonCap;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class StarBoneItem extends Item{
	public StarBoneItem(Properties p_i48487_1_){
		super(p_i48487_1_);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn){
		DragonStateHandler handler = DragonStateProvider.getHandler(playerIn);

		if (handler != null && handler.isDragon()) {
			double size = handler.getSize();
			if (size > 14) {
				size -= 2;
				size = Math.max(size, DragonLevel.NEWBORN.size);
				handler.setSize(size, playerIn);

				if (!playerIn.isCreative()) {
					playerIn.getItemInHand(handIn).shrink(1);
				}

				if (!worldIn.isClientSide) {
					NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> playerIn), new SyncSize(playerIn.getId(), size));
					if (handler.getPassengerId() != 0) {
						Entity mount = worldIn.getEntity(handler.getPassengerId());
						if (mount != null) {
							mount.stopRiding();
							((ServerPlayer) playerIn).connection.send(new ClientboundSetPassengersPacket(playerIn));
							NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn), new SynchronizeDragonCap(playerIn.getId(), handler.isDiving(), handler.getType(), handler.getBody(), handler.getSize(), handler.hasFlight(), 0));
						}
					}
				}

				playerIn.refreshDimensions();
				return InteractionResultHolder.consume(playerIn.getItemInHand(handIn));
			}
		}

		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public void appendHoverText(ItemStack p_77624_1_,
		@Nullable
			Level p_77624_2_, List<Component> p_77624_3_, TooltipFlag p_77624_4_){
		super.appendHoverText(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
		p_77624_3_.add(Component.translatable("ds.description.starBone"));
	}
}