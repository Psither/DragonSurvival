package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonHandler;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class WingGrantItem extends TooltipItem {
    public WingGrantItem(final Properties properties, final String key) {
        super(properties, key);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.isDragon()) {
            if (!level.isClientSide()) {
                handler.setHasFlight(!handler.hasFlight());
                PacketDistributor.sendToAllPlayers(new SyncDragonHandler.Data(handler.getTypeName(), handler.getBody(), handler.getSize(), player.getId(), handler.getPassengerId(), handler.hasFlight(), handler.isHiding()));

                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }
            }

            player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1F, 0F);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        return super.use(level, player, hand);
    }
}