package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public class FakeClientPlayerUtils {
	private static final ConcurrentHashMap<Integer, FakeClientPlayer> FAKE_PLAYERS = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, DragonEntity> FAKE_DRAGONS = new ConcurrentHashMap<>();

	public static DragonEntity getFakeDragon(int index, final DragonStateHandler handler) {
		FakeClientPlayer clientPlayer = getFakePlayer(index, handler);

		FAKE_DRAGONS.computeIfAbsent(index, key -> new DragonEntity(DSEntities.DRAGON.get(), clientPlayer.level()) {
			@Override
			public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
				AnimationController<DragonEntity> ac = new AnimationController<DragonEntity>(this, "fake_player_controller", 2, state -> {
					if (FAKE_PLAYERS.get(index).handler.refreshBody) {
						clientPlayer.animationController.forceAnimationReset();
						FAKE_PLAYERS.get(index).handler.refreshBody = false;
						return PlayState.STOP;
					}

					if (getPlayer() instanceof FakeClientPlayer) {
						if (clientPlayer.animationSupplier != null) {
							return state.setAndContinue(RawAnimation.begin().thenLoop(clientPlayer.animationSupplier.get()));
						}
					}

					return PlayState.STOP;
				});
				if (getPlayer() instanceof FakeClientPlayer fcp) {
					fcp.animationController = ac;
				}
				controllers.add(ac);
			}

			@Override
			public Player getPlayer() {
				return clientPlayer;
			}
		});

		return FAKE_DRAGONS.get(index);
	}

	public static FakeClientPlayer getFakePlayer(int num, DragonStateHandler handler){
		FAKE_PLAYERS.computeIfAbsent(num, FakeClientPlayer::new);
		FAKE_PLAYERS.get(num).handler = handler;
		FAKE_PLAYERS.get(num).lastAccessed = System.currentTimeMillis();
		return FAKE_PLAYERS.get(num);
	}

	@SubscribeEvent
	public static void clientTick(ClientTickEvent event){
		FAKE_PLAYERS.forEach((i, v) -> {
			if(System.currentTimeMillis() - v.lastAccessed >= TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)){
				v.remove(RemovalReason.DISCARDED);
				FAKE_DRAGONS.get(i).remove(RemovalReason.DISCARDED);

				FAKE_DRAGONS.remove(i);
				FAKE_PLAYERS.remove(i);
			}
		});
	}
}