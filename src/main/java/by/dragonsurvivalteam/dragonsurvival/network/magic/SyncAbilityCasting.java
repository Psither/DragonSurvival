package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class SyncAbilityCasting implements IMessage<SyncAbilityCasting> {
	public int playerId;
	public boolean isCasting;
	public int abilitySlot;
	public CompoundTag nbt;
	public long castStartTime;
	public long clientTime;

	public SyncAbilityCasting() { /* Nothing to do */ }

//	public SyncAbilityCasting(int playerId, boolean isCasting, final CompoundTag nbt) {
//		this.playerId = playerId;
//		this.isCasting = isCasting;
//		this.nbt = nbt;
//	}
	
	public SyncAbilityCasting(int playerId, boolean isCasting, int abilitySlot, final CompoundTag nbt, long castStartTime, long clientTime) {
		this.playerId = playerId;
		this.isCasting = isCasting;
		this.abilitySlot = abilitySlot;
		this.nbt = nbt;
		this.castStartTime = castStartTime;
		this.clientTime = clientTime;
	}

	@Override
	public void encode(final SyncAbilityCasting message, final FriendlyByteBuf buffer) {
		buffer.writeInt(message.playerId);
		buffer.writeBoolean(message.isCasting);
		buffer.writeInt(message.abilitySlot);
		buffer.writeNbt(message.nbt);
		buffer.writeLong(message.castStartTime);
		buffer.writeLong(message.clientTime);
	}

	@Override
	public SyncAbilityCasting decode(final FriendlyByteBuf buffer) {
		int playerId = buffer.readInt();
		return new SyncAbilityCasting(playerId, buffer.readBoolean(), buffer.readInt(), buffer.readNbt(), buffer.readLong(), buffer.readLong());
	}

	@Override
	public void handle(final SyncAbilityCasting message, final Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();

		if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			context.enqueueWork(() -> ClientProxy.handleSyncAbilityCasting(message));
		} else if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			context.enqueueWork(() -> {
				ServerPlayer sender = context.getSender();

				DragonStateProvider.getCap(sender).ifPresent(handler -> {
					ActiveDragonAbility ability = handler.getMagicData().getAbilityFromSlot(message.abilitySlot);
					ability.loadNBT(message.nbt);
					handler.getMagicData().isCasting = message.isCasting;

					if (message.isCasting) {
						ability.onKeyPressed(sender, () -> {}, message.castStartTime, message.clientTime);
					} else {
						ability.onKeyReleased(sender);
					}
				});

				NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender), new SyncAbilityCasting(sender.getId(), message.isCasting, message.abilitySlot, message.nbt, message.castStartTime, message.clientTime));
			});
		}

		context.setPacketHandled(true);
	}
}