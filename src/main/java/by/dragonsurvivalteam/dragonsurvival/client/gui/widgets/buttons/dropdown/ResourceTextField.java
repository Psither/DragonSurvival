package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.settings.widgets.ResourceTextFieldOption;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.lists.OptionsList;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigType;
import com.google.common.primitives.Primitives;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceTextField extends EditBox /*implements TooltipAccessor*/ {
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/textbox.png");
	private static final int maxItems = 6;

	private final List<ResourceEntry> suggestions = new ArrayList<>();
	private final ResourceTextFieldOption textField;

	protected DropdownList list;

	private ResourceEntry stack;

    private final String optionKey;
	// TODO :: Enum?
	private final boolean isItem;
	private final boolean isBlock;
	private final boolean isEntity;
	private final boolean isEffect;
	private final boolean isBiome;

	private final boolean configValueIsList;

	public ResourceTextField(final String optionKey, final ResourceTextFieldOption textField, int x, int y, int width, int height, final Component component) {
		super(Minecraft.getInstance().font, x, y, width, height, component);
		setBordered(false);
		this.textField = textField;
        this.optionKey = optionKey;

		Field field = ConfigHandler.configFields.get(optionKey);
		Class<?> checkType = Primitives.unwrap(field.getType());

		if (field.isAnnotationPresent(ConfigType.class)) {
			ConfigType type = field.getAnnotation(ConfigType.class);
			checkType = Primitives.unwrap(type.value());
		}

		isItem = Item.class.isAssignableFrom(checkType);
		isBlock = Block.class.isAssignableFrom(checkType);
		isEntity = EntityType.class.isAssignableFrom(checkType);
		isEffect = MobEffect.class.isAssignableFrom(checkType);
		isBiome = Biome.class.isAssignableFrom(checkType);

		configValueIsList = field.getType().isAssignableFrom(List.class);
		list = new DropdownList(getX(), getY() + this.height, this.width, 0, 23);
		// So that when you click on the suggestion entries it fills the text field
		Minecraft.getInstance().screen.children.add(list);
		update();
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (shouldBeHidden()) {
			return;
		}

		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		// Re-check if still focused
		if ((isFocused() || list != null) && (!visible || !isMouseOver(mouseX, mouseY) && !list.isMouseOver(mouseX, mouseY))) {
			setFocused(false);
		}

		if (isFocused() && list != null) {
			list.reposition(getX(), getY() + height, width, Math.min(suggestions.size() + 1, maxItems) * height);
		}

		if (list != null && list.visible) {
			list.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public boolean isActive() {
		if (shouldBeHidden()) {
			return false;
		}

		return super.isActive();
	}

	public void update() {
		stack = null;
		suggestions.clear();

		if (list != null) {
			list.children().clear();
		}

		String resource = getValue().isEmpty() && textField != null ? textField.getter.apply(Minecraft.getInstance().options) : getValue();

		// Only keep the actual resource location (namespace:path)
		while (StringUtils.countMatches(resource, ":") > 1) {
			resource = resource.substring(0, resource.lastIndexOf(":"));
		}

		List<ResourceEntry> resourceEntries = parseCombinedList(Collections.singletonList(resource), true);

		for (ResourceEntry resourceEntry : resourceEntries) {
			if (!resourceEntry.displayItems.isEmpty()) {
				stack = resourceEntry;
				break;
			} else if (resourceEntries.indexOf(resourceEntry) == resourceEntries.size() - 1) {
				stack = resourceEntry;
			}
		}

		if (!isFocused()) {
			return;
		}

		fillSuggestions(resource);

		if (suggestions.isEmpty() && !resource.isEmpty()) {
			fillSuggestions("");
		}

		suggestions.removeIf(entry -> entry.id.isEmpty());
		suggestions.removeIf(ResourceEntry::isEmpty);
		suggestions.sort((entryOne, entryTwo) -> entryTwo.mod.compareTo(entryOne.mod));
		suggestions.sort(Comparator.comparing(c -> c.id));

		for (int i = 0; i < suggestions.size(); i++) {
			ResourceEntry entry = suggestions.get(i);

			if (list != null) {
				list.addEntry(new ResourceDropdownEntry(this, i, entry, val -> {
					setValue(val.id);
					setFocused(false);
					update();
				}));
			}
		}
	}

	private void fillSuggestions(final String resource) {
		SuggestionsBuilder builder = new SuggestionsBuilder(resource, 0);

		if (isItem) {
			SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), builder);
		}

		if (isBlock) {
			SharedSuggestionProvider.suggestResource(BuiltInRegistries.BLOCK.keySet(), builder);
		}

		if (isEntity) {
			SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), builder);
		}

		if (isEffect) {
			SharedSuggestionProvider.suggestResource(BuiltInRegistries.MOB_EFFECT.keySet(), builder);
		}

		if (isBiome) {
			SharedSuggestionProvider.suggestResource(BuiltInRegistries.BIOME_SOURCE.keySet(), builder);
		}

		Suggestions sgs = builder.build();
		List<String> suggestions = new ArrayList<>(sgs.getList().stream().map(Suggestion::getText).toList());

		suggestions.removeIf(string -> string == null || string.isEmpty());
		suggestions.forEach(string -> this.suggestions.addAll(parseCombinedList(Collections.singletonList(string), true)));
	}

	/**
	 * @param values The resource locations
	 * @param isTag If this is set also search the registries for tags
	 * @return List of entries which match the given resource location
	 */
	public List<ResourceEntry> parseCombinedList(final List<String> values, boolean isTag) {
		List<ResourceEntry> results = new ArrayList<>();

		for (String value : values) {
			if (value.isEmpty() || StringUtils.countMatches(value, ":") == 0) {
				continue;
			}

			ResourceLocation location = ResourceLocation.tryParse(value);

			if (location == null) {
				continue;
			}

			// Go through the registries to create a ResourceEntry (which will contain relevant information, e.g. a fitting ItemStack to render in the text field)
			// TODO :: Currently tags are not suggested
			if (isTag) {
				if (isItem) {
					try {
						results.add(new ResourceEntry(value, Objects.requireNonNull(ForgeRegistries.ITEMS.tags().getTag(TagKey.create(ForgeRegistries.Keys.ITEMS, location)).stream().map(ItemStack::new).toList()), true));
					} catch (Exception e) {
						DragonSurvivalMod.LOGGER.debug("Error while trying to retrieve a value from the 'ITEMS' registry for the config, value: [" + value + "]", e);
					}
				}

				if (isBlock) {
					try {
						results.add(new ResourceEntry(value, Objects.requireNonNull(ForgeRegistries.BLOCKS.tags().getTag(TagKey.create(ForgeRegistries.Keys.BLOCKS, location))).stream().map(ItemStack::new).toList(), true));
					} catch (Exception e) {
						DragonSurvivalMod.LOGGER.debug("Error while trying to retrieve a value from the 'BLOCKS' registry for the config, value: [" + value + "]", e);
					}
				}

				if (isEntity) {
                    try {
                        results.add(new ResourceEntry(value, Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.tags().getTag(TagKey.create(ForgeRegistries.Keys.ENTITY_TYPES, location))).stream().map(type -> new ItemStack(ForgeSpawnEggItem.fromEntityType(type))).toList(), true));
                    } catch (Exception e) {
						DragonSurvivalMod.LOGGER.debug("Error while trying to retrieve a value from the 'ENTITY_TYPES' registry for the config, value: [" + value + "]", e);
					}
                }
			}

			LocalPlayer localPlayer = Minecraft.getInstance().player;

			if (localPlayer != null) {
				RegistryAccess registryAccess = localPlayer.level().registryAccess();

				if (isItem) {
					Optional<HolderLookup.RegistryLookup<Item>> optionalLookup = registryAccess.lookup(ForgeRegistries.Keys.ITEMS);

					optionalLookup.ifPresent(lookup -> {
						try {
							results.add(new ResourceEntry(value, Collections.singletonList(new ItemStack(ItemParser.parseForItem(lookup, new StringReader(value)).item()))));
						} catch (CommandSyntaxException ignored) { /* Nothing to do */ }
					});
				}

				if (isBlock) {
					Optional<HolderLookup.RegistryLookup<Block>> optionalLookup = registryAccess.lookup(ForgeRegistries.Keys.BLOCKS);

					optionalLookup.ifPresent(lookup -> {
						try {
							results.add(new ResourceEntry(value, Collections.singletonList(new ItemStack(BlockStateParser.parseForBlock(lookup, new StringReader(value), false).blockState().getBlock()))));
						} catch (CommandSyntaxException ignored) { /* Nothing to do */ }
					});
				}

				if (isEntity) {
					Optional<HolderLookup.RegistryLookup<Item>> optionalLookup = registryAccess.lookup(ForgeRegistries.Keys.ITEMS);

					optionalLookup.ifPresent(lookup -> {
						try {
							EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(location);

							if (entityType != null) {
								SpawnEggItem item = ForgeSpawnEggItem.fromEntityType(entityType);

								if (item != null) {
									results.add(new ResourceEntry(value, Collections.singletonList(new ItemStack(item))));
								} else {
									results.add(new ResourceEntry(value, Collections.singletonList(new ItemStack(ItemParser.parseForItem(lookup, new StringReader(value)).item()))));
								}
							}
						} catch (CommandSyntaxException ignored) { /* Nothing to do */ }
					});
				}
			}

			if (isEffect) {
                try {
                    MobEffectInstance instance = new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(location).get(), 20);
                    ItemStack stack = new ItemStack(Items.POTION);
                    PotionUtils.setPotion(stack, Potions.WATER);
                    PotionUtils.setCustomEffects(stack, Collections.singletonList(instance));
                    results.add(new ResourceEntry(value, Collections.singletonList(stack)));
                } catch (Exception e) {
					DragonSurvivalMod.LOGGER.debug("Error while trying to retrieve a value from the 'MOB_EFFECTS' registry for the config, value: [" + value + "]", e);
				}
            }
		}

		results.forEach(entry -> {
			if (entry.displayItems != null && !entry.displayItems.isEmpty()) {
				entry.displayItems = entry.displayItems.stream().filter(c -> {
					boolean blockItem = c.getItem() instanceof BlockItem;
					boolean itemNameBlockItem = c.getItem() instanceof ItemNameBlockItem;

					return !isItem ? !itemNameBlockItem : isBlock || itemNameBlockItem || !blockItem;
				}).toList();

				entry.displayItems = entry.displayItems.stream().filter(itemStack -> !itemStack.isEmpty()).toList();
			}
		});

		return results;
	}

	@Override
	public void deleteChars(int pNum){
		super.deleteChars(pNum);
		update();
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers){
		boolean val = super.charTyped(pCodePoint, pModifiers);
		update();
		return val;
	}

	@Override
	public void setFocused(boolean focus) {
		if (shouldBeHidden()) {
			return;
		}

		super.setFocused(focus);

		/* FIXME
		Currently the suggestions only properly work if they're in a sub menu which gets opened if the config value is a list
		Otherwise they get overlayed by the other OptionEntry / CategoryEntry elements
		*/
        list.visible = focus && Minecraft.getInstance().screen != null && !Minecraft.getInstance().screen.children.isEmpty() && configValueIsList;

        if (Minecraft.getInstance().screen == null) {
            DragonSurvivalMod.LOGGER.warn("Screen was not available while trying to focus 'ResourceTextField' [" + optionKey + "]");
            return;
        }

		// Hide suggestion windows which are no longer relevant
		Minecraft.getInstance().screen.children.forEach(widget -> {
			if (widget instanceof DropdownList dropdownList && dropdownList != list) {
				if (list.visible && dropdownList.visible) {
					dropdownList.visible = false;
				}
			}
		});
	}

	/** Avoid overlapping suggestion entries */
	private boolean shouldBeHidden() {
		AtomicBoolean shouldBeHidden = new AtomicBoolean(false);

		Minecraft.getInstance().screen.children.forEach(widget -> {
			if (widget instanceof OptionsList optionsList) {
				optionsList.children().forEach(listEntry -> {
					// Check if the list of any resource text field is visible
					if (!listEntry.children().isEmpty()) {
						GuiEventListener entry = listEntry.children().get(0);

						if (entry instanceof ResourceTextField resourceTextField && resourceTextField != this) {
							if (resourceTextField.list.visible && !resourceTextField.list.children().isEmpty()) {
								// Offset by item height since without it the text field below the focused one is not hidden
								if (getY() > resourceTextField.list.getTop() - 23 && getY() < resourceTextField.list.getBottom() + 3) {
									shouldBeHidden.set(true);
								}
							}
						}
					}
				});
			}
		});

		return shouldBeHidden.get();
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (shouldBeHidden()) {
			return;
		}

		int v = isHovered ? 32 : 0;

		// Background box for the list entries
		guiGraphics.blitWithBorder(BACKGROUND_TEXTURE, getX(), getY() + 1, 0, v, width, height, 32, 32, 10, 10, 10, 10);

		if (stack != null && !stack.isEmpty()) {
			stack.tick();
			guiGraphics.renderItem(stack.getDisplayItem(), getX() + 3, getY() + 3);
		}

		setX(getX() + 25);
		setY(getY() + 6);

		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
		setTextColor(14737632);

		// Sets the prompt text
		if (getValue().isEmpty() && !getMessage().toString().isBlank()) {
			boolean isFocus = isFocused();
			setFocused(false);
			int cursor = getCursorPosition();
			setCursorPosition(0);
			setTextColor(7368816);
			setValue(getMessage().getString());
			super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
			setValue("");
			setTextColor(14737632);
			setCursorPosition(cursor);
			setFocused(isFocus);
		}

		setX(getX() - 25);
		setY(getY() - 6);
	}
}