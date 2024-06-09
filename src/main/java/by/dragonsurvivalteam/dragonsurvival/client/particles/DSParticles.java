package by.dragonsurvivalteam.dragonsurvival.client.particles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.LargeFireParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.LargeFireParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.SmallFireParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.SmallFireParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.ForestDragon.LargePoisonParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.ForestDragon.LargePoisonParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.ForestDragon.SmallPoisonParticle.ForestFactory;
import by.dragonsurvivalteam.dragonsurvival.client.particles.ForestDragon.SmallPoisonParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaDragon.LargeLightningParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaDragon.LargeLightningParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaDragon.SmallLightningParticle.SeaFactory;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaDragon.SmallLightningParticleData;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DSParticles{
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DragonSurvivalMod.MODID);
	public static final RegistryObject<ParticleType<TreasureParticleData>> TREASURE = REGISTRY.register("treasures", () -> new ParticleType<TreasureParticleData>(false, TreasureParticleData.DESERIALIZER){
		@Override
		public Codec<TreasureParticleData> codec(){
			return TreasureParticleData.CODEC;
		}
	});
	public static SimpleParticleType fireBeaconParticle, magicBeaconParticle, peaceBeaconParticle;
	public static SimpleParticleType seaSweep;
	public static SimpleParticleType snowflake;

	public static void register()
	{
		fireBeaconParticle = new SimpleParticleType(false);
		REGISTRY.register("netherite_particle", ()->fireBeaconParticle);

		peaceBeaconParticle = new SimpleParticleType(false);
		REGISTRY.register("gold_particle", ()->peaceBeaconParticle);

		magicBeaconParticle = new SimpleParticleType(false);
		REGISTRY.register("diamond_particle", ()->magicBeaconParticle);

		seaSweep = new SimpleParticleType(false);
		REGISTRY.register("sea_sweep", ()->seaSweep);

		snowflake = new SimpleParticleType(false);
		REGISTRY.register("snowflake", ()->snowflake);
	}

	//Insecure modifications
	//LargePoisonParticleData
	@SubscribeEvent( priority = EventPriority.LOWEST)
	public static void registerParticles(RegisterParticleProvidersEvent event){
		event.register(DSParticles.FIRE.get(), SmallFireParticle.FireFactory::new);
		event.register(DSParticles.LARGE_FIRE.get(), LargeFireParticle.FireFactory::new);
		event.register(DSParticles.POISON.get(), ForestFactory::new);
		event.register(DSParticles.LARGE_POISON.get(), LargePoisonParticle.ForestFactory::new);
		event.register(DSParticles.LIGHTNING.get(), SeaFactory::new);
		event.register(DSParticles.LARGE_LIGHTNING.get(), LargeLightningParticle.SeaFactory::new);
		event.register(DSParticles.TREASURE.get(), by.dragonsurvivalteam.dragonsurvival.client.particles.TreasureParticle.Factory::new);
	}

	public static final RegistryObject<ParticleType<SmallFireParticleData>> FIRE = REGISTRY.register("fire", () -> new ParticleType<SmallFireParticleData>(false, SmallFireParticleData.DESERIALIZER){
		@Override
		public Codec<SmallFireParticleData> codec(){
			return SmallFireParticleData.CODEC(FIRE.get());
		}
	});


	public static final RegistryObject<ParticleType<LargeFireParticleData>> LARGE_FIRE = REGISTRY.register("large_fire", () -> new ParticleType<LargeFireParticleData>(false, LargeFireParticleData.DESERIALIZER){
		@Override
		public Codec<LargeFireParticleData> codec(){
			return LargeFireParticleData.CODEC(LARGE_FIRE.get());
		}
	});


	public static final RegistryObject<ParticleType<SmallPoisonParticleData>> POISON = REGISTRY.register("poison", () -> new ParticleType<SmallPoisonParticleData>(false, SmallPoisonParticleData.DESERIALIZER){
		@Override
		public Codec<SmallPoisonParticleData> codec(){
			return SmallPoisonParticleData.CODEC(POISON.get());
		}
	});

	public static final RegistryObject<ParticleType<LargePoisonParticleData>> LARGE_POISON = REGISTRY.register("large_poison", () -> new ParticleType<LargePoisonParticleData>(false, LargePoisonParticleData.DESERIALIZER){
		@Override
		public Codec<LargePoisonParticleData> codec(){
			return LargePoisonParticleData.CODEC(LARGE_POISON.get());
		}
	});

	public static final RegistryObject<ParticleType<SmallLightningParticleData>> LIGHTNING = REGISTRY.register("lightning", () -> new ParticleType<SmallLightningParticleData>(false, SmallLightningParticleData.DESERIALIZER){
		@Override
		public Codec<SmallLightningParticleData> codec(){
			return SmallLightningParticleData.CODEC(LIGHTNING.get());
		}
	});

	public static final RegistryObject<ParticleType<LargeLightningParticleData>> LARGE_LIGHTNING = REGISTRY.register("large_lightning", () -> new ParticleType<LargeLightningParticleData>(false, LargeLightningParticleData.DESERIALIZER){
		@Override
		public Codec<LargeLightningParticleData> codec(){
			return LargeLightningParticleData.CODEC(LARGE_LIGHTNING.get());
		}
	});
}