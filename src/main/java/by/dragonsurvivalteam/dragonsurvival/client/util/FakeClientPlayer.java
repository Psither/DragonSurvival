package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import software.bernie.geckolib.core.animation.AnimationController;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

@OnlyIn( Dist.CLIENT )
public class FakeClientPlayer extends AbstractClientPlayer{
	private static final ResourceLocation STEVE_SKIN_LOCATION = new ResourceLocation("textures/entity/player/slim/steve.png");
	private static final ResourceLocation ALEX_SKIN_LOCATION = new ResourceLocation("textures/entity/player/slim/alex.png");
	public DragonStateHandler handler = new DragonStateHandler();
	public Supplier<String> animationSupplier = null;
	public AnimationController<DragonEntity> animationController = null;
	public Long lastAccessed;
	public int number;

	public FakeClientPlayer(int number){
		super(Minecraft.getInstance().level, new GameProfile(UUID.randomUUID(), "FAKE_PLAYER_" + number));
		this.number = number;
	}

	@Override
	public @NotNull ResourceLocation getSkinTextureLocation(){
		return number % 2 == 0 ? STEVE_SKIN_LOCATION : ALEX_SKIN_LOCATION;
	}

	@Override
	public boolean shouldRender(double pX, double pY, double pZ){
		return true;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double pDistance){
		return true;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(){return null;}

	@Override
	public void tick(){return;}

	@Override
	public void die(DamageSource source){return;}

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound){}

	@Override
	public void addAdditionalSaveData(CompoundTag pCompound){}

	@Override
	public boolean isInvulnerableTo(DamageSource source){return true;}

	@Override
	public boolean canHarmPlayer(Player player){return false;}

	@Override
	public void displayClientMessage(Component chatComponent, boolean actionBar){}

	@Override
	public void awardStat(Stat par1StatBase, int par2){}

	@Override
	public boolean shouldShowName(){
		return false;
	}

	@Override
	public Component getDisplayName(){
		return Component.empty();
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(
		@Nonnull
			Capability<T> cap){return LazyOptional.empty();}

	@Override
	public boolean saveAsPassenger(CompoundTag pCompound){return false;}

	@Override
	public boolean save(CompoundTag pCompound){return false;}

	@Override
	@Nullable
	public MinecraftServer getServer(){return Minecraft.getInstance().getSingleplayerServer();}

	@Override
	public Vec3 position(){return new Vec3(0, 0, 0);}

	@Override
	public BlockPos blockPosition(){return BlockPos.ZERO;}

	@Override
	public void onAddedToWorld(){}
}