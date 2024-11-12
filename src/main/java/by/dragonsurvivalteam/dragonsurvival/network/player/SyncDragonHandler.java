package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class SyncDragonHandler implements IMessage<SyncDragonHandler.Data> {

    public static void handleClient(final Data message, final IPayloadContext context) {
        context.enqueueWork(() -> ClientProxy.handleSynchronizeDragonCap(message));
    }

    public static void handleServer(final Data message, final IPayloadContext context) {
        Player sender = context.player();
        context.enqueueWork(() -> {
            DragonStateProvider.getOptional(sender).ifPresent(cap -> {
                if (message.dragonType == null && cap.getType() != null) {
                    DragonCommand.reInsertClawTools(sender, cap);
                }

                cap.setIsDiving(message.diving);
                cap.setType(message.dragonType, sender);
                cap.setBody(message.dragonBody, sender);
                cap.setSize(message.size, sender);
                cap.setHasFlight(message.hasWings);
                cap.setPassengerId(message.passengerId);
            });
        }).thenRun(() -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), message));
    }

    public record Data(int playerId, boolean diving, AbstractDragonType dragonType, AbstractDragonBody dragonBody,
                    double size, boolean hasWings, int passengerId) implements CustomPacketPayload {
        public static final Type<Data> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "dragon_cap"));

        public static StreamCodec<ByteBuf, Data> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(ByteBuf pBuffer, Data pValue) {
                pBuffer.writeInt(pValue.playerId);
                Utf8String.write(pBuffer, pValue.dragonType != null ? pValue.dragonType.getSubtypeName() : "none", 32);
                Utf8String.write(pBuffer, pValue.dragonBody != null ? pValue.dragonBody.getBodyName() : "none", 32);
                pBuffer.writeBoolean(pValue.diving);
                pBuffer.writeDouble(pValue.size);
                pBuffer.writeBoolean(pValue.hasWings);
                pBuffer.writeInt(pValue.passengerId);
            }

            @Override
            public Data decode(ByteBuf pBuffer) {
                int id = pBuffer.readInt();
                String typeS = Utf8String.read(pBuffer, 32);
                String typeB = Utf8String.read(pBuffer, 32);
                AbstractDragonType type = typeS.equals("none") ? null : DragonTypes.getStaticSubtype(typeS);
                AbstractDragonBody body = typeB.equals("none") ? null : DragonBodies.getStatic(typeB);
                boolean diving = pBuffer.readBoolean();
                double size = pBuffer.readDouble();
                boolean hasWings = pBuffer.readBoolean();
                int passengerId = pBuffer.readInt();
                return new Data(id, diving, type, body, size, hasWings, passengerId);
            }
        };

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}