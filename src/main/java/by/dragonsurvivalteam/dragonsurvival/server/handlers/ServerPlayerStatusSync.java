package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.CompleteDataSync;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber
public class ServerPlayerStatusSync {

	// This function is a failsafe that sync the player's data with the server after a certain period of time if it somehow gets out of sync.

	// TODO: Include some error reporting that says what went out of sync if the server and client data are not the same.
	@SubscribeEvent
	public static void onServerTick(PlayerTickEvent event){
        int syncTicks = Functions.secondsToTicks(ServerConfig.serverSyncTime);
		if(event.side == LogicalSide.CLIENT || event.phase != Phase.START || ServerConfig.serverSyncTime == -1) return;

		Player player = event.player;

		if(player.isAddedToWorld() && player.isAlive()){
			if(DragonUtils.isDragon(player)){
				DragonStateHandler handler = DragonUtils.getHandler(player);
				if(player.tickCount >= handler.lastSync + syncTicks){
                    // We don't do an initial sync here since it could result in the player syncing before their data is loaded, causing data loss.
					handler.lastSync = player.tickCount;
                    NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new CompleteDataSync(player.getId(), handler.writeNBT()));
				}
			}
		}
	}
}