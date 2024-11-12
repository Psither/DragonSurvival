package by.dragonsurvivalteam.dragonsurvival.common.items;


import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WingGrantItem extends Item {
    public WingGrantItem(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand p_77659_3_) {
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.isDragon()) {
            if (!world.isClientSide) {
                handler.setHasFlight(!handler.hasFlight());
                PacketDistributor.sendToAllPlayers(new SyncDragonHandler.Data(player.getId(), handler.isDiving(), handler.getType(), handler.getBody(), handler.getSize(), handler.hasFlight(), handler.getPassengerId()));

                if (!player.isCreative()) {
                    player.getItemInHand(p_77659_3_).shrink(1);
                }
            }

            player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1F, 0F);
            return InteractionResultHolder.success(player.getItemInHand(p_77659_3_));
        }

        return super.use(world, player, p_77659_3_);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        pTooltipComponents.add(Component.translatable("ds.description.wing_grant"));
    }
}