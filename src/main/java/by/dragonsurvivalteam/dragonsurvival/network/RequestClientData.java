package by.dragonsurvivalteam.dragonsurvival.network;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestClientData implements IMessage<RequestClientData> {
	public DragonStateHandler handler;
	public AbstractDragonType type;
	public AbstractDragonBody body;
	public DragonLevel level;

	public RequestClientData(final AbstractDragonType type, final AbstractDragonBody body, final DragonLevel level) {
		this.type = type;
		this.body = body;
		this.level = level;
	}

	public RequestClientData() { /* Nothing to do */ }

	@Override
	public void encode(final RequestClientData message, final FriendlyByteBuf buffer) {
		buffer.writeUtf(message.type != null ? message.type.getTypeName() : "none");
		buffer.writeUtf(message.body != null ? message.body.getBodyName() : "central");
		buffer.writeEnum(message.level);
	}

	@Override
	public RequestClientData decode(final FriendlyByteBuf buffer) {
		String type = buffer.readUtf();
		String body = buffer.readUtf();
		return new RequestClientData(type.equals("none") ? null : DragonTypes.getStatic(type), DragonBodies.getStatic(body), buffer.readEnum(DragonLevel.class));
	}

	@Override
	public void handle(final RequestClientData message, final Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();

		if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			context.enqueueWork(() -> ClientProxy.handleRequestClientData(message));
		}

		context.setPacketHandled(true);
	}
}