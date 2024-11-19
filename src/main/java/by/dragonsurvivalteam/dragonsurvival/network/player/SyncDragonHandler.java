package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class SyncDragonHandler implements IMessage<SyncDragonHandler.Data> {
    public static void handleClient(final Data message, final IPayloadContext context) {
        context.enqueueWork(() -> ClientProxy.handleSynchronizeDragonCap(message));
    }

    public static void handleServer(final Data message, final IPayloadContext context) {
        Player sender = context.player();
        context.enqueueWork(() -> {
            DragonStateProvider.getOptional(sender).ifPresent(handler -> {
                AbstractDragonType newType = DragonTypes.getStaticSubtype(message.dragonType());

                if (newType == null && handler.getType() != null) {
                    DragonCommand.reInsertClawTools(sender, handler);
                }

                handler.setType(newType, sender);
                handler.setBody(message.dragonBody());
                handler.setSize(message.size(), sender);
                handler.setPassengerId(message.passengerId());
                handler.setHasFlight(message.hasWings());
                handler.setIsHiding(message.hiding());
            });
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), message));
    }

    public record Data(@NotNull String dragonType, @NotNull Holder<DragonBody> dragonBody, double size, int playerId, int passengerId, boolean hasWings, boolean hiding) implements CustomPacketPayload {
        public static final Type<Data> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_dragon_handler"));

        public static StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = NeoForgeStreamCodecs.composite(
                ByteBufCodecs.STRING_UTF8, Data::dragonType,
                DragonBody.STREAM_CODEC, Data::dragonBody,
                ByteBufCodecs.DOUBLE, Data::size,
                ByteBufCodecs.INT, Data::playerId,
                ByteBufCodecs.INT, Data::passengerId,
                ByteBufCodecs.BOOL, Data::hasWings,
                ByteBufCodecs.BOOL, Data::hiding,
                Data::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}