package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class SynchronizeDragonCap implements IMessage<SynchronizeDragonCap> {
	public int playerId;
	public boolean diving;
	public AbstractDragonType dragonType;
	public AbstractDragonBody dragonBody;
	public double size;
	public boolean hasWings;
	public int passengerId;

	public SynchronizeDragonCap() { /* Nothing to do */ }

	public SynchronizeDragonCap(int playerId, boolean diving, final AbstractDragonType dragonType, final AbstractDragonBody dragonBody, double size, boolean hasWings, int passengerId) {
		this.playerId = playerId;
		this.diving = diving;
		this.dragonType = dragonType;
		this.dragonBody = dragonBody;
		this.size = size;
		this.hasWings = hasWings;
		this.passengerId = passengerId;
	}

	@Override
	public void encode(final SynchronizeDragonCap message, final FriendlyByteBuf buffer) {
		buffer.writeInt(message.playerId);
		buffer.writeUtf(message.dragonType != null ? message.dragonType.getSubtypeName() : "none");
		buffer.writeUtf(message.dragonBody != null ? message.dragonBody.getBodyName() : "none");
		buffer.writeBoolean(message.diving);
		buffer.writeDouble(message.size);
		buffer.writeBoolean(message.hasWings);
		buffer.writeInt(message.passengerId);
	}

	@Override
	public SynchronizeDragonCap decode(final FriendlyByteBuf buffer) {
		int id = buffer.readInt();
		String typeS = buffer.readUtf();
		String typeB = buffer.readUtf();
		AbstractDragonType type = typeS.equals("none") ? null : DragonTypes.getStaticSubtype(typeS);
		AbstractDragonBody body = typeB.equals("none") ? null : DragonBodies.getStatic(typeB);
		boolean diving = buffer.readBoolean();
		double size = buffer.readDouble();
		boolean hasWings = buffer.readBoolean();
		int passengerId = buffer.readInt();
		return new SynchronizeDragonCap(id, diving, type, body, size, hasWings, passengerId);
	}

	@Override
	public void handle(final SynchronizeDragonCap message, final Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();

		if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			context.enqueueWork(() -> ClientProxy.handleSynchronizeDragonCap(message));
		} else if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), message);

			context.enqueueWork(() -> {
				ServerPlayer sender = context.getSender();
				DragonStateProvider.getCap(sender).ifPresent(handler -> {
					if (message.dragonType == null && handler.getType() != null) {
						DragonCommand.reInsertClawTools(sender, handler);
					}

					handler.setIsDiving(message.diving);
					handler.setType(message.dragonType, sender);
					handler.setBody(message.dragonBody, sender);
					handler.setSize(message.size, sender);
					handler.setHasFlight(message.hasWings);
					handler.setPassengerId(message.passengerId);
				});
			});
		}

		context.setPacketHandled(true);
	}
}