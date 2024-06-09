package by.dragonsurvivalteam.dragonsurvival.client;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.SourceOfMagicScreen;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.KeyInputHandler;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.KnightModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.PrinceModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.PrincessHorseModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.projectiles.FireballModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.projectiles.LightningBallModel;
import by.dragonsurvivalteam.dragonsurvival.client.particles.BeaconParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaSweepParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SnowflakeParticle;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.DragonBeaconRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.HelmetEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.*;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.*;
import by.dragonsurvivalteam.dragonsurvival.registry.DSContainers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DSTileEntities;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT )
@SuppressWarnings( "unused" )
public class ClientModEvents{

	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		if(event.getAtlas().location() == TextureAtlas.LOCATION_BLOCKS){
			event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/cage"));
			event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/wind"));
			event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/open_eye"));
			event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/wind_vertical"));

			event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "gui/dragon_claws_axe"));
			event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "gui/dragon_claws_pickaxe"));
			event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "gui/dragon_claws_shovel"));
			event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "gui/dragon_claws_sword"));
		}

		DragonSurvivalMod.LOGGER.info("Successfully added sprites!");
	}
	@SubscribeEvent
	public static void setup(FMLClientSetupEvent event)
	{
		/*ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_stone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_sandstone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_red_sandstone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_purpur_block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_oak_log, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_nether_bricks, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_mossy_cobblestone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_blackstone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_birch_log, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.birchDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.acaciaDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.peaceDragonBeacon, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.fireDragonBeacon, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.magicDragonBeacon, RenderType.cutout());

		// enable transparency for certain small doors
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.birchSmallDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.acaciaSmallDoor, RenderType.cutout());*/

		EntityRenderers.register(DSEntities.DRAGON_SPIKE, DragonSpikeRenderer::new);
		EntityRenderers.register(DSEntities.BLIZZARD_SPIKE, BlizzardSpikeRenderer::new);

		EntityRenderers.register(DSEntities.BOLAS_ENTITY, BolasEntityRenderer::new);

		EntityRenderers.register(DSEntities.PRINCESS, PrincessRenderer::new);
		EntityRenderers.register(DSEntities.HUNTER_HOUND, HunterHoundRenderer::new);
		EntityRenderers.register(DSEntities.SHOOTER_HUNTER, ShooterHunterRenderer::new);
		EntityRenderers.register(DSEntities.SQUIRE_HUNTER, SquireHunterRenderer::new);

		BlockEntityRenderers.register(DSTileEntities.helmetTile, HelmetEntityRenderer::new);
		BlockEntityRenderers.register(DSTileEntities.dragonBeacon, DragonBeaconRenderer::new);

		//ShaderHelper.initShaders();

		MenuScreens.register(DSContainers.nestContainer, SourceOfMagicScreen::new);
		MenuScreens.register(DSContainers.dragonContainer, DragonScreen::new);

		//Gecko renderers
		EntityRenderers.register(DSEntities.BALL_LIGHTNING, manager -> new BallLightningRenderer(manager, new LightningBallModel()));
		EntityRenderers.register(DSEntities.FIREBALL, manager -> new FireBallRenderer(manager, new FireballModel()));

		EntityRenderers.register(DSEntities.DRAGON, manager -> new DragonRenderer(manager, ClientDragonRender.dragonModel));
		EntityRenderers.register(DSEntities.DRAGON_ARMOR, manager -> new DragonRenderer(manager, ClientDragonRender.dragonArmorModel));
		EntityRenderers.register(DSEntities.KNIGHT, manager -> new KnightRenderer(manager, new KnightModel()));
		EntityRenderers.register(DSEntities.PRINCESS_ON_HORSE, manager -> new PrincessHorseRenderer(manager, new PrincessHorseModel()));
		EntityRenderers.register(DSEntities.PRINCE_ON_HORSE, manager -> new PrinceHorseRenderer(manager, new PrinceModel()));
	}

	@SubscribeEvent
	public static void onKeyRegister(RegisterKeyMappingsEvent event)
	{
		KeyInputHandler.registerKeys(event);
	}
	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent event){
		event.register(DSParticles.fireBeaconParticle, p_create_1_ -> new ParticleProvider<SimpleParticleType>(){
			@Override
			public @NotNull Particle createParticle(@NotNull SimpleParticleType p_199234_1_, @NotNull ClientLevel clientWorld, double v, double v1, double v2, double v3, double v4, double v5) {
				BeaconParticle beaconParticle = new BeaconParticle(clientWorld, v, v1, v2, v3, v4, v5);
				beaconParticle.pickSprite(p_create_1_);
				return beaconParticle;
			}
		});
		event.register(DSParticles.magicBeaconParticle, p_create_1_ -> new ParticleProvider<SimpleParticleType>(){
			@Override
			public @NotNull Particle createParticle(@NotNull SimpleParticleType p_199234_1_, @NotNull ClientLevel clientWorld, double v, double v1, double v2, double v3, double v4, double v5){
				BeaconParticle beaconParticle = new BeaconParticle(clientWorld, v, v1, v2, v3, v4, v5);
				beaconParticle.pickSprite(p_create_1_);
				return beaconParticle;
			}
		});
		event.register(DSParticles.peaceBeaconParticle, p_create_1_ -> new ParticleProvider<SimpleParticleType>(){
			@Override
			public @NotNull Particle createParticle(@NotNull SimpleParticleType p_199234_1_, @NotNull ClientLevel clientWorld, double v, double v1, double v2, double v3, double v4, double v5){
				BeaconParticle beaconParticle = new BeaconParticle(clientWorld, v, v1, v2, v3, v4, v5);
				beaconParticle.pickSprite(p_create_1_);
				return beaconParticle;
			}
		});

		event.register(DSParticles.seaSweep, p_create_1_ -> new ParticleProvider<SimpleParticleType>(){
			@Override
			public @NotNull Particle createParticle(@NotNull SimpleParticleType p_199234_1_, @NotNull ClientLevel clientWorld, double v, double v1, double v2, double v3, double v4, double v5){
				SeaSweepParticle beaconParticle = new SeaSweepParticle(clientWorld, v, v1, v2, v3, p_create_1_);
				beaconParticle.pickSprite(p_create_1_);
				return beaconParticle;
			}
		});
		event.register(DSParticles.snowflake, p_create_1_ -> new ParticleProvider<SimpleParticleType>(){
			@Override
			public @NotNull Particle createParticle(@NotNull SimpleParticleType p_199234_1_, @NotNull ClientLevel clientWorld, double x, double y, double z, double vX, double vY, double vZ){
				SnowflakeParticle snowflakeParticle = new SnowflakeParticle(clientWorld, x, y, z, vX, vY, vZ);
				snowflakeParticle.pickSprite(p_create_1_);
				return snowflakeParticle;
			}
		});
	}
}