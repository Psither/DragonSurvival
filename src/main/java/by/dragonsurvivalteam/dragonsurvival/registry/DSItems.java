package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.client.extensions.ShakeWhenUsedExtension;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.dragon_leg;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.dragon_body;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.dragon_helmet;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.dragon_shoe;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.SeaDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.items.*;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.DragonHunterWeapon;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.EvilDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.GoodDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.food.ChargedSoupItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.food.DragonFoodItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarBoneItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;
import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.res;

@SuppressWarnings("unused")
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DSItems {

	private static class CustomHoverTextItem extends Item {
		private final String description;

		public CustomHoverTextItem(Properties properties, String description) {
			super(properties);
			this.description = description;
		}

		@Override
		public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
			super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
			pTooltipComponents.add(Component.translatable(description));
		}
	}

	public static final DeferredRegister<Item> DS_ITEMS = DeferredRegister.create(
			BuiltInRegistries.ITEM,
			MODID
	);

	private static final Properties defaultProperties = new Item.Properties();
	private static final Properties defaultNonFoodProperties = new Item.Properties(){
		@Override
		public Item.Properties food(FoodProperties pFood) {
			return null;
		}
	};

	public static final Holder<Item> STAR_BONE = DS_ITEMS.register("star_bone", () -> new StarBoneItem(defaultProperties));
	public static final Holder<Item> STAR_HEART = DS_ITEMS.register("star_heart", () -> new StarHeartItem(defaultProperties));

	public static final Holder<Item> ELDER_DRAGON_DUST = DS_ITEMS.register("elder_dragon_dust", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.elderDragonDust"));
	public static final Holder<Item> ELDER_DRAGON_BONE = DS_ITEMS.register("elder_dragon_bone", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.elderDragonBone"));

	public static final Holder<Item> DRAGON_HEART_SHARD = DS_ITEMS.register("heart_element", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.heartElement"));
	public static final Holder<Item> WEAK_DRAGON_HEART = DS_ITEMS.register("weak_dragon_heart", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.weakDragonHeart"));
	public static final Holder<Item> ELDER_DRAGON_HEART = DS_ITEMS.register("elder_dragon_heart", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.elderDragonHeart"));

	public static final Holder<Item> CHARGED_COAL = DS_ITEMS.register("charged_coal", () -> new ChargedCoalItem(defaultProperties, DragonTypes.CAVE , LivingEntity::removeAllEffects));
	public static final Holder<Item> CHARGED_SOUP = DS_ITEMS.register("charged_soup", () -> new ChargedSoupItem(defaultProperties));
	public static final Holder<Item> CHARRED_MEAT = DS_ITEMS.register("charred_meat", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> CHARRED_VEGETABLE = DS_ITEMS.register("charred_vegetable", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> CHARRED_MUSHROOM = DS_ITEMS.register("charred_mushroom", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> CHARRED_SEAFOOD = DS_ITEMS.register("charred_seafood", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> HOT_DRAGON_ROD = DS_ITEMS.register("hot_dragon_rod", () -> new DragonFoodItem(defaultProperties, DragonTypes.CAVE, () -> new MobEffectInstance(DSEffects.FIRE, Functions.minutesToTicks(1))));
	public static final Holder<Item> EXPLOSIVE_COPPER = DS_ITEMS.register("explosive_copper", () -> new DragonFoodItem(defaultProperties, null, e -> {
		e.hurt(e.damageSources().explosion(e, e), 1f);
		e.level().addParticle(ParticleTypes.EXPLOSION, e.getX(), e.getEyeY(), e.getZ(), 1.0D, 0.0D, 0.0D);
		e.level().playSound(null, BlockPosHelper.get(e.getEyePosition()), SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR, SoundSource.PLAYERS, 1f, 1f);
	}));
	public static final Holder<Item> QUARTZ_EXPLOSIVE_COPPER = DS_ITEMS.register("quartz_explosive_copper", () -> new DragonFoodItem(defaultProperties, DragonTypes.CAVE, e -> {
		e.removeEffect(MobEffects.POISON);
		e.removeEffect(MobEffects.WITHER);
	}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)));
	public static final Holder<Item> DOUBLE_QUARTZ = DS_ITEMS.register("double_quartz", () -> new DragonFoodItem(defaultProperties, DragonTypes.CAVE, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))));

	public static final Holder<Item> SWEET_SOUR_RABBIT = DS_ITEMS.register("sweet_sour_rabbit", () -> new DragonFoodItem(defaultProperties, DragonTypes.FOREST , LivingEntity::removeAllEffects));
	public static final Holder<Item> LUMINOUS_OINTMENT = DS_ITEMS.register("luminous_ointment", () -> new DragonFoodItem(defaultProperties, DragonTypes.FOREST, () -> new MobEffectInstance(MobEffects.GLOWING, Functions.minutesToTicks(5)), () -> new MobEffectInstance(DSEffects.MAGIC, Functions.minutesToTicks(5))));
	public static final Holder<Item> DIAMOND_CHORUS = DS_ITEMS.register("diamond_chorus", () -> new DragonFoodItem(defaultProperties, DragonTypes.FOREST, e -> {
		e.removeEffect(MobEffects.POISON);
		e.removeEffect(MobEffects.WITHER);
	}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)));
	public static final Holder<Item> SMELLY_MEAT_PORRIDGE = DS_ITEMS.register("smelly_meat_porridge", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> MEAT_WILD_BERRIES = DS_ITEMS.register("meat_wild_berries", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> MEAT_CHORUS_MIX = DS_ITEMS.register("meat_chorus_mix", () -> new DragonFoodItem(defaultProperties, DragonTypes.FOREST, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))));

	public static final Holder<Item> SEASONED_FISH = DS_ITEMS.register("seasoned_fish", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> GOLDEN_CORAL_PUFFERFISH = DS_ITEMS.register("golden_coral_pufferfish", () -> new DragonFoodItem(defaultProperties, DragonTypes.SEA, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))));
	public static final Holder<Item> FROZEN_RAW_FISH = DS_ITEMS.register("frozen_raw_fish", () -> new DragonFoodItem(defaultProperties, DragonTypes.SEA , e -> {
		e.removeAllEffects();

		if(!e.level().isClientSide()){
			if(DragonStateProvider.getOrGenerateHandler(e).getType() instanceof SeaDragonType type){
				type.timeWithoutWater = 0;
			}
		}
	}));
	public static final Holder<Item> GOLDEN_TURTLE_EGG = DS_ITEMS.register("golden_turtle_egg", () -> new DragonFoodItem(defaultProperties, DragonTypes.SEA, e -> {
		e.removeEffect(MobEffects.POISON);
		e.removeEffect(MobEffects.WITHER);
	}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)));

	public static final Holder<Item> SEA_DRAGON_TREAT = DS_ITEMS.register("sea_dragon_treat", () -> new DragonTreatItem(DragonTypes.SEA, defaultProperties));
	public static final Holder<Item> CAVE_DRAGON_TREAT = DS_ITEMS.register("cave_dragon_treat", () -> new DragonTreatItem(DragonTypes.CAVE, defaultProperties));
	public static final Holder<Item> FOREST_DRAGON_TREAT = DS_ITEMS.register("forest_dragon_treat", () -> new DragonTreatItem(DragonTypes.FOREST, defaultProperties));

	public static final Holder<Item> WING_GRANT_ITEM = DS_ITEMS.register("wing_grant", () -> new WingGrantItem(defaultProperties));
	public static final Holder<Item> SPIN_GRANT_ITEM = DS_ITEMS.register("spin_grant", () -> new SpinGrantItem(defaultProperties));

	public static final Holder<Item> GOOD_DRAGON_UPGRADE = DS_ITEMS.register("good_dragon_upgrade", () -> new Item(defaultProperties.rarity(Rarity.RARE)){
			@Override
			public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
				super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
				pTooltipComponents.add(Component.translatable("ds.description.good_dragon_upgrade"));
			}
		}
	);
	public static final Supplier<ArmorItem> GOOD_DRAGON_HELMET = DS_ITEMS.register("good_dragon_helmet", () -> new GoodDragonArmorItem(
			ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(100)).rarity(Rarity.EPIC).fireResistant()) {
				@Override
				public void initializeClient(Consumer<IClientItemExtensions> consumer) {
					consumer.accept(new IClientItemExtensions() {
						@Override
						public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
							HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
									Map.of("head", new dragon_helmet<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_helmet.LAYER_LOCATION)).head, "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
							armorModel.crouching = living.isShiftKeyDown();
							armorModel.riding = defaultModel.riding;
							armorModel.young = living.isBaby();
							return armorModel;
						}
					});
				}
			}
	);
	public static final Supplier<ArmorItem> GOOD_DRAGON_CHESTPLATE = DS_ITEMS.register("good_dragon_chestplate", () -> new GoodDragonArmorItem(
			ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(100)).rarity(Rarity.EPIC).fireResistant()) {
				@Override
				public void initializeClient(Consumer<IClientItemExtensions> consumer) {
					consumer.accept(new IClientItemExtensions() {
						@Override
						@OnlyIn(Dist.CLIENT)
						public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
							HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of("body", new dragon_body(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_body.LAYER_LOCATION)).body, "left_arm",
									new dragon_body<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_body.LAYER_LOCATION)).body, "right_arm",
									new dragon_body<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_body.LAYER_LOCATION)).body, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat",
									new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
							armorModel.crouching = living.isShiftKeyDown();
							armorModel.riding = defaultModel.riding;
							armorModel.young = living.isBaby();
							return armorModel;
						}
					});
				}
			}
	);
	public static final Supplier<ArmorItem> GOOD_DRAGON_LEGGINGS = DS_ITEMS.register("good_dragon_leggings", () -> new GoodDragonArmorItem(
			ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(100)).rarity(Rarity.EPIC).fireResistant()) {
				@Override
				public void initializeClient(Consumer<IClientItemExtensions> consumer) {
					consumer.accept(new IClientItemExtensions() {
						@Override
						@OnlyIn(Dist.CLIENT)
						public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
							HumanoidModel<LivingEntity> armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(),
									Map.of("left_leg", new dragon_leg<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_leg.LAYER_LOCATION)).left_leg, "right_leg",
											new dragon_leg<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_leg.LAYER_LOCATION)).right_leg, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
											"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
							armorModel.crouching = living.isShiftKeyDown();
							armorModel.riding = defaultModel.riding;
							armorModel.young = living.isBaby();
							return armorModel;
						}
					});
				}
			}
	);
	public static final Supplier<ArmorItem> GOOD_DRAGON_BOOTS = DS_ITEMS.register("good_dragon_boots", () -> new GoodDragonArmorItem(
			ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(100)).rarity(Rarity.EPIC).fireResistant()) {
				@Override
				public void initializeClient(Consumer<IClientItemExtensions> consumer) {
					consumer.accept(new IClientItemExtensions() {
						@Override
						@OnlyIn(Dist.CLIENT)
						public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
							HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
									Map.of("left_leg", new dragon_shoe<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_shoe.LAYER_LOCATION)).left_shoe, "right_leg",
											new dragon_shoe<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_shoe.LAYER_LOCATION)).right_shoe, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
											"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
							armorModel.crouching = living.isShiftKeyDown();
							armorModel.riding = defaultModel.riding;
							armorModel.young = living.isBaby();
							return armorModel;
						}
					});
				}
			}
	);

	public static final Holder<Item> EVIL_DRAGON_UPGRADE = DS_ITEMS.register("evil_dragon_upgrade", () -> new Item(defaultProperties.rarity(Rarity.RARE)){
		@Override
		public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
			super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
			pTooltipComponents.add(Component.translatable("ds.description.evil_dragon_upgrade"));
		}
	});
	public static final Supplier<ArmorItem> EVIL_DRAGON_HELMET = DS_ITEMS.register("evil_dragon_helmet", () -> new EvilDragonArmorItem(
			ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(100)).rarity(Rarity.EPIC).fireResistant()) {
				@Override
				public void initializeClient(Consumer<IClientItemExtensions> consumer) {
					consumer.accept(new IClientItemExtensions() {
						@Override
						public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
							HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
									Map.of("head", new dragon_helmet<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_helmet.LAYER_LOCATION)).head, "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
							armorModel.crouching = living.isShiftKeyDown();
							armorModel.riding = defaultModel.riding;
							armorModel.young = living.isBaby();
							return armorModel;
						}
					});
				}
			}
	);
	public static final Supplier<ArmorItem> EVIL_DRAGON_CHESTPLATE = DS_ITEMS.register("evil_dragon_chestplate", () -> new EvilDragonArmorItem(
			ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(100)).rarity(Rarity.EPIC).fireResistant()) {
				@Override
				public void initializeClient(Consumer<IClientItemExtensions> consumer) {
					consumer.accept(new IClientItemExtensions() {
						@Override
						@OnlyIn(Dist.CLIENT)
						public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
							HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of("body", new dragon_body(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_body.LAYER_LOCATION)).body, "left_arm",
									new dragon_body<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_body.LAYER_LOCATION)).body, "right_arm",
									new dragon_body<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_body.LAYER_LOCATION)).body, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat",
									new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
							armorModel.crouching = living.isShiftKeyDown();
							armorModel.riding = defaultModel.riding;
							armorModel.young = living.isBaby();
							return armorModel;
						}
					});
				}
			}
	);
	public static final Supplier<ArmorItem> EVIL_DRAGON_LEGGINGS = DS_ITEMS.register("evil_dragon_leggings", () -> new EvilDragonArmorItem(
			ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(100)).rarity(Rarity.EPIC).fireResistant()) {
				@Override
				public void initializeClient(Consumer<IClientItemExtensions> consumer) {
					consumer.accept(new IClientItemExtensions() {
						@Override
						@OnlyIn(Dist.CLIENT)
						public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
							HumanoidModel<LivingEntity> armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(),
									Map.of("left_leg", new dragon_leg<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_leg.LAYER_LOCATION)).left_leg, "right_leg",
											new dragon_leg<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_leg.LAYER_LOCATION)).right_leg, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
											"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
							armorModel.crouching = living.isShiftKeyDown();
							armorModel.riding = defaultModel.riding;
							armorModel.young = living.isBaby();
							return armorModel;
						}
					});
				}
			}
	);
	public static final Supplier<ArmorItem> EVIL_DRAGON_BOOTS = DS_ITEMS.register("evil_dragon_boots", () -> new EvilDragonArmorItem(
			ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(100)).rarity(Rarity.EPIC).fireResistant()) {
				@Override
				public void initializeClient(Consumer<IClientItemExtensions> consumer) {
					consumer.accept(new IClientItemExtensions() {
						@Override
						@OnlyIn(Dist.CLIENT)
						public HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
							HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
									Map.of("left_leg", new dragon_shoe<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_shoe.LAYER_LOCATION)).left_shoe, "right_leg",
											new dragon_shoe<>(Minecraft.getInstance().getEntityModels().bakeLayer(dragon_shoe.LAYER_LOCATION)).right_shoe, "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "hat",
											new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
											"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
							armorModel.crouching = living.isShiftKeyDown();
							armorModel.riding = defaultModel.riding;
							armorModel.young = living.isBaby();
							return armorModel;
						}
					});
				}
			}
	);

	public static final Holder<Item> DRAGON_HUNTER_SWORD = DS_ITEMS.register("dragon_hunter_sword", () -> new DragonHunterWeapon(
			DSEquipment.DRAGON_HUNTER, new Item.Properties().rarity(Rarity.EPIC).fireResistant().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4, -2.8F)))

	);
	public static final Holder<Item> PARTISAN = DS_ITEMS.register("hunter_partisan", () -> new SwordItem(
			Tiers.IRON, new Item.Properties().component(
					DataComponents.ATTRIBUTE_MODIFIERS,
					ItemAttributeModifiers.builder()
							.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.2f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
							.add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(res("dragonsurvival.partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
							.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(res("dragonsurvival.partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
							.build()
			))
			{
				@Override
				public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
					super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
					pTooltipComponents.add(Component.translatable("ds.description.hunter_partisan"));
				}
			}
	);

	public static final Holder<Item> HUNTER_PARTISAN_DIAMOND = DS_ITEMS.register("hunter_partisan_diamond", () -> new DragonHunterWeapon(
			Tiers.DIAMOND, new Item.Properties().component(
				DataComponents.ATTRIBUTE_MODIFIERS,
				ItemAttributeModifiers.builder()
						.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.2f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
						.add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(res("dragonsurvival.partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
						.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(res("dragonsurvival.partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
						.build()
			))
			{
				@Override
				public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
					super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
					pTooltipComponents.add(Component.translatable("ds.description.hunter_partisan"));
				}
			}
	);

	public static final Holder<Item> HUNTER_PARTISAN_NETHERITE = DS_ITEMS.register("hunter_partisan_netherite", () -> new DragonHunterWeapon(
					Tiers.NETHERITE, new Item.Properties().component(
					DataComponents.ATTRIBUTE_MODIFIERS,
					ItemAttributeModifiers.builder()
							.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.2f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
							.add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(res("dragonsurvival.partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
							.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(res("dragonsurvival.partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
							.build()
			))
			{
				@Override
				public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
					super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
					pTooltipComponents.add(Component.translatable("ds.description.hunter_partisan"));
				}
			}
	);

	public static final String GOOD_DRAGON_KEY_ID = "good_dragon_key";
	public static final Holder<Item> GOOD_DRAGON_KEY = DS_ITEMS.register(GOOD_DRAGON_KEY_ID, () -> new RotatingKeyItem(
			new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f()),
			res("geo/" + GOOD_DRAGON_KEY_ID + ".geo.json"),
			res("textures/item/" + GOOD_DRAGON_KEY_ID + ".png"),
			res("treasure_friendly"))
			{
				@Override
				public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
					super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
					pTooltipComponents.add(Component.translatable("ds.description." + GOOD_DRAGON_KEY_ID));
				}
			}
	);

	public static final String EVIL_DRAGON_KEY_ID = "evil_dragon_key";
	public static final Holder<Item> EVIL_DRAGON_KEY = DS_ITEMS.register(EVIL_DRAGON_KEY_ID, () -> new RotatingKeyItem(
			new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f()),
			res("geo/" + EVIL_DRAGON_KEY_ID + ".geo.json"),
			res("textures/item/" + EVIL_DRAGON_KEY_ID + ".png"),
			res("treasure_angry"))
			{
				@Override
				public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
					super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
					pTooltipComponents.add(Component.translatable("ds.description." + EVIL_DRAGON_KEY_ID));
				}
			}
	);

	public static final String HUNTER_KEY_ID = "hunter_key";
	public static final Holder<Item> HUNTER_DRAGON_KEY = DS_ITEMS.register(HUNTER_KEY_ID, () -> new RotatingKeyItem(
			new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f()),
			res("geo/" + HUNTER_KEY_ID + ".geo.json"),
			res("textures/item/" + HUNTER_KEY_ID + ".png"),
			res("treasure_hunter"))
			{
				@Override
				public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
					super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
					pTooltipComponents.add(Component.translatable("ds.description." + HUNTER_KEY_ID));
				}
			}
	);

	public static final Holder<Item> DRAGON_SOUL = DS_ITEMS.register("dragon_soul", () -> new DragonSoulItem(defaultNonFoodProperties.rarity(Rarity.EPIC)));

	public static final Holder<Item> SPEARMAN_PROMOTION = DS_ITEMS.register("spearman_promotion", () -> new Item(defaultNonFoodProperties.rarity(Rarity.COMMON))
	{
		@Override
		public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
			super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
			pTooltipComponents.add(Component.translatable("ds.description.spearman_promotion"));
		}
	});

	// Items that shouldn't show up in the creative tab
	public static final Holder<Item> BOLAS = DS_ITEMS.register("bolas", () -> new BolasArrowItem(new Item.Properties()));
	public static final Holder<Item> HUNTING_NET = DS_ITEMS.register("dragon_hunting_mesh", () -> new Item(new Item.Properties()));
	public static final Holder<Item> LIGHTNING_TEXTURE_ITEM = DS_ITEMS.register("lightning", () -> new Item(new Item.Properties()));

	public static final Holder<Item> PASSIVE_MAGIC_BEACON = DS_ITEMS.register("beacon_magic_1", () -> new Item(new Item.Properties()));
	public static final Holder<Item> PASSIVE_PEACE_BEACON = DS_ITEMS.register("beacon_peace_1", () -> new Item(new Item.Properties()));
	public static final Holder<Item> PASSIVE_FIRE_BEACON = DS_ITEMS.register("beacon_fire_1", () -> new Item(new Item.Properties()));

	public static final Holder<Item> INACTIVE_MAGIC_DRAGON_BEACON = DS_ITEMS.register("beacon_magic_0", () -> new Item(new Item.Properties()));
	public static final Holder<Item> INACTIVE_PEACE_DRAGON_BEACON = DS_ITEMS.register("beacon_peace_0", () -> new Item(new Item.Properties()));
	public static final Holder<Item> INACTIVE_FIRE_DRAGON_BEACON = DS_ITEMS.register("beacon_fire_0", () -> new Item(new Item.Properties()));

	@SubscribeEvent
	public static void registerItemExtensions(RegisterClientExtensionsEvent event){
		event.registerItem(new ShakeWhenUsedExtension(), DRAGON_SOUL.value());
		/*event.registerItem(new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
				return new dragon_helmet<>(original.head);
			}
		}, GOOD_DRAGON_HELMET.get(), EVIL_DRAGON_HELMET.get());*/
	}
}